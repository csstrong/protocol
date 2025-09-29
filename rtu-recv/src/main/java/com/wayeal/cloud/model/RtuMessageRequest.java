package com.wayeal.cloud.model;

public class RtuMessageRequest extends Message {

    protected  MessageRequest messageRequest;

    protected  ContentMessageRequest contentMessageRequest;

    protected  String     start;

    protected  String       end;

    private    String checkCode;

    private    String  clientId;

    private    int    messageId;

    private    int     serialNo;

    public MessageRequest getMessageRequest() {
        return messageRequest;
    }

    public void setMessageRequest(MessageRequest messageRequest) {
        this.messageRequest = messageRequest;
    }

    public ContentMessageRequest getContentMessageRequest() {
        return contentMessageRequest;
    }

    public void setContentMessageRequest(ContentMessageRequest contentMessageRequest) {
        this.contentMessageRequest = contentMessageRequest;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(int serialNo) {
        this.serialNo = serialNo;
    }
}
