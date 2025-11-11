package com.datarecv.cloud.rule;

import com.datarecv.cloud.constant.AlarmModeEnum;
import com.datarecv.cloud.dto.AlarmTypeDto;

/**
 * @des 管道报警
 */
public class PipelineAlarmRule extends AbstractAlarmRule {

    private String monitoringValue;

    private AlarmTypeDto alarmTypeDto;

    public PipelineAlarmRule(AlarmTypeDto alarmTypeDto){

        this.monitoringValue=alarmTypeDto.getValue();

        this.alarmTypeDto=alarmTypeDto;
    }


    @Override
    public AlarmTypeDto doAlarm() {
        if (AlarmModeEnum.guandaoliuliang.name().equals(alarmTypeDto.getStyle())){
            String m= calculation(monitoringValue,alarmTypeDto.getMaxQ());
            if (m!=null){
                isAlarm(m,alarmTypeDto);
            }
        }
        if (AlarmModeEnum.guandaoliusu.name().equals(alarmTypeDto.getStyle())){
            String m= calculation(monitoringValue,alarmTypeDto.getMaxV());
            if (m!=null){
                isAlarm(m,alarmTypeDto);
            }
        }
        if (AlarmModeEnum.guandaoyewei.name().equals(alarmTypeDto.getStyle())){
            String m= calculation(monitoringValue,alarmTypeDto.getMaxFull());
            if (m!=null){
                isAlarm(m,alarmTypeDto);
            }
        }
        String content= getContent(alarmTypeDto);
        if (content!=null){
            alarmTypeDto.setContent(content);
        }

        return alarmTypeDto;
    }
}
