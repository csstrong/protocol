package com.datarecv.cloud.mongo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.datarecv.cloud.constant.Constant;
import com.datarecv.cloud.mongo.dao.HourMonitorDataRepository;
import com.datarecv.cloud.mongo.dao.MinuteMonitorDataRepository;
import com.datarecv.cloud.mongo.entity.MinuteMonitorData;
import com.datarecv.cloud.mongo.entity.MonitorData;
import com.datarecv.cloud.mongo.entity.RTMonitorData;
import com.datarecv.cloud.mongo.service.MonitorDataService;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MonitorDataServiceImpl implements MonitorDataService {

    @Resource
    private HourMonitorDataRepository hourMonitorDataRepository;

    @Resource
    private MinuteMonitorDataRepository minuteMonitorDataRepository;

    @Resource
    private MongoTemplate mongoTemplate;

    ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public String create() {
        return null;
    }

    @Override
    public String update() {
        return null;
    }

    @Override
    public String delete() {
        return null;
    }

    @Override
    public List<MonitorData> select() {
        return null;
    }

    @Override
    public boolean insertDataToHour(Object o) {
        boolean res = false;

        Future future = executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MonitorData data = (MonitorData) o;
                List<MonitorData> queryRes = hourMonitorDataRepository
                    .findByCondition(data.getId(), data.getMonitorTime());
                if (!queryRes.isEmpty()) {
                    MonitorData existData = queryRes.stream().findFirst().get();
                    //相同时间监测时间的数据存在，则数据合并
                    log.info("[data_hour]=> (Id: {} , MonitorTime: {}) is exists!", existData.getId(), existData.getMonitorTime());

                    JSONObject srcJson = data.getComponentVal();
                    JSONObject existJson = existData.getComponentVal();
                    existJson.putAll(srcJson);
                    existData.setMonitorTime(data.getMonitorTime());
                    existData.setStroageTime(data.getStroageTime());
                    existData.setComponentVal(existJson);

                    hourMonitorDataRepository.save(existData);
                    updateRTData(existData);
                    return null;
                }
                updateRTData(data);
                hourMonitorDataRepository.save(data);
                return null;
            }
        });
        try {
            future.get(10, TimeUnit.SECONDS);
            boolean flag = true;
            while (flag) {
                res = future.isDone();
                Thread.sleep(10);
                if (res) {
                    flag = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public boolean insertDataToMinute(List<Object> list) {
        List<MinuteMonitorData> minuteDataList = list.stream().map(m -> {
            MonitorData md = (MonitorData) m;
            MinuteMonitorData o = MinuteMonitorData.builder().id(md.getId())
                .ip(md.getIp()).monitorTime(md.getMonitorTime())
                .stroageTime(md.getStroageTime())
                .componentVal(md.getComponentVal()).build();
            return o;
        }).collect(Collectors.toList());

        Future future = null;
        boolean res = false;
        for (MinuteMonitorData data : minuteDataList) {
            future = executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    List<MinuteMonitorData> queryRes = minuteMonitorDataRepository
                        .findByCondition(data.getId(), data.getMonitorTime());
                    if (!queryRes.isEmpty()) {
                        MinuteMonitorData existData = queryRes.stream().findFirst().get();
                        log.info("[data_minute]=> (Id: {} , MonitorTime: {}) is exists!", existData.getId(), existData.getMonitorTime());

                        JSONObject srcJson = data.getComponentVal();
                        JSONObject existJson = existData.getComponentVal();
                        existJson.putAll(srcJson);
                        existData.setMonitorTime(data.getMonitorTime());
                        existData.setStroageTime(data.getStroageTime());
                        existData.setComponentVal(existJson);
                        //更新
                        minuteMonitorDataRepository.save(existData);
                        return null;
                    }
                    minuteMonitorDataRepository.save(data);
                    return null;
                }
            });
            try {
                future.get(10, TimeUnit.SECONDS);
                boolean flag = true;
                while (flag) {
                    res = future.isDone();
                    Thread.sleep(10);
                    if (res) {
                        flag = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
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
        UpdateResult updateResult = this.mongoTemplate.updateMulti(query, update, RTMonitorData.class);
        log.info("表data_realtime操作结束，状态：" + updateResult.wasAcknowledged());
    }
}
