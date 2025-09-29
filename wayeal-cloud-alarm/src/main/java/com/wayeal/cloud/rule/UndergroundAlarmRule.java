package com.wayeal.cloud.rule;

import com.wayeal.cloud.constant.AlarmModeEnum;
import com.wayeal.cloud.constant.AlarmTypeEnum;
import com.wayeal.cloud.dto.AlarmTypeDto;

/**
 * @des 井下报警
 * @author jian
 * @version 2022-08-09 14:36
 */
public class UndergroundAlarmRule extends AbstractAlarmRule {

    private String monitoringValue;

    private AlarmTypeDto alarmTypeDto;

    public UndergroundAlarmRule(AlarmTypeDto alarmTypeDto){

        this.monitoringValue=alarmTypeDto.getValue();

        this.alarmTypeDto=alarmTypeDto;
    }

    @Override
    public AlarmTypeDto doAlarm() {
        if (AlarmModeEnum.yijingyewei.name().equals(alarmTypeDto.getStyle())){
            String m= calculation(monitoringValue,alarmTypeDto.getWellDepth());
            if (m!=null){
                isAlarm(m,alarmTypeDto);
            }
        }
        return alarmTypeDto;
    }

}
