package com.wayeal.cloud.dto;

import com.wayeal.cloud.utils.silt_height_calculation.Pipeline;
import lombok.Data;

@Data
public class PipelineDto {

    private String siteId;

    private String siteName;

    private String time;

    /**
     * 流量
     */
    private String flow;
    /**
     * 流速
     */
    private String speed;
    /**
     * 水位
     */
    private String waterLevel;

    private String wellType;

    private Pipeline pipeline;

    private String gisPipeType;

    private String gisPipeCode;

    private CloggingAlarmConfDto cloggingAlarmConfDto;

}
