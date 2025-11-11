package com.datarecv.cloud.rabbit;

import com.datarecv.cloud.enums.ProtocolType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqSendUtil implements RabbitMqInterface {
    @Autowired
    private  MessageProducer messageProducer;

    @Override
    public  void  sendMessage(String message, ProtocolType protocolType){
       messageProducer.send(message,protocolType);
    }
}
