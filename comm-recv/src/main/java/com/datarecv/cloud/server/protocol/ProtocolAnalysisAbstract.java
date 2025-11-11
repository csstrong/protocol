package com.datarecv.cloud.server.protocol;

import com.datarecv.cloud.enums.ProtocolType;

public  abstract class ProtocolAnalysisAbstract implements ProtocolAnalysisInterface {

    protected String delimiter;

    protected ProtocolType protocolType;

    @Override
    public String getDelimiter() {
        return this.delimiter;
    }

    @Override
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public ProtocolType getProtocolType() {
        return protocolType;
    }

    @Override
    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

}
