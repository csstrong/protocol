package com.wayeal.cloud.mongo.dao;

import com.wayeal.cloud.mongo.entity.RTMonitorData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface RTMonitorDataRepository extends MongoRepository<RTMonitorData, String> {

    @Query("{'Id':?0}")
    List<RTMonitorData> findByCondition(String id);

}
