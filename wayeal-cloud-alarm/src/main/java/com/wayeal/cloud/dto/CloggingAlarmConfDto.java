package com.wayeal.cloud.dto;

import lombok.Data;

/**
 * @author jian
 * @version 2022-10-28 15:22
 */
@Data
public class CloggingAlarmConfDto {

    private double levelOne;

    private double levelTwo;

    private double alarmValue;
}
