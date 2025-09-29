package com.wayeal.cloud.dao;

import com.alibaba.fastjson.JSONObject;
import com.wayeal.cloud.constant.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@CacheConfig(cacheNames = {Constant.BASE_EQUIPMENT_INFO})
public class EquipmentRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Cacheable(key= "#root.methodName+'_'+#id")
    public JSONObject findById(String id) {
       JSONObject factorJson=new JSONObject();
        Query query = new Query();
        query.addCriteria(Criteria.where(Constant.SITE_ID).is(id));
        List<JSONObject> list = mongoTemplate.find(query,JSONObject.class, Constant.BASE_EQUIPMENT_INFO);
        for (JSONObject json : list) {
            String siteId = json.getString(Constant.SITE_ID);
            if (!StringUtils.hasText(siteId)) {
                continue;
            }
            JSONObject js = json.getJSONObject(Constant.FACTOR);
            factorJson.putAll(js);
        }
        return  factorJson;
    }
}
