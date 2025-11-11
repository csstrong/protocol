package com.datarecv.cloud.rule;

import com.datarecv.cloud.dto.AlarmTypeDto;

public interface AlarmRule {

    boolean isAlarm(String monitoringValue,String ruleValue);

    AlarmTypeDto isAlarm(String monitoringValue, AlarmTypeDto ruleValue);

    AlarmTypeDto doAlarm();

}
