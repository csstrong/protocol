package com.wayeal.cloud.dao;

import com.alibaba.fastjson.JSONObject;
import com.wayeal.cloud.constant.Constant;
import com.wayeal.cloud.dto.CloggingAlarmConfDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@CacheConfig(cacheNames = {Constant.BASE_CLOGGING_ALARM_CONF})
public class CloggingAlarmConfRepository {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Cacheable(key= "#root.methodName+'_'")
    public CloggingAlarmConfDto findAll(){
        List<JSONObject> listJson = mongoTemplate.findAll(JSONObject.class, Constant.BASE_CLOGGING_ALARM_CONF);
        if (listJson.size()>0){
            JSONObject jsonObject=listJson.get(0);
            CloggingAlarmConfDto cloggingAlarmConfDto=new CloggingAlarmConfDto();
            cloggingAlarmConfDto.setLevelOne(jsonObject.getDouble("LevelOne"));
            cloggingAlarmConfDto.setLevelTwo(jsonObject.getDouble("LevelTwo"));
            cloggingAlarmConfDto.setAlarmValue(jsonObject.getDouble("AlarmValue"));
            return  cloggingAlarmConfDto;
        }
        return  null;
    }
}
