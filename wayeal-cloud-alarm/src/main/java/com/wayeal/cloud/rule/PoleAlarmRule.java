package com.wayeal.cloud.rule;

import com.wayeal.cloud.constant.AlarmModeEnum;
import com.wayeal.cloud.constant.AlarmTypeEnum;
import com.wayeal.cloud.dto.AlarmTypeDto;

/**
 * @des 立杆报警规则
 */
public class PoleAlarmRule extends AbstractAlarmRule {

    public static final String format= "当前%s为%s,已%s值（%s），%s";

    private String monitoringValue;

    private AlarmTypeDto alarmTypeDto;
    /**
     * 是否是降序比较，默认为false,就是值越低报警等级越高
     */
    private boolean isDes=false;

    public PoleAlarmRule(AlarmTypeDto alarmTypeDto){

        this.monitoringValue=alarmTypeDto.getValue();

        this.alarmTypeDto=alarmTypeDto;
    }

    public PoleAlarmRule(AlarmTypeDto alarmTypeDto,boolean isDes){

        this.monitoringValue=alarmTypeDto.getValue();

        this.alarmTypeDto=alarmTypeDto;

        this.isDes=isDes;
    }

    @Override
    public AlarmTypeDto doAlarm() {
       isAlarm(monitoringValue,alarmTypeDto,isDes);
       String content= getContent(alarmTypeDto);
       if (content!=null){
           alarmTypeDto.setContent(content);
       }
       return alarmTypeDto;
    }
    @Override
    public String getContent(AlarmTypeDto alarmTypeDto){
        String al=alarmTypeDto.getAlarmType();
        if (al!=null){
            AlarmTypeEnum a= AlarmTypeEnum.getAlarmByVale(al);
            String ruleVale="";
            String state="超过";
            String warn="";
            String unit="";
            String value= alarmTypeDto.getValue();
            double d=Double.parseDouble(value);
            if (alarmTypeDto.isAlarm()){
                ruleVale=alarmTypeDto.getAlarmVal();
                warn="建议禁止通行，请及时处置！";
            }else if (alarmTypeDto.isLevelOne()){
                ruleVale=alarmTypeDto.getLevelOneVal();
                warn="请谨慎通行！";
            }else if (alarmTypeDto.isLevelTwo()){
                ruleVale=alarmTypeDto.getLevelTwoVal();
                warn="请减速慢行！";
            }
            if (AlarmModeEnum.liganliuliang.name().equals(alarmTypeDto.getStyle())){
                unit="m³/s";
            }
            if (AlarmModeEnum.liganliusu.name().equals(alarmTypeDto.getStyle())){
                unit="m/s";
            }
            if (AlarmModeEnum.liganshuiwei.name().equals(alarmTypeDto.getStyle())){
                unit="cm";
            }
            if (AlarmModeEnum.didianya.name().equals(alarmTypeDto.getStyle())){
                unit="V";
                state="低于";
                warn="请及时处置！";
            }
            ruleVale=ruleVale+unit;
            return String.format(format,alarmTypeDto.getFactorName(),String.format("%.2f",d)+unit,state+a.getName(),ruleVale,warn);
        }
        return null;
    }
}
