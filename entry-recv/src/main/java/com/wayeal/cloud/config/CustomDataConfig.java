package com.wayeal.cloud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomDataConfig {


    @Value(value = "${wy.port}")
    private  int  port;

    @Value(value = "${wy.mqPrefix}")
    private String mqPrefix;

    @Value(value = "${wy.topicExchange}")
    private String topicExchange;


    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMqPrefix() {
        return mqPrefix;
    }

    public void setMqPrefix(String mqPrefix) {
        this.mqPrefix = mqPrefix;
    }

    public String getTopicExchange() {
        return topicExchange;
    }

    public void setTopicExchange(String topicExchange) {
        this.topicExchange = topicExchange;
    }
}
