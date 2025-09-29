package com.wayeal.cloud.rabbit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value(value = "${wy.notify}")
    private String notify;

    private static final Logger log= LoggerFactory.getLogger(MessageProducer.class);
    public void send(String message){
        log.info("发送消息开始");
        this.rabbitTemplate.convertAndSend(notify,message);
        log.info("send message is:"+message);
        log.info("发送消息结束");
    }
    public void send(String queName, String message){
        log.info("发送消息开始");
        this.rabbitTemplate.convertAndSend(queName,message);
        log.info("发送消息结束");
    }
 }
