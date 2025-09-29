package com.wayeal.cloud.dao;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wayeal.cloud.constant.Constant;
import com.wayeal.cloud.constant.FactorType;
import com.wayeal.cloud.dto.FactorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.util.EnumUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ 因子管理
 */
@Repository
@CacheConfig(cacheNames = {Constant.BASE_FACTOR_INFO})
public class FactorRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Cacheable(key= "#root.methodName+'_'")
    public Map<String, FactorDto> findAll() {
        // key -> 因子编码，value is 因子 类型
        Map<String, FactorDto> map=new HashMap<>();

        LookupOperation lookupOperation=LookupOperation.newLookup()
                                                       .from(Constant.BASE_DICTIONARY_INFO)
                                                       .localField("Unit")
                                                       .foreignField("Id")
                                                       .as("UnitName");

        Aggregation aggregation = Aggregation.newAggregation(lookupOperation);
        List<JSONObject> list=  mongoTemplate.aggregate(aggregation, Constant.BASE_FACTOR_INFO,JSONObject.class).getMappedResults();
       // List<JSONObject> list = mongoTemplate.findAll(JSONObject.class, Constant.BASE_FACTOR_INFO);
        for (JSONObject json : list) {
            FactorDto factorDto=new FactorDto();
            String id = json.getString(Constant.ID);
            String type =json.getString(Constant.TYPE);
            String name =json.getString(Constant.NAME);
            JSONArray unitNameArr =json.getJSONArray("UnitName");
            FactorType factorType=FactorType.valueOf(type);
            factorDto.setFactorType(factorType);
            factorDto.setName(name);
            if (unitNameArr!=null && unitNameArr.size()>0){
                JSONObject j= unitNameArr.getJSONObject(0);
                String unitName=  j.getString(Constant.NAME);
                factorDto.setUnit(unitName);
            }
            if (!StringUtils.hasText(id)) {
                continue;
            }
           map.put(id,factorDto);
        }
        return  map;
    }

}
