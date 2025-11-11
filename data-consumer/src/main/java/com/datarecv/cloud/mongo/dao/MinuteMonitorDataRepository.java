package com.datarecv.cloud.mongo.dao;

import com.datarecv.cloud.mongo.entity.MinuteMonitorData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface MinuteMonitorDataRepository extends MongoRepository<MinuteMonitorData, String> {

    @Query("{'Id':?0,'MonitorTime':?1}")
    List<MinuteMonitorData> findByCondition(String id, String monitorTime);

    //@Query 使用jpql方式查询
    @Query("{'Ip':?0,'MonitorTime':?1}")
    List<MinuteMonitorData> findByJpql(String ip, String monitorTime);

    @Query("{'ComponentVal.38-电源电压':?0}")
    List<MinuteMonitorData> findByJpql2(String value);

    //模糊查询
    List<MinuteMonitorData> findByIdLike(String id);

}
