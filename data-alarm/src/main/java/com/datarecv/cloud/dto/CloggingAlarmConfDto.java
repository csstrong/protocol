package com.datarecv.cloud.dto;

import lombok.Data;

@Data
public class CloggingAlarmConfDto {

    private double levelOne;

    private double levelTwo;

    private double alarmValue;
}
