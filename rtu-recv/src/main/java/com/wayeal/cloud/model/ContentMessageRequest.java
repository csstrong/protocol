package com.wayeal.cloud.model;

import java.util.List;
import java.util.Map;

/**
 * @正文数据
 */
public class ContentMessageRequest {

    /**
     * 序列号
     */
    private String serialNumber;
    /**
     * 发送时间
     */
    private String sendingTime;

    /**
     * 遥测站地址
     */
    private String stationCode;
    /**
     * 遥测站分类码
     */
    private String stationClassificationCode;
    /**
     * 要素组
     */
    private List<ElementResult>  elementResultS;

    private Map<String,String>   commonFactor;


    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSendingTime() {
        return sendingTime;
    }

    public void setSendingTime(String sendingTime) {
        this.sendingTime = sendingTime;
    }

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    public String getStationClassificationCode() {
        return stationClassificationCode;
    }

    public void setStationClassificationCode(String stationClassificationCode) {
        this.stationClassificationCode = stationClassificationCode;
    }

    public List<ElementResult> getElementResultS() {
        return elementResultS;
    }

    public void setElementResultS(List<ElementResult> elementResultS) {
        this.elementResultS = elementResultS;
    }

    public Map<String, String> getCommonFactor() {
        return commonFactor;
    }

    public void setCommonFactor(Map<String, String> commonFactor) {
        this.commonFactor = commonFactor;
    }
}
