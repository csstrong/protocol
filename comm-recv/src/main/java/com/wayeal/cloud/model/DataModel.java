package com.wayeal.cloud.model;

import com.wayeal.cloud.enums.ProtocolType;

/**
 * @author jian
 * @version 2023-02-15 16:09
 */
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
