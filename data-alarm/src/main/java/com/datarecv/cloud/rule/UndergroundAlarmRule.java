package com.datarecv.cloud.rule;

import com.datarecv.cloud.constant.AlarmModeEnum;
import com.datarecv.cloud.dto.AlarmTypeDto;

/**
 * @des 井下报警
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
