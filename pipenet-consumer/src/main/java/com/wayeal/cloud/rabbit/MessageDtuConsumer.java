package com.wayeal.cloud.rabbit;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import com.wayeal.cloud.constant.Constant;
import com.wayeal.cloud.model.Dtu212Dto;
import com.wayeal.cloud.mongo.entity.BaseAlarmManagementDto;
import com.wayeal.cloud.mongo.entity.HeartBeatEO;
import com.wayeal.cloud.mongo.entity.MonitorData;
import com.wayeal.cloud.mongo.entity.RTMonitorData;
import com.wayeal.cloud.mongo.service.impl.MonitorDataServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author jian
 * @version 2023-04-11 13:30
 */
@Component
public class MessageDtuConsumer {

    private static final Logger log = LoggerFactory.getLogger(MessageDtuConsumer.class);

    private Map<String, String> dataTypeMap = new HashMap<>();

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private final MongoTemplate mongoTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private MonitorDataServiceImpl monitorDataServiceImpl;

    public MessageDtuConsumer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        dataTypeMap.put("2061", "data_hour");
        dataTypeMap.put("2051", "data_minute");
        dataTypeMap.put("2011", "data_realtime");
    }

    @RabbitListener(queues = "${wy.in_dtu}", containerFactory = "batchQueueRabbitListenerContainerFactory")
    @RabbitHandler
    public void process(String msgData, Channel channel, Message message) {
        try {
            boolean redisConnStatus = getRedisConnStatus();
            if(redisConnStatus){
                return;
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            //log.info("deliveryTag = {},待消费的消息是 {}", message.getMessageProperties().getDeliveryTag(), msgData);
            //存储报文数据到数据库
            processMsgToDB(msgData);
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
     * 判断redis连接状态
     */
    public boolean getRedisConnStatus() {
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        RedisConnection connection = connectionFactory.getConnection();
        boolean closed = connection.isClosed();
        return closed;
    }

    public void processMsgToDB(String msgData) throws ParseException {
        Dtu212Dto dtu212Dto = JSONObject.parseObject(msgData, Dtu212Dto.class);
        log.info("dtu message is :{}", dtu212Dto);
        String cn = dtu212Dto.getCn();
        if (cn != null && dataTypeMap.containsKey(cn)) {
            MonitorData monitorData = new MonitorData();
            monitorData.setId(dtu212Dto.getMn());
            DateFormat df1 = new SimpleDateFormat("yyyyMMddHHmmss");
            DateFormat df2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            if (dtu212Dto.getCp().containsKey("DataTime")) {
                String time = dtu212Dto.getCp().get("DataTime");
                Date date = df1.parse(time);
                monitorData.setMonitorTime(df2.format(date));
            }
            monitorData.setIp(dtu212Dto.getIp());
            LocalDateTime nowTime = LocalDateTime.now();
            monitorData.setStroageTime(nowTime.format(dateTimeFormatter));

            JSONObject jsonObject = new JSONObject();
            Map<String, String> cp = dtu212Dto.getCp();
            String dataTime = cp.get("DataTime");
            for (Map.Entry<String, String> entry : cp.entrySet()) {
                String key = entry.getKey();
                if (key.contains("Avg") || key.contains("Rtd") || key.contains("Flag")) {
                    String[] arr = key.split("-");
                    if (arr.length >= 2) {
                        if ("Flag".equals(arr[1])) {
                            jsonObject.put(key, entry.getValue());
                        } else {
                            jsonObject.put(arr[0], entry.getValue());
                        }
                    }
                }
            }
            jsonObject.put("DataTime",df2.format(df1.parse(dataTime)));
            monitorData.setComponentVal(jsonObject);

            //因为小时数据监测时间有00:00:00和00:00:01，进行数据整合到整点
            String time = monitorData.getMonitorTime();
            LocalDateTime mTime = LocalDateTime.parse(time, dateTimeFormatter);
            LocalDateTime newTime = mTime.withSecond(0);
            monitorData.setMonitorTime(newTime.format(dateTimeFormatter));

            if (dataTypeMap.get(cn).equals("data_realtime")) {
                //updateRTData(monitorData);
            }
            if (dataTypeMap.get(cn).equals("data_minute")) {
                List<Object> dataList = Lists.newArrayList(monitorData);
                monitorDataServiceImpl.insertDataToMinute(dataList);
            }
            if (dataTypeMap.get(cn).equals("data_hour")) {
                updateRTData(monitorData);
                monitorDataServiceImpl.insertDataToHour(monitorData);
                RabbitMqSendUtil.sendMessage(JSONObject.toJSON(monitorData).toString());
            }
            //更新心跳表
            updateHeartInfo(monitorData);
        }
    }

    public void updateHeartInfo(MonitorData monitorData) {
        //判断若是心跳包，存入redis,设置ttl 2min,若过期，生成一条离线报警信息。若再次产生心跳，则消警。
        if (monitorData != null) {
            LocalDateTime nowTime = LocalDateTime.now();
            HeartBeatEO heartBeatEO;
            Query query = new Query();
            String siteId = monitorData.getId();
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
            redisTemplate.opsForValue().set(siteId + "-ttl", 1, 22, TimeUnit.MINUTES);
            log.info("{}:消费成功。", siteId);
            return;
        } else {
            log.info("待消费的消息是 {}", monitorData.toString());
        }
    }

    //更新实时数据表
    private void updateRTData(MonitorData monitorData) {
        //查询data_realtime表，更新相同id的数据
        Query query = new Query();
        query.addCriteria(Criteria.where("Id").is(monitorData.getId()));
        query.addCriteria(Criteria.where("MonitorTime").lte(monitorData.getMonitorTime()));

        Update update = new Update();
        update.set(Constant.IP, monitorData.getIp());
        update.set(Constant.MONITOR_TIME, monitorData.getMonitorTime());
        update.set(Constant.STROAGE_TIME, monitorData.getStroageTime());
        update.set(Constant.COMPONENT_VAL, monitorData.getComponentVal());
        if (!this.mongoTemplate.exists(query, RTMonitorData.class)) {
            this.mongoTemplate.insert(monitorData, "data_realtime");
            return;
        }
        UpdateResult updateResult = this.mongoTemplate.updateMulti(query, update, RTMonitorData.class);
        log.info("表data_realtime操作结束，状态：" + updateResult.wasAcknowledged());
    }
}
