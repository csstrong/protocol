package com.wayeal.cloud.config;

import com.wayeal.cloud.enums.ProtocolType;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author jian
 * @version 2023-02-28 15:03
 */
@Configuration
public class RabbitConfig {

  //  public static final String prefix="wayeal_cloud_in_";

   // public static final String topicExchange="topicExchange";

    @Autowired
    private CustomDataConfig customDataConfig;

    @Bean
    public Queue rtuSL561() {
        return new Queue(customDataConfig.getMqPrefix()+ ProtocolType.RTU_SL561);
    }

    @Bean
    public Queue dtu212() {
        return new Queue(customDataConfig.getMqPrefix()+ ProtocolType.DTU_212);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(customDataConfig.getTopicExchange());
    }

    @Bean
    public Binding bindingExchangeRtuSl561Message(Queue rtuSL561, TopicExchange exchange) {
        return BindingBuilder.bind(rtuSL561).to(exchange).with(customDataConfig.getMqPrefix()+ProtocolType.RTU_SL561.name());
    }

    @Bean
    public Binding bindingExchangeDtu212Message(Queue dtu212, TopicExchange exchange) {
        return BindingBuilder.bind(dtu212).to(exchange).with(customDataConfig.getMqPrefix()+ProtocolType.DTU_212.name());
    }


}
