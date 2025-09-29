package com.wayeal.cloud.dto;

import com.wayeal.cloud.constant.SubsystemEnum;
import com.wayeal.cloud.utils.silt_height_calculation.Pipeline;
import lombok.Data;

@Data
public class AlarmTypeDto {

    private String siteId;

    private String siteName;

    private String factor;

    private String factorName;

    private String unit;

    private String value;

    private String time;

    private SubsystemEnum subsystemEnum;

    private String content;
    /**
     * 因子报警类型
     */
    private String style;

    /**
     * max 最大流速
     */
    private String maxV;
    /**
     * max 最大流量
     */
    private String maxQ;
    /**
     * max full最大充满度
     */
    private String maxFull;

    private String wellDepth;

    private String alarmType;

    private boolean levelOne=false;

    private boolean levelTwo=false;

    private boolean alarm=false;

    private String  levelOneVal;

    private String  levelTwoVal;

    private String  alarmVal;

    private String  wellType;

    private Pipeline pipeline;

    private String gisPipeType;

    private String gisPipeCode;

    private CloggingAlarmConfDto cloggingAlarmConfDto;

}
