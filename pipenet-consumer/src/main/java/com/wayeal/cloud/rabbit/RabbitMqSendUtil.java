package com.wayeal.cloud.rabbit;


import com.wayeal.cloud.utils.SpringUtil;

/**
 * @author jian
 * @version 2022-04-26 10:57
 */
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
            messageProducer=SpringUtil.getBean(MessageProducer.class);
        }
        messageProducer.send(que,message);
    }
}
