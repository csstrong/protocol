package com.datarecv.cloud.dto;

import com.alibaba.fastjson.JSONObject;
import com.datarecv.cloud.utils.silt_height_calculation.Pipeline;
import com.datarecv.cloud.constant.MonitoringType;
import com.datarecv.cloud.constant.SubsystemEnum;
import lombok.Data;

/**
 * @des  事实
 */
@Data
public class RecvDataDto {
    /**
     * 物联网子系统
     */
    private SubsystemEnum subsystemEnum;
    /**
     * 站点id
     */
    private String siteId;

    private String siteName;
    /**
     * 监测值
     */
    private JSONObject componentVal;
    /**
     * 监测测类型
     */
    private MonitoringType monitoringType;
    /**
     * 水质标准，只有subsystemEnum pipeEnvironment数据具有
     */
    private String waterClass;

    private String maxQ;

    private String maxV;

    private String wellType;

    /**
     * max full最大充满度(m)
     */
    private String maxFull;
    /**
     * 井深(m)
     */
    private String wellDepth;

    private String time;

    private String gisPipeType;

    private String gisPipeCode;

    private Pipeline pipeline;
}
