package com.datarecv.cloud.redis;

import com.alibaba.fastjson.JSONObject;
import com.datarecv.cloud.constant.Constant;
import com.datarecv.cloud.mongo.dao.AlarmManagementRepository;
import com.datarecv.cloud.mongo.entity.BaseAlarmManagementDto;
import com.datarecv.cloud.mongo.entity.MonitorData;
import com.datarecv.cloud.rabbit.MessageConsumer;
import com.mongodb.lang.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

	DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	@Resource
	private MessageConsumer messageConsumer;

	@Resource
	private AlarmManagementRepository alarmManagementRepository;

	@Resource
	private MongoTemplate mongoTemplate;

	@Resource
	private PatternTopic patternTopic;

	@Resource
	private RedissonClient redissonClient;

	@Resource
	private RedisMessageListenerContainer redisMessageListenerContainer;

	@Override
	protected void doRegister(RedisMessageListenerContainer listenerContainer) {
		redisMessageListenerContainer.addMessageListener(this, patternTopic);
	}

	public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
		super(listenerContainer);
	}

	/**
	 * 针对redis数据失效事件，进行数据处理
	 *
	 * @param message
	 * @param pattern
	 */
	@Override
	public void onMessage(Message message, @Nullable byte[] pattern) {
		String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
		String key = new String(message.getBody(), StandardCharsets.UTF_8);
		String patterString = new String(pattern, StandardCharsets.UTF_8);
		log.info("key 失效：channel：{}；key：{}；patterString：{}", channel, key, patterString);

		//处理心跳包失效key事件
		if (key.contains("-ttl")) {
			int index = key.indexOf("-");
			String siteId = key.substring(0, index);
			BaseAlarmManagementDto alarmDto = new BaseAlarmManagementDto();
			LocalDateTime now = LocalDateTime.now();
			String time = now.format(dateTimeFormatter);
			alarmDto.setId(time);
			alarmDto.setContent("站点处于离线，请处理！");
			alarmDto.setCreateTime(time);
			alarmDto.setReason("心跳包自动监测报警");
			alarmDto.setStartTime(time);
			alarmDto.setType("3");
			alarmDto.setComponentId("heartbeat");
			alarmDto.setAlarmOff("0");
			alarmDto.setSiteId(siteId);
			alarmManagementRepository.save(Arrays.asList(alarmDto));

			Query query = new Query();
			Update update = new Update();
			query.addCriteria(Criteria.where(Constant.SITE_ID).is(siteId));
			update.set("UpdateTime", time);
			update.set("Status", Constant.OFFLINE);
			this.mongoTemplate.updateMulti(query, update, Constant.HEARTBEAT_INFO);
			log.info("{} 心跳报警，添加报警信息和更新心跳信息。", key);

		} else {
			RLock rLock = redissonClient.getLock("keylock");
			String keyBak = key + Constant.SUFFIX_UNION;
			try {
				if (rLock.tryLock(5, 10, TimeUnit.SECONDS)) {
					Map<String, JSONObject> map = (Map<String, JSONObject>) redisTemplate.opsForValue().get(keyBak);
					MonitorData monitorData =
						(MonitorData) redisTemplate.opsForValue().get(key + Constant.SUFFIX_CLASS);
					JSONObject configObj = messageConsumer.getConfigObj();
					JSONObject factorItem = configObj.getJSONObject("FactorItem");
					processRedisMsg(map, monitorData, factorItem);
				} else {
					log.info("排队中...");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				//插入数据后，删除key
				if (rLock.isLocked()) {
					redisTemplate.delete(keyBak);
					redisTemplate.delete(key + Constant.SUFFIX_CLASS);
					rLock.unlock();
				}
			}
		}
	}

	private void processRedisMsg(Map<String, JSONObject> timeDataMap, MonitorData monitorData, JSONObject factorItem) {
		if (timeDataMap == null || timeDataMap.size() == 0) {
			log.info("该时间段没有数据。");
			return;
		}
		List<Object> insertL = new ArrayList<>();
		//从key(分钟时间)获取整点时间,插入小时数据时使用。
		String hourTime = "";
		boolean flag = true;
		for (Map.Entry<String, JSONObject> entry : timeDataMap.entrySet()) {
			MonitorData singleData = monitorData.deepCopy(monitorData);
			singleData.setMonitorTime(entry.getKey());
			singleData.setComponentVal(entry.getValue());
			LocalDateTime t = LocalDateTime.parse(entry.getKey(), dateTimeFormatter);

			if (flag) {
				//若是只有一条整点数据，例如（15:00:00），则取15点
				if (t.getMinute() == 0 && t.getSecond() == 0) {
					hourTime = entry.getKey();
				} else {
					//若是存在其他分钟数据，例如（14:50:00 14:55:00），则需要小时加一
					LocalDateTime of = t.minusHours(-1);
					of = of.withMinute(0);
					hourTime = of.format(dateTimeFormatter);
				}
				flag = false;
			}
			insertL.add(singleData);
		}
		//存入分钟表
		messageConsumer.insertDatatoTable(insertL, "data_minute");

		//1.分钟数据因子取并集，求平均值存入小时表
		//Map<因子名称,Pair<因子值累积和,因子数量>>
		Map<String, Pair<Double, Integer>> map = new HashMap<>();

		List<String> filterFactorList = Arrays.asList("VT", "SS");
		for (Map.Entry<String, JSONObject> entry : timeDataMap.entrySet()) {

			JSONObject compVal = entry.getValue();
			for (Map.Entry<String, Object> objectEntry : compVal.entrySet()) {
				String id = objectEntry.getKey();
				String strV = (String) objectEntry.getValue();
				if ("/".equals(strV)) {
					continue;
				}
				double value = Double.parseDouble((String) objectEntry.getValue());
				if (!map.containsKey(id)) {
					map.put(id, new Pair<>(0d, 0));
				}
				Pair<Double, Integer> pair = map.get(id);
				double v1;
				int v2;
				if (filterFactorList.contains(id)) {
					//若因子是电压或信号强度，取较小的值。无需求平均值。
					Pair<Double, Integer> p = map.get(id);
					v1 = Math.min(value, p.getValue1() == 0 ? Double.MAX_VALUE : p.getValue0());
					v2 = 1;
				} else {
					v1 = value + pair.getValue0();
					v2 = 1 + pair.getValue1();
				}
				Pair<Double, Integer> newPair = new Pair<>(v1, v2);
				map.put(id, newPair);
			}
		}

		//2.求平均值,构造插入实体类
		JSONObject compVal = new JSONObject();
		for (Map.Entry<String, Pair<Double, Integer>> entry : map.entrySet()) {
			String code = entry.getKey();
			int accuracy = messageConsumer.getFactorAccuracy(factorItem, code);
			Pair<Double, Integer> pair = entry.getValue();
			double avg = new BigDecimal(pair.getValue0())
				.divide(new BigDecimal(pair.getValue1()), accuracy, BigDecimal.ROUND_HALF_UP).doubleValue();
			compVal.put(code, String.valueOf(avg));
		}
		MonitorData singleData = monitorData.deepCopy(monitorData);
		singleData.setMonitorTime(hourTime);
		singleData.setComponentVal(compVal);
		//3.存入小时表
		messageConsumer.insertDatatoTable(singleData, "data_hour");
	}
}

