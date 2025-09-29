package com.wayeal.cloud.mongo.dao;


import com.wayeal.cloud.mongo.entity.MonitorData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/*
 * @author  chensi
 * @date  2022/7/20
 */
public interface HourMonitorDataRepository extends MongoRepository<MonitorData, String> {
    @Query("{'Id':?0,'MonitorTime':?1}")
    List<MonitorData> findByCondition(String id, String monitorTime);
}
