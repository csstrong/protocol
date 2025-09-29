package com.wayeal.cloud.dao;

import com.alibaba.fastjson.JSONObject;
import com.wayeal.cloud.constant.Constant;
import com.wayeal.cloud.dto.StandardDto;
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

/**
 * @author jian
 * @version 2022-08-09 17:22
 */
@Repository
@CacheConfig(cacheNames = {Constant.BASE_STANDARD_INFO})
public class StandardRepository {


    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 查询水质类型测标准项限值
     * @return
     */
    @Cacheable(key= "#root.methodName+'_'")
    public Map<String, Map<String,List<StandardDto>>> findQuality() {
        // key -> 标准项类型 : ->key-> 因子编码 : 标准项值
        Map<String,Map<String,List<StandardDto>>> map=new HashMap<>();
        Query query=new Query();
        query.addCriteria(Criteria.where(Constant.TYPE).is("quality"));
        List<JSONObject> list = mongoTemplate.find(query,JSONObject.class,Constant.BASE_STANDARD_INFO);
        for (JSONObject json : list) {
            String code = json.getString(Constant.CODE);
            //String type = json.getString(Constant.TYPE);
            String belong= json.getString("Belong");
            //
            String value=  json.getString("StandardValue");

            String level= json.getString("Level");

            String unit= json.getString("Unit");

            String name= json.getString("Name");
            if (!StringUtils.hasText(code) || !StringUtils.hasText(value)) {
                continue;
            }
            StandardDto dto=new StandardDto();
            dto.setCode(code);
            dto.setLevel(level);
            dto.setName(name);
            dto.setUnit(unit);
            dto.setValue(Double.valueOf(value));
           if (map.containsKey(belong)){
               Map<String,List<StandardDto>> map1= map.get(belong);
               List<StandardDto> list1= map1.getOrDefault(code,new ArrayList<>());
               list1.add(dto);
               map1.put(code,list1);
            }else {
               Map<String,List<StandardDto>> map1=new HashMap<>();
               List<StandardDto> list1=new ArrayList<>();
               list1.add(dto);
               map1.put(code,list1);
               map.put(belong,map1);
           }
        }
        return  map;
    }
}
