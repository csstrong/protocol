package com.wayeal.cloud.rabbit;


import com.wayeal.cloud.enums.ProtocolType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author jian
 * @version 2022-04-26 10:57
 */
@Component
public class RabbitMqSendUtil implements RabbitMqInterface{
    @Autowired
    private  MessageProducer messageProducer;

    @Override
    public  void  sendMessage(String message, ProtocolType protocolType){
       messageProducer.send(message,protocolType);
    }
}
