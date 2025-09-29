package com.wayeal.cloud.rule;

import com.wayeal.cloud.dto.AlarmTypeDto;

public interface AlarmRule {


    boolean isAlarm(String monitoringValue,String ruleValue);

    AlarmTypeDto isAlarm(String monitoringValue, AlarmTypeDto ruleValue);

    AlarmTypeDto doAlarm();

}
