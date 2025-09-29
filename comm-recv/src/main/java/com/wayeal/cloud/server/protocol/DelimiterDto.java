package com.wayeal.cloud.server.protocol;

import io.netty.buffer.ByteBuf;

/**
 * @author jian
 * @version 2023-02-17 11:26
 */
public class DelimiterDto {

    private  boolean save;

    private  ByteBuf delimiter;

    private  DelimiterType delimiterType;

    private  String value;

    private  boolean separator;

    public boolean isSave() {
        return save;
    }

    public void setSave(boolean save) {
        this.save = save;
    }

    public ByteBuf getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(ByteBuf delimiter) {
        this.delimiter = delimiter;
    }

    public DelimiterType getDelimiterType() {
        return delimiterType;
    }

    public void setDelimiterType(DelimiterType delimiterType) {
        this.delimiterType = delimiterType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSeparator() {
        return separator;
    }

    public void setSeparator(boolean separator) {
        this.separator = separator;
    }

    public static enum DelimiterType{
        str,
        arr;
    }
}
