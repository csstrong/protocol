package com.datarecv.cloud.rule;

import com.datarecv.cloud.constant.AlarmTypeEnum;
import com.datarecv.cloud.dto.AlarmTypeDto;

public class DrainageAlarmRule   extends AbstractAlarmRule{

    public static final String format1 = "当前%s为%s，已超过%s，%s";

    private String monitoringValue;

    private AlarmTypeDto alarmTypeDto;

    public DrainageAlarmRule(AlarmTypeDto alarmTypeDto){

        this.monitoringValue=alarmTypeDto.getValue();

        this.alarmTypeDto=alarmTypeDto;
    }

    @Override
    public AlarmTypeDto doAlarm() {
        isAlarm(monitoringValue,alarmTypeDto);
        String content= getContent(alarmTypeDto);
        if (content!=null){
            alarmTypeDto.setContent(content);
        }
        return alarmTypeDto;
    }
    @Override
    public String getContent(AlarmTypeDto alarmTypeDto){
        String al = alarmTypeDto.getAlarmType();
        if (al != null) {
            //  AlarmTypeEnum a = AlarmTypeEnum.getAlarmByVale(al);
            String ruleVale ="";
            String warn = "";
            AlarmTypeEnum a= AlarmTypeEnum.getAlarmByVale(al);
            if (alarmTypeDto.isAlarm()) {
                ruleVale=alarmTypeDto.getAlarmVal();
                warn = "请及时处置！";
            } else if (alarmTypeDto.isLevelOne()) {
                ruleVale=alarmTypeDto.getLevelOneVal();
                warn = "请注意！";
            } else if (alarmTypeDto.isLevelTwo()) {
                ruleVale=alarmTypeDto.getLevelTwoVal();
                warn = "请注意！";
            }
            String val=alarmTypeDto.getValue();
            String unit=alarmTypeDto.getUnit();
            String valCon=val+unit;
            String ru=a.getName()+"("+ruleVale+unit+")";
            return String.format(format1, alarmTypeDto.getFactorName(),valCon,ru, warn);
        }
        return null;
    }
}
