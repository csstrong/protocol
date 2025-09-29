package com.wayeal.cloud.model;

/**
 * @des
 * @author jian
 * @version 2022-07-08 17:29
 */
public class MessageRequest {

    protected   String centerAddress;

    protected   String telemetryStationAddress;

    protected   String password;

    protected   String functionCode;

    protected   String identificationAndLength;

    protected   String startCharacter;

    /**
     *  M3模式下才有占用3个字节
     */
    protected   String packageTotalAndNo;
    /**
     * 包长度
     */
    protected   int length;
    /**
     * 上下行标识
     */
    protected   int  upAndDownIdentification;
    /**
     * 包总数
     */
    protected   Integer packageTotal;
    /**
     * 序列号
     */
    protected   Integer packageNo;

    public String getCenterAddress() {
        return centerAddress;
    }

    public void setCenterAddress(String centerAddress) {
        this.centerAddress = centerAddress;
    }

    public String getTelemetryStationAddress() {
        return telemetryStationAddress;
    }

    public void setTelemetryStationAddress(String telemetryStationAddress) {
        this.telemetryStationAddress = telemetryStationAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(String functionCode) {
        this.functionCode = functionCode;
    }

    public String getIdentificationAndLength() {
        return identificationAndLength;
    }

    public void setIdentificationAndLength(String identificationAndLength) {
        this.identificationAndLength = identificationAndLength;
    }

    public String getStartCharacter() {
        return startCharacter;
    }

    public void setStartCharacter(String startCharacter) {
        this.startCharacter = startCharacter;
    }


    public String getPackageTotalAndNo() {
        return packageTotalAndNo;
    }

    public void setPackageTotalAndNo(String packageTotalAndNo) {
        this.packageTotalAndNo = packageTotalAndNo;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getUpAndDownIdentification() {
        return upAndDownIdentification;
    }

    public void setUpAndDownIdentification(int upAndDownIdentification) {
        this.upAndDownIdentification = upAndDownIdentification;
    }

    public Integer getPackageTotal() {
        return packageTotal;
    }

    public void setPackageTotal(Integer packageTotal) {
        this.packageTotal = packageTotal;
    }

    public Integer getPackageNo() {
        return packageNo;
    }

    public void setPackageNo(Integer packageNo) {
        this.packageNo = packageNo;
    }
}
