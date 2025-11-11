package com.datarecv.cloud.server.protocol;

import com.datarecv.cloud.enums.ProtocolType;

public class ProtocolDto {

    /** 分割符 */
    private String delimiter;
    /** 开始位置 */
    private int startIndex;
    /** 长度 */
    private int length;

    private boolean start;

    private boolean hex;

    private ProtocolType protocolType;

    private ProtocolAnalysisInterface protocolAnalysisInterface;

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public boolean isHex() {
        return hex;
    }

    public void setHex(boolean hex) {
        this.hex = hex;
    }

    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType=ProtocolType.valueOf(protocolType);
    }

    public void setProtocolType1(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    public ProtocolAnalysisInterface getProtocolAnalysisInterface() {
        return protocolAnalysisInterface;
    }

    public void setProtocolAnalysisInterface(String protocolAnalysisInterface) {
        try {
            Class clazz =  Class.forName(protocolAnalysisInterface);
            this.protocolAnalysisInterface= (ProtocolAnalysisInterface) clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void setProtocolAnalysisInterface1(ProtocolAnalysisInterface protocolAnalysisInterface) {
        this.protocolAnalysisInterface = protocolAnalysisInterface;
    }
}
