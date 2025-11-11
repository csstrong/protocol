package com.datarecv.cloud.rabbit;

import com.datarecv.cloud.service.DataHandle;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;

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
