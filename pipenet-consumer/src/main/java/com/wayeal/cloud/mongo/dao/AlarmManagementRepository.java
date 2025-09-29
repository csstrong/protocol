package com.wayeal.cloud.mongo.dao;

import com.wayeal.cloud.constant.Constant;
import com.wayeal.cloud.mongo.entity.BaseAlarmManagementDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AlarmManagementRepository {

    @Autowired
    private MongoTemplate mongoTemplate;


    public Map<String, BaseAlarmManagementDto> findById(String id) {
        Map<String, BaseAlarmManagementDto> map = new HashMap<>();
        Query query = new Query();
        query.addCriteria(Criteria.where(Constant.SITE_ID).is(id));
        query.addCriteria(Criteria.where(Constant.ALARM_OFF).is("0"));
        List<BaseAlarmManagementDto> list = mongoTemplate.find(query, BaseAlarmManagementDto.class, Constant.BASE_ALARM_MANAGEMENT);

        for (BaseAlarmManagementDto baseAlarmManagementDto : list) {
            String key = baseAlarmManagementDto.getComponentId();
            map.put(key, baseAlarmManagementDto);
        }
        return map;
    }

    public void save(List<BaseAlarmManagementDto> list) {
        List<String> ids = new ArrayList<>();
        for (BaseAlarmManagementDto baseAlarmManagementDto : list) {
            String id = baseAlarmManagementDto.getId();
            ids.add(id);
        }
        Query query = new Query();
        query.addCriteria(Criteria.where(Constant.ALARM_OFF).is("1"));
        query.addCriteria(Criteria.where(Constant.ID).in(ids));

        Map<String, BaseAlarmManagementDto> map = new HashMap<>();
        List<BaseAlarmManagementDto> bList = mongoTemplate.find(query, BaseAlarmManagementDto.class, Constant.BASE_ALARM_MANAGEMENT);
        //
        if (bList.size() > 0) {
            for (BaseAlarmManagementDto baseAlarmManagementDto : bList) {
                map.put(baseAlarmManagementDto.getId(), baseAlarmManagementDto);
            }
        }
        List<BaseAlarmManagementDto> cList = new ArrayList<>();
        for (BaseAlarmManagementDto baseAlarmManagementDto : list) {
            String id = baseAlarmManagementDto.getId();
            if (!map.containsKey(id)) {
                cList.add(baseAlarmManagementDto);
            }
        }
        if (cList.size() > 0) {
            mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, BaseAlarmManagementDto.class).insert(list).execute();
        }

    }

    public void update(List<BaseAlarmManagementDto> list) {
        for (BaseAlarmManagementDto baseAlarmManagementDto : list) {
            Update update = new Update();
            update.set("AlarmOff", baseAlarmManagementDto.getAlarmOff());
            update.set("Duration", baseAlarmManagementDto.getDuration());
            update.set("EndTime", baseAlarmManagementDto.getEndTime());
            update.set("UpdateTime", baseAlarmManagementDto.getUpdateTime());
            Query query = Query.query(Criteria.where("Id").is(baseAlarmManagementDto.getId()));
            this.mongoTemplate.updateFirst(query, update, Constant.BASE_ALARM_MANAGEMENT);
        }

    }
}
