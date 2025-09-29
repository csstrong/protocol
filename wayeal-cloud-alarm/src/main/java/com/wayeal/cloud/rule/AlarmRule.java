package com.wayeal.cloud.rule;

import com.wayeal.cloud.dto.AlarmTypeDto;

/**
 * @author jian
 * @version 2022-08-06 17:06
 */
public interface AlarmRule {


    boolean isAlarm(String monitoringValue,String ruleValue);

    AlarmTypeDto isAlarm(String monitoringValue, AlarmTypeDto ruleValue);

    AlarmTypeDto doAlarm();

}
