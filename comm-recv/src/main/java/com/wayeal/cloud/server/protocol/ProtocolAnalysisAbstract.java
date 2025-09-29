package com.wayeal.cloud.server.protocol;

import com.wayeal.cloud.enums.ProtocolType;

/**
 * @author jian
 * @version 2023-03-16 9:48
 */
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
