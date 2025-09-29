package com.wayeal.cloud.rule;

import com.wayeal.cloud.constant.AlarmTypeEnum;
import com.wayeal.cloud.dto.AlarmTypeDto;
import org.springframework.util.StringUtils;

public abstract class AbstractAlarmRule implements AlarmRule {

    public static final String format = "当前%s为%s，已超过最大设计%s的%s，%s";

    public boolean isAlarm(String monitoringValue, String ruleValue) {

        if (!StringUtils.hasText(monitoringValue)) {
            return false;
        }
        if (!StringUtils.hasText(ruleValue)) {
            return false;
        }
        double val = Double.parseDouble(monitoringValue);
        double ruleVal = Double.parseDouble(ruleValue);
        if (val > ruleVal) {
            return true;
        }
        return false;
    }

    public AlarmTypeDto isAlarm(String monitoringValue, AlarmTypeDto ruleValue) {
        boolean flag;
        flag = isAlarm(monitoringValue, ruleValue.getAlarmVal());
        if (flag) {
            ruleValue.setAlarm(true);
            ruleValue.setAlarmType("2");
            return ruleValue;
        }
        flag = isAlarm(monitoringValue, ruleValue.getLevelTwoVal());
        if (flag) {
            ruleValue.setLevelTwo(true);
            ruleValue.setAlarmType("1");
            return ruleValue;
        }
        flag = isAlarm(monitoringValue, ruleValue.getLevelOneVal());
        if (flag) {
            ruleValue.setLevelOne(true);
            ruleValue.setAlarmType("0");
            return ruleValue;
        }
        return ruleValue;
    }

    public AlarmTypeDto isAlarm(String monitoringValue, AlarmTypeDto ruleValue, boolean isDes) {
        if (!isDes) {
            return isAlarm(monitoringValue, ruleValue);
        }
        boolean flag;
        flag = isAlarm(ruleValue.getAlarmVal(), monitoringValue);
        if (flag) {
            ruleValue.setAlarm(true);
            ruleValue.setAlarmType("2");
            return ruleValue;
        }
        flag = isAlarm(ruleValue.getLevelTwoVal(), monitoringValue);
        if (flag) {
            ruleValue.setLevelTwo(true);
            ruleValue.setAlarmType("1");
            return ruleValue;
        }
        flag = isAlarm(ruleValue.getLevelOneVal(), monitoringValue);
        if (flag) {
            ruleValue.setLevelOne(true);
            ruleValue.setAlarmType("0");
            return ruleValue;
        }
        return ruleValue;
    }

    public String calculation(String monitoringValue, String setValue) {
        if (setValue != null && monitoringValue != null) {
            double q = Double.parseDouble(setValue);
            double m = Double.parseDouble(monitoringValue);
            double v = m / q;
            return String.valueOf(v);
        }
        return null;
    }

    public String getContent(AlarmTypeDto alarmTypeDto) {
        String al = alarmTypeDto.getAlarmType();
        if (al != null) {
          //  AlarmTypeEnum a = AlarmTypeEnum.getAlarmByVale(al);
            String ruleVale = "";
            String warn = "";
            if (alarmTypeDto.isAlarm()) {
                double d = Double.parseDouble(alarmTypeDto.getAlarmVal());
                ruleVale =String.format("%.2f",d*100)+"%";;
                warn = "请及时处置！";
            } else if (alarmTypeDto.isLevelOne()) {
                double d = Double.parseDouble(alarmTypeDto.getLevelOneVal());
                ruleVale =String.format("%.2f",d*100)+"%";;
                warn = "请注意！";
            } else if (alarmTypeDto.isLevelTwo()) {
                double d = Double.parseDouble(alarmTypeDto.getLevelTwoVal());
                ruleVale =String.format("%.2f",d*100)+"%";;
                warn = "请注意！";
            }
            String val=alarmTypeDto.getValue();
            String unit=alarmTypeDto.getUnit();
            if ("m".equals(unit)){
                unit="cm";
                double d = Double.parseDouble(val)*100;
                val=String.valueOf(d);
            }
            String valCon=val+unit;
            return String.format(format, alarmTypeDto.getFactorName(),valCon, alarmTypeDto.getFactorName(), ruleVale, warn);
        }
        return null;
    }
}
