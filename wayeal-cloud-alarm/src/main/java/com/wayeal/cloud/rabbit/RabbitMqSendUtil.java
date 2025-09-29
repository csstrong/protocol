package com.wayeal.cloud.rabbit;


import com.wayeal.cloud.util.SpringUtil;

public class RabbitMqSendUtil {

    private static MessageProducer messageProducer;

    public static  void  sendMessage(String message){
        if (messageProducer==null){
            messageProducer= SpringUtil.getBean(MessageProducer.class);
        }
        messageProducer.send(message);
    }
    public static  void  sendMessage(String que,String message){
        if (messageProducer==null){
            messageProducer= SpringUtil.getBean(MessageProducer.class);
        }
        messageProducer.send(que,message);
    }
}
