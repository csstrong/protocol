package com.wayeal.cloud.rabbit;

import com.rabbitmq.client.Channel;
import com.wayeal.cloud.service.DataHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author jian
 * @version 2022-04-26 9:20
 */
@Component
public class MessageConsumer {

    private static final Logger log= LoggerFactory.getLogger(MessageConsumer.class);
    @Autowired
    private DataHandle dataHandle;
    @RabbitListener(queues = "#{'${wy.in}'.split(';')}")
    @RabbitHandler
    public void process(String msgData, Channel channel, Message message) {
        try {
            log.info("待消费的消息是 {}", msgData);
            dataHandle.init(msgData);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ioException) {
                log.error("message receiver is fail  message is:{}",msgData);
                ioException.printStackTrace();
            }
            e.printStackTrace();
        }
    }

}
