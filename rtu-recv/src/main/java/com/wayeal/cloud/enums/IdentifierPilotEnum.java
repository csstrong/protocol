package com.wayeal.cloud.enums;

/**
 * @des 标识符引导符 enum
 * @author jian
 * @version 2022-07-07 14:40
 */
public enum IdentifierPilotEnum {

    F0( "F0",  "观测时间引导符", 5, 0,false,false),
    F1( "F1",  "测站编码引导符", 6, 0,false,false),
    F4( "F4",  "1小时内每5分钟时段雨量", 1, 0,true,false),
    F5( "F5",  "1小时内5分钟间隔相对水位", 2, 0,true,false),
    F6( "F6",  "1小时内5分钟间隔相对水位2;对于闸坝（泵）站表示闸（站）下水位", 2, 0,true,false),
    F9( "F9",  "1小时内5分钟间隔相对水位5", 2, 3,true,false),
    _03("03", "瞬时水温", 2, 1,false,false),
    _04("04", "时间步长码", 3, 0,false,false),
    _48("48", "电导率", 3, 0,false,false),
    _4A("4A", "高锰酸盐指数", 2, 1,false,false),
    _4C("4C", "氨氮", 3, 2,false,false),

    // 自定义标识符
    _79("79", "瞬时流量", 5, 0,false,false),
    _7D("7D", "累计流量", 5, 0,false,false),

    // 公共因子
    _98("98", "信号", 1, 0,false,true),
    _38("38", "电压", 2, 2,false,true),

    B0( "B0",  "1小时内时段瞬时流量1", 5, 3,false,false),
    B1( "B1",  "1小时内时段瞬时流量2", 5, 3,false,false),
    B9( "B9",  "1小时内累计流量1", 5, 0,false,false),
    BA( "BA",  "1小时内累积流量2", 5, 0,false,false),
    A6( "A6",  "1小时内流速1", 2, 0,true,false),
    A7( "A7",  "1小时内流速2", 2, 0,false,false);

    public static IdentifierPilotEnum getDataByValue(String value) {
        IdentifierPilotEnum e;
        switch (value) {
            case "F0":
                e = F0;
                break;
            case "F1":
                e = F1;
                break;
            case "F4":
                e = F4;
                break;
            case "F5":
                e = F5;
                break;
            case "F6":
                e = F6;
                break;
            case "F9":
                e = F9;
                break;
            case "03":
                e = _03;
                break;
            case "04":
                e = _04;
                break;
            case "48":
                e = _48;
                break;
            case "79":
                e = _79;
                break;
            case "7D":
                e = _7D;
                break;
            case "B9":
                e = B9;
                break;
            case "B0":
                e = B0;
                break;
            case "A6":
                e = A6;
                break;
            case "B1":
                e = B1;
                break;
            case "BA":
                e = BA;
                break;
            case "A7":
                e = A7;
                break;
            case "98":
                e = _98;
                break;
            case "38":
                e = _38;
                break;
            case "4A":
                e = _4A;
                break;
            case "4C":
                e = _4C;
                break;
            default:
                e = null;
                break;
        }
        return e;
    }

    /** 值 */
    private String value;
    /** 描述 */
    private String des;
    /** 默认所占字节 */
    private int byteLength;
    /** 小数点位数 */
    private int decimals;
    /**
     * 是否是16进制表示
     */
    private boolean isHex;
    /**
     * 是否是公共因子
     */
    private boolean isCommonFactor;


    IdentifierPilotEnum(String value, String des, int byteLength, int decimals,boolean isHex,boolean isCommonFactor) {
        this.value = value;
        this.des = des;
        this.byteLength = byteLength;
        this.decimals = decimals;
        this.isHex=isHex;
        this.isCommonFactor=isCommonFactor;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public int getByteLength() {
        return byteLength;
    }

    public void setByteLength(int byteLength) {
        this.byteLength = byteLength;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public boolean isHex() {
        return isHex;
    }

    public void setHex(boolean hex) {
        isHex = hex;
    }

    public boolean isCommonFactor() {
        return isCommonFactor;
    }

    public void setCommonFactor(boolean commonFactor) {
        isCommonFactor = commonFactor;
    }
}
