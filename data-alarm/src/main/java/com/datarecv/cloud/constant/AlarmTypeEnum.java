package com.datarecv.cloud.constant;

public enum AlarmTypeEnum {

    ONE_ALARM("0","一级预警"),
    TWO_ALARM("1","二级预警"),
    ALARM("2","报警");

    private String value;

    private String name;

    AlarmTypeEnum(String value, String name){
        this.value=value;
        this.name=name;
    }

    public static AlarmTypeEnum getAlarmByVale(String value){
        AlarmTypeEnum alarmEnum=null;
        switch (value){
            case "0" :alarmEnum=ONE_ALARM;break;
            case "1" :alarmEnum=TWO_ALARM;break;
            case "2" :alarmEnum=ALARM;break;
            default: break;
        }
        return alarmEnum;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
