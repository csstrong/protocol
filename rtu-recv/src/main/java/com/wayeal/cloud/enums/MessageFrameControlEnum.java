package com.wayeal.cloud.enums;

public enum MessageFrameControlEnum {

    STX ("02","传输正文起始"            ,""),

    SYN ("16","多包传输正文起始"         ,"多包发送，一次确认的传输模式中使用"),

    ETX ("03","报文结束，后续无报文"      ,"作为报文结束符，表示传输完成，等待退出通信"),

    ETB ("17","报文结束，后续有报文在报文"  ,"分包传输时作为报文结束符，表示传输未完成，不可退出通信"),

    ENQ ("05","询问"                    ,"作为下行查询及控制命令帧的报文结束符"),

    EOT ("04","传输结束，退出"            ,"作为传输结束确认帧报文结束符，表示可以退出通信"),

    ACK ("06","肯定确认，继续发送"         ,"作为有后续报文帧的“确认帧”报文结束符"),

    NAK ("15","否定应答，反馈重发"         ,"用于要求对方重发某数据包的报文结束符"),

    ESC ("1B","传输结束，终端保持在线"      ,"在下行确认帧代替EOT作为报文结束符，要求终端在线保持在线10分钟内若没有接收到中心站命令，终端退回原先设定的工作状态");

    private String hexStr;

    private String function;

    private String des;

    MessageFrameControlEnum(String hexStr, String function, String des) {
        this.hexStr = hexStr;
        this.des = des;
        this.function=function;
    }

    public String getHexStr() {
        return hexStr;
    }

    public void setHexStr(String hexStr) {
        this.hexStr = hexStr;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }
}
