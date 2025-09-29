package com.wayeal.cloud.rabbit;

import com.wayeal.cloud.config.CustomDataConfig;
import com.wayeal.cloud.config.RabbitConfig;
import com.wayeal.cloud.enums.ProtocolType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageProducer {

    private static final Logger log= LoggerFactory.getLogger(MessageProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CustomDataConfig customDataConfig;

    public void send(String message, ProtocolType protocolType){
        log.info("发送消息开始");
        try{
            this.rabbitTemplate.convertAndSend(customDataConfig.getTopicExchange(),customDataConfig.getMqPrefix()+protocolType.name(),message);
        }catch (Exception e){
            log.error("send error----> {}",message);
        }
        log.info("send message is:"+message);
        log.info("发送消息结束");
    }
 }
