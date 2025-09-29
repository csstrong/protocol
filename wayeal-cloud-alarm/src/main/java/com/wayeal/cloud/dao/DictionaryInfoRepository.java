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

import java.util.List;

@Repository
@CacheConfig(cacheNames = {Constant.BASE_DICTIONARY_INFO})
public class DictionaryInfoRepository {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Cacheable(key= "#root.methodName+'_'+#id")
    public JSONObject findById(String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Constant.ID).is(id));
        List<JSONObject> list = mongoTemplate.find(query,JSONObject.class, Constant.BASE_DICTIONARY_INFO);
        if (list.size()>0){
            return  list.get(0);
        }
        return  null;
    }

}
