package com.datarecv.cloud.model;

import com.datarecv.cloud.enums.ProtocolType;

public class DataModel {

    private String data;

    private ProtocolType protocolType;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }
}
