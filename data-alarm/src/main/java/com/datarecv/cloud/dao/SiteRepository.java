package com.datarecv.cloud.dao;

import com.alibaba.fastjson.JSONObject;
import com.datarecv.cloud.constant.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@CacheConfig(cacheNames = {Constant.BASE_SITE_INFO})
public class SiteRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Cacheable(key= "#root.methodName+'_'+#siteId")
    public JSONObject getSiteInfoById(String siteId) {

        Query query = new Query();

        query.addCriteria(Criteria.where(Constant.ID).is(siteId));

        JSONObject siteJson = mongoTemplate.findOne(query, JSONObject.class, Constant.BASE_SITE_INFO);

        return siteJson;
    }

    @Cacheable(key= "#root.methodName+'_'+'all'")
    public Map<String,JSONObject> getAllSiteInfo(){
        Map<String,JSONObject> res=new LinkedHashMap<>();
        List<JSONObject> listJson = mongoTemplate.findAll(JSONObject.class, Constant.BASE_SITE_INFO);
        Map<String,JSONObject> station=new LinkedHashMap<>();
        Map<String,JSONObject> drainageHousehold=new LinkedHashMap<>();
        for (JSONObject json:listJson){
            String id=json.getString("Id");
            String subsystem=json.getString("Subsystem");
            if ("sewagePlant".equals(subsystem)){
                station.put(id,json);
            }else {
                res.put(id,json);
            }
        }

        List<JSONObject> listJson1 = mongoTemplate.findAll(JSONObject.class, Constant.DRAINAGEHOUSEHOLD_MANAGER_INFO);
        for (JSONObject json:listJson1){
            String id=json.getString("Id");
            drainageHousehold.put(id,json);
        }

        List<JSONObject> listJson2 = mongoTemplate.findAll(JSONObject.class, Constant.BASE_STATION_INFO);
        for (JSONObject json:listJson2){
            String id=json.getString("Id");
            String stationType=json.getString("StationType");
            //厂站类型
            if ("factorysite".equals(stationType)){
                String siteId=json.getString("SiteId");
                if (station.containsKey(siteId)){
                    JSONObject jsonObject= station.get(siteId);
                    JSONObject j=new JSONObject();
                    j.putAll(jsonObject);
                    j.put("WaterClass",json.getString("WaterClass"));
                    j.put("MaxFull",json.getString("MaxFull"));
                    j.put("MaxQ",json.getString("MaxQ"));
                    j.put("MaxV",json.getString("MaxV"));
                    res.put(id,j);
                }
            }
            //排水户类型
            if ("drainagesite".equals(stationType)){
                String siteId=json.getString("SiteId");
                if (drainageHousehold.containsKey(siteId)){
                    JSONObject jsonObject= drainageHousehold.get(siteId);
                    jsonObject.put("WaterClass",json.getString("WaterClass"));
                    res.put(id,jsonObject);
                }
            }
        }
        return res;
    }


}
