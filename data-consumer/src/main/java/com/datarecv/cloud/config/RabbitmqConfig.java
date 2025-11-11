package com.datarecv.cloud.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class RabbitmqConfig {

    @Bean("batchQueueRabbitListenerContainerFactory")
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        //setConnectionFactory：设置spring-amqp的ConnectionFactory。
        factory.setConnectionFactory(connectionFactory);
        //factory.setMessageConverter(new Jackson2JsonMessageConverter());
        //确认方式,manual为手动ack.
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        //每次处理数据数量，提高并发量
        factory.setPrefetchCount(1);
        //设置线程数
        factory.setConcurrentConsumers(1);
        //最大线程数
        factory.setMaxConcurrentConsumers(1);

        //factory.setDefaultRequeueRejected(true);
        //使用自定义线程池来启动消费者。
        factory.setTaskExecutor(taskExecutor());
        return factory;
    }

    @Bean("correctTaskExecutor")
    @Primary
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(8);
        // 设置最大线程数
        executor.setMaxPoolSize(100);
        // 设置队列容量
        executor.setQueueCapacity(100);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(300);
        // 设置默认线程名称
        executor.setThreadNamePrefix("thread-file-queue");
        // 设置拒绝策略rejection-policy：当pool已经达到max size的时候，丢弃
         executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }
}

