package com.datarecv.cloud.utils.silt_height_calculation;

public enum SiltState {
    ABNORMAL, // 异常 (流量和流速偏大)
    NORMAL, // 正常淤堵
    SERIOUS // 严重淤堵 (流速低于阈值)
}
