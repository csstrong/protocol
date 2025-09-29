package com.wayeal.cloud.rabbit;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.wayeal.cloud.constant.Constant;
import com.wayeal.cloud.model.ContentMessageRequest;
import com.wayeal.cloud.model.ElementResult;
import com.wayeal.cloud.model.MessageRequest;
import com.wayeal.cloud.model.RtuMessageRequest;
import com.wayeal.cloud.mongo.entity.*;
import com.wayeal.cloud.mongo.service.impl.MonitorDataServiceImpl;
import com.wayeal.cloud.redis.GetRedisStatusService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/*
 * @author  chensi
 * @date  2022/7/26
 */
@Component
public class MessageConsumer {
	public static final Logger log = LoggerFactory.getLogger(MessageConsumer.class);
	DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	private static final JSONObject configObj;

	@Resource
	private MongoTemplate mongoTemplate;

	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	@Resource
	private RedissonClient redissonClient;

	@Resource
	private GetRedisStatusService getRedisStatusService;

	static {
		try {
			String configJson = readJsonFile("default.json");
			configObj = com.alibaba.fastjson.JSON.parseObject(configJson);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error(e);
		}
	}

	public JSONObject getConfigObj() {
		return this.configObj;
	}

	@RabbitListener(queues = "${wy.in}", containerFactory = "batchQueueRabbitListenerContainerFactory")
	@RabbitHandler
	public void process(String msgData, Channel channel, Message message) {
		try {
			boolean redisConnStatus = getRedisStatusService.judgeRedisConnectionStatus();
			if (!redisConnStatus) {
				return;
			}

			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			//log.info("deliveryTag = {},待消费的消息是 {}", message.getMessageProperties().getDeliveryTag(), msgData);
			//存储报文数据到数据库
			processMsgToDB(msgData, message);
		} catch (Exception e) {
			try {
				channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
			} catch (IOException ioException) {
				log.error("message receiver is fail");
				ioException.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	/**
	 * 对mq接受到的数据进行处理
	 *
	 * @param msgData 待处理的json字符串
	 * @return
	 */
	protected void processMsgToDB(String msgData, Message message) throws InterruptedException {
		RtuMessageRequest rtuMessageRequest = JSONObject.parseObject(msgData, RtuMessageRequest.class);
		//获取遥测站的ip
		String ip = rtuMessageRequest.getIp();
		//获取报文的基础信息
		MessageRequest messageRequest = rtuMessageRequest.getMessageRequest();

		//遥测站第一次上电时，主动上报测试报，功能码为30H。测试报上报遥测站传感器数据及电源电压等信息。
		if (messageRequest.getFunctionCode().equals("30")) {
			return;
		}
		//获取报文正文的数据
		ContentMessageRequest contentMessageRequest = rtuMessageRequest.getContentMessageRequest();
		//获取发送时间：年月日时,用于key生成
		String sendingTime = contentMessageRequest.getSendingTime().substring(0, 8);

		//若base_site_info和base_station_info中不存在站点信息，则不存数据到数据库。
		String siteId = messageRequest.getTelemetryStationAddress();
		if (siteId == null) {
			return;
		}
		Query query = new Query(Criteria.where("Id").is(siteId));
		List<BaseSiteInfoEO> list1 = mongoTemplate.find(query, BaseSiteInfoEO.class);
		List<BaseStationInfoEO> list2 = mongoTemplate.find(query, BaseStationInfoEO.class);
		if (list1.isEmpty() && list2.isEmpty()) {
			log.info("deliveryTag = {},The database have not site info for {}!",
				message.getMessageProperties().getDeliveryTag(), siteId);
			return;
		}

		//判断若是心跳包，存入redis,设置ttl 2min,若过期，生成一条离线报警信息。若再次产生心跳，则消警。
		if (messageRequest.getFunctionCode().equals("2f")) {
			LocalDateTime nowTime = LocalDateTime.now();
			HeartBeatEO heartBeatEO;
			query = new Query();
			query.addCriteria(Criteria.where(Constant.SITE_ID).is(siteId));
			HeartBeatEO hbEO = this.mongoTemplate.findOne(query, HeartBeatEO.class, Constant.HEARTBEAT_INFO);
			//初始化或心跳断开，这时心跳重连，则设置值为1
			if (hbEO == null) {
				//存点位心跳状态到mongo
				heartBeatEO = new HeartBeatEO(null, siteId, nowTime.format(dateTimeFormatter), Constant.ONLINE);
				this.mongoTemplate.save(heartBeatEO);
			} else {
				//1.更新心跳表`heartbeat_info`中点位状态为在线
				query = new Query();
				query.addCriteria(Criteria.where(Constant.SITE_ID).is(siteId));
				Update update = new Update();
				update.set("UpdateTime", nowTime.format(dateTimeFormatter));
				update.set("Status", Constant.ONLINE);
				this.mongoTemplate.updateMulti(query, update, Constant.HEARTBEAT_INFO);

				//2.再次收到心跳包，消警处理
				query = new Query();
				query.addCriteria(Criteria.where(Constant.SITE_ID).is(siteId));
				query.addCriteria(Criteria.where(Constant.ALARM_OFF).is("0"));
				query.addCriteria(Criteria.where(Constant.COMPONENTID).is("heartbeat"));
				List<BaseAlarmManagementDto> list = this.mongoTemplate.find(query, BaseAlarmManagementDto.class,
					Constant.BASE_ALARM_MANAGEMENT);
				for (BaseAlarmManagementDto alarmDto : list) {
					update = new Update();
					update.set(Constant.ALARM_OFF, "1");
					String sTime = alarmDto.getStartTime();
					LocalDateTime startTime = LocalDateTime.parse(sTime, dateTimeFormatter);

					Duration duration = Duration.between(startTime, nowTime);
					long minute = duration.toMinutes();
					update.set("EndTime", nowTime.format(dateTimeFormatter));
					update.set("UpdateTime", nowTime.format(dateTimeFormatter));
					update.set("Duration", new BigDecimal(minute).setScale(2, BigDecimal.ROUND_HALF_UP).toString());

					Query query2 = new Query();
					query2.addCriteria(Criteria.where("_id").is(alarmDto.get_id()));
					this.mongoTemplate.upsert(query2, update, BaseAlarmManagementDto.class);
				}
			}
			redisTemplate.opsForValue().set(siteId + "-ttl", 1, 2, TimeUnit.MINUTES);
			log.info("deliveryTag = {},{}:心跳包消费成功。", message.getMessageProperties().getDeliveryTag(), siteId);
			return;
		} else {
			log.info("deliveryTag = {},待消费的消息是 {}", message.getMessageProperties().getDeliveryTag(), msgData);
		}

		//正文数据的解析
		//获取报文要素信息list
		List<ElementResult> elementInformationS = contentMessageRequest.getElementResultS();
		Map<String,String>  commonFactor= contentMessageRequest.getCommonFactor();
		if (!elementInformationS.isEmpty()) {

			MonitorData monitorData = new MonitorData();
			monitorData.setId(messageRequest.getTelemetryStationAddress());
			monitorData.setIp(ip.substring(1));
			LocalDateTime now = LocalDateTime.now();
			String nowTime = now.format(dateTimeFormatter);
			monitorData.setStroageTime(nowTime);

			Map<String, JSONObject> timeDataMap = new HashMap<>();
			JSONObject factorItem = configObj.getJSONObject("FactorItem");

			for (ElementResult elementResult : elementInformationS) {

				Map<String, Object> eleMap = elementResult.getValue();
				String monitorTime = "";
				String f0 = (String) eleMap.get("F0");
				if (f0 == null) {
					monitorTime = parseStrToDate(elementResult.getF0());
				} else {
					monitorTime = parseStrToDate(f0);
				}

				//如果监测报文的小时时间低于现在时间两个小时，则不存入
				int nowHour = now.getHour();
				int msgHour = LocalDateTime.parse(monitorTime, dateTimeFormatter).getHour();
				if (nowHour - msgHour >= 2) {
					//continue;
				}

				for (Map.Entry<String, Object> entry : eleMap.entrySet()) {
					if ("F0".equals(entry.getKey()) || "04".equals(entry.getKey())) {
						continue;
					}

					String identifier = entry.getKey();
					Object value = entry.getValue();

					if (value instanceof List) {
						JSONObject factorInfo =
							Optional.ofNullable(factorItem.getJSONObject(identifier)).orElse(new JSONObject());
						Map<String, String> resMap = processEleValue(elementResult, factorInfo, monitorTime, value);
						for (Map.Entry<String, String> stringEntry : resMap.entrySet()) {
							String time = stringEntry.getKey();
							String factorValue = stringEntry.getValue();

							if (timeDataMap.get(time) == null) {
								JSONObject compJson = new JSONObject();
								compJson.put(getFactorName(identifier), factorValue);
								timeDataMap.put(time, compJson);
							} else {
								JSONObject tempJo = timeDataMap.get(time);
								tempJo.put(getFactorName(identifier), factorValue);
							}
						}
					} else {
						String val = processInvalidValue((String) value);
						//若值为ff,则该因子不再存入
						if ("/".equals(val)) {
							continue;
						}
						if (timeDataMap.get(monitorTime) == null) {
							JSONObject compJson = new JSONObject();
							compJson.put(getFactorName(identifier), val);
							timeDataMap.put(monitorTime, compJson);
						} else {
							JSONObject tempJo = timeDataMap.get(monitorTime);
							tempJo.put(getFactorName(identifier), val);
						}
					}
				}
			}
			timeDataMap = sortMap(timeDataMap);
			for (Map.Entry<String, JSONObject> entry1 : timeDataMap.entrySet()) {
				JSONObject jsonObject = entry1.getValue();
				for (Map.Entry<String, String> stringEntry : commonFactor.entrySet()) {
					jsonObject.put(getFactorName(stringEntry.getKey()), stringEntry.getValue());
				}
			}
			storeMqToRedis(timeDataMap, siteId, sendingTime, monitorData);
		}
		return;
	}

	public void storeMqToRedis(Map<String, JSONObject> timeDataMap, String siteId, String sendingTime,
	                           MonitorData monitorData) {
		log.info("timeDataMap:{}", timeDataMap);

		RLock rLock = redissonClient.getLock("lock");
		String key = siteId + Constant.SEPARATOR + sendingTime;
		try {
			log.info("加锁成功,处理key为{}", key);
			if (rLock.tryLock(5, 10, TimeUnit.SECONDS)) {
				redisTemplate.opsForValue().set(key + Constant.SUFFIX_CLASS, monitorData);
				Object o = redisTemplate.opsForValue().get(key);
				if (o == null) {
					redisTemplate.opsForValue().set(key, timeDataMap, 2, TimeUnit.MINUTES);
					redisTemplate.opsForValue().set(key + Constant.SUFFIX_UNION, timeDataMap);
				} else {
					Map<String, JSONObject> srcMap = (Map<String, JSONObject>) o;
					//遍历两个map，取map中key的并集，若key相同，则合并value
					Set<String> keySet = new HashSet<>();
					keySet.addAll(srcMap.keySet());
					keySet.addAll(timeDataMap.keySet());

					List<String> keyList = new ArrayList<>(keySet);
					Map<String, JSONObject> newMap = new HashMap<>();
					for (String k : keyList) {
						JSONObject json = new JSONObject();
						json.putAll(Optional.ofNullable(srcMap.get(k)).orElse(new JSONObject()));
						json.putAll(Optional.ofNullable(timeDataMap.get(k)).orElse(new JSONObject()));
						newMap.put(k, json);
					}
					redisTemplate.opsForValue().set(key, newMap, 2, TimeUnit.MINUTES);
					redisTemplate.opsForValue().set(key + Constant.SUFFIX_UNION, newMap);
				}
			} else {
				log.info("排队中...");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//解锁
			if (rLock.isLocked()) {
				log.info("释放锁,处理key为{}", key);
				rLock.unlock();
			}
		}
	}

	public int getFactorAccuracy(JSONObject factorItem, String code) {
		int accurcy = 2;
		for (Map.Entry<String, Object> entry : factorItem.entrySet()) {
			JSONObject json = (JSONObject) entry.getValue();
			if (json.getString("id").equals(code)) {
				String value = Optional.ofNullable(json.getString("accuracy")).orElse("2");
				accurcy = Integer.parseInt(value);
			}
		}
		return accurcy;
	}

	/**
	 * 转换 yyDDhhmmss 为 yyyy/MM/dd HH:mm:ss
	 *
	 * @param s
	 * @return
	 */
	private String parseStrToDate(String s) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

		int year = 2000 + Integer.parseInt(s.substring(0, 2));
		int month = Integer.parseInt(s.substring(2, 4));
		int day = Integer.parseInt(s.substring(4, 6));
		int hour = Integer.parseInt(s.substring(6, 8));
		int minute = Integer.parseInt(s.substring(8, 10));

		if (s.length() == 12) {
			int second = Integer.parseInt(s.substring(10, 12));
			return LocalDateTime.of(year, month, day, hour, minute, second).format(dateTimeFormatter);
		}
		return LocalDateTime.of(year, month, day, hour, minute).format(dateTimeFormatter);
	}

	protected Map<String, String> processEleValue(ElementResult elementResult, JSONObject factorInfo,
	                                              String time, Object value) {

		LocalDateTime monitorTime = LocalDateTime.parse(time, dateTimeFormatter);

		Map<String, String> resMap = new HashMap<>();
		List list = (List<String>) value;

		//存在0418引导符，读取解析后面的步长码，确定时间间隔
		String stepCode = (String) elementResult.getValue().get("04");

		if (factorInfo.size() == 0) {
			//判断是否有步长
			if (stepCode != null) {
				resMap = splitTimeStep(list, monitorTime, stepCode, null);
			} else {
				resMap = generateNumberList(list, monitorTime, null);
			}
		} else {
			String accuracy = factorInfo.getString("accuracy");
			if (stepCode != null) {
				resMap = splitTimeStep(list, monitorTime, stepCode, accuracy);
			} else {
				resMap = generateNumberList(list, monitorTime, accuracy);
			}
		}
		return resMap;
	}

	protected Map<String, String> splitTimeStep(List list, LocalDateTime monitorTime, String stepCode,
	                                            String accuracy) {
		Map<String, String> resMap = new HashMap<>();
		int dayStep = 0, hourStep = 0, minuteStep = 0;
		int length = stepCode.length();
		int count = list.size();
		switch (length) {
			case 1:
			case 2:
				minuteStep = Integer.parseInt(stepCode);
				break;
			case 3:
			case 4:
				hourStep = Integer.parseInt(stepCode.substring(0, 2));
				break;
			case 5:
			case 6:
				dayStep = Integer.parseInt(stepCode.substring(0, 2));
				break;
			default:
				break;
		}

		if (dayStep != 0) {
			//int count = 30 / dayStep;
			for (int i = 0; i < count; i++) {
				if (i != 0) {
					monitorTime = monitorTime.minusDays(-dayStep);
				}
			}
		} else if (hourStep != 0) {
			//int count = 24 / hourStep;
			for (int i = 0; i < count; i++) {
				if (i != 0) {
					monitorTime = monitorTime.minusHours(-hourStep);
				}
				String v = (String) list.get(i);
				resMap.put(monitorTime.format(dateTimeFormatter), v);
			}
		} else if (minuteStep != 0) {
			//int count = 60 / minuteStep;
			for (int i = 0; i < count; i++) {
				if (i != 0) {
					monitorTime = monitorTime.minusMinutes(-minuteStep);
				}
				String v = (String) list.get(i);
				v = processHexToNumber(v, accuracy);
				if ("/".equals(v)) {
					continue;
				}
				resMap.put(monitorTime.format(dateTimeFormatter), v);
			}
		}
		return resMap;
	}

	/**
	 * bcd码解析为Number
	 *
	 * @param list
	 * @param monitorTime
	 * @return
	 */
	protected Map<String, String> generateNumberList(List list, LocalDateTime monitorTime, String accuracy) {
		Map<String, String> resMap = new HashMap<>();
		//时间步长
		int step = 60 / list.size();
		for (int i = 0; i < list.size(); i++) {
			if (i != 0) {
				monitorTime = monitorTime.minusMinutes(-step);
			}
			String v = (String) list.get(i);
			String[] split = v.split("");
			boolean f = Arrays.stream(split).allMatch(m -> m.equals("f"));
			if (f) {
				resMap.put(monitorTime.format(dateTimeFormatter), "/");
				continue;
			} else {
				if (accuracy != null) {
					int acc = Integer.parseInt(accuracy);
					resMap.put(monitorTime.format(dateTimeFormatter),
						new BigDecimal(String.valueOf(Double.valueOf(v) / Math.pow(10, acc)))
							.setScale(acc, BigDecimal.ROUND_HALF_UP).toString());
				} else {
					resMap.put(monitorTime.format(dateTimeFormatter), v);
				}
			}
		}
		return resMap;
	}

	/**
	 * 对map中key进行排序
	 *
	 * @param resMap
	 * @return
	 */
	protected Map<String, JSONObject> sortMap(Map<String, JSONObject> resMap) {
		Map<String, JSONObject> map = resMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
				(oldValue, newValue) -> oldValue, LinkedHashMap::new));
		return map;
	}

	/**
	 * 对一组hex值进行数字解析
	 *
	 * @param v
	 * @return
	 */
	protected String processHexToNumber(String v, String accuracy) {
		v = processInvalidValue(v);
		String vStr = null;
		if (!"/".equals(v) && accuracy != null) {
			double i = Double.parseDouble(v);
			int acc = Integer.parseInt(accuracy);
			vStr = new BigDecimal(String.valueOf(i / Math.pow(10, acc)))
				.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
		} else {
			vStr = v;
		}
		return vStr;
	}

	protected String getFactorName(String identifier) {
		JSONObject factorItem = configObj.getJSONObject("FactorItem");
		JSONObject factorInfo = Optional.ofNullable(factorItem.getJSONObject(identifier)).orElse(new JSONObject());
		return Optional.ofNullable(factorInfo.getString("id")).orElse(identifier);
	}

	protected String processInvalidValue(String s) {
		String[] split = s.split("");
		boolean f = Arrays.stream(split).allMatch(m -> m.equals("f"));
		if (f) {
			return "/";
		}
		return s;
	}

	public static String readJsonFile(String fileName) {
		try {
			ClassPathResource classPathResource = new ClassPathResource(fileName);
			InputStreamReader reader = new InputStreamReader(classPathResource.getInputStream(), "UTF-8");
			BufferedReader bfReader = new BufferedReader(reader);
			String tmpContent;
			StringBuilder builder = new StringBuilder();
			while ((tmpContent = bfReader.readLine()) != null) {
				builder.append(tmpContent);
			}
			bfReader.close();
			return builder.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("readJsonFile: " + e.getMessage());
		}
	}

	/**
	 * 将数据存入数据表
	 *
	 * @param o
	 * @param tableName
	 */
	@Resource
	private MonitorDataServiceImpl monitorDataServiceImpl;

	@Value("${wy.env}")
	private String env;

	public void insertDatatoTable(Object o, String tableName) {
		log.info("=====开始操作表" + tableName + "=====");
		if ("data_minute".equals(tableName)) {
			List resL = (List) o;
			log.info(resL.toString());
			boolean insertResult = monitorDataServiceImpl.insertDataToMinute(resL);
			if (insertResult) {
				log.info("=====表" + tableName + "操作结束，状态：" + insertResult + "=====");
			}
		}
		if ("data_hour".equals(tableName)) {
			log.info(((MonitorData) o).toString());
			boolean insertResult = monitorDataServiceImpl.insertDataToHour(o);
			if (insertResult) {
				//如果操作成功，向mq发送消息
				MonitorData md = (MonitorData) o;
				//用于计算报警
				if (Constant.ENV_PROD.equals(env)) {
					RabbitMqSendUtil.sendMessage(JSONObject.toJSON(md).toString());
				}
				log.info("=====表" + tableName + "操作结束，状态：" + insertResult + "=====");
			}
		}
	}
}
