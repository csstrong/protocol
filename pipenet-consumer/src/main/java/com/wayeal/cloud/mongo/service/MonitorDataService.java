package com.wayeal.cloud.mongo.service;

import com.wayeal.cloud.mongo.entity.MonitorData;

import java.util.List;

public interface MonitorDataService {
    /**
     * 新增
     *
     * @return String
     */
    String create();

    /**
     * 更新
     *
     * @return String
     */
    String update();

    /**
     * 删除
     *
     * @return String
     */
    String delete();

    /**
     * 查询
     *
     * @return String
     */
    List<MonitorData> select();

    /**
     * 插入单条数据
     *
     * @param o
     * @return
     */
    boolean insertDataToHour(Object o);

    /**
     * 插入一组分钟数据
     *
     * @param list
     * @return
     */
    boolean insertDataToMinute(List<Object> list);

}
