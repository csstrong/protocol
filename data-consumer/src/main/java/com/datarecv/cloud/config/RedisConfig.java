package com.datarecv.cloud.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.net.UnknownHostException;
import java.util.concurrent.ThreadPoolExecutor;

@Data
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String dbHost;

    @Value("${spring.redis.port}")
    private String dbPort;

    @Value("${spring.redis.database}")
    private String dbNum;

    @Value("${spring.redis.password}")
    private String password;

    @Bean
    @SuppressWarnings("all")
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory redisConnectionFactory)
        throws UnknownHostException {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // Json序列化配置
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        // String序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key 序列化方式
        template.setKeySerializer(stringRedisSerializer);

        // hash的key序列化方式
        template.setHashKeySerializer(stringRedisSerializer);

        // value的序列化方式（json序列化）value的序列化方式（String序列化，value 不是String类型会报错，建议使用json序列化）
        template.setValueSerializer(jackson2JsonRedisSerializer);

        // hash的value序列化方式（json序列化）
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public PatternTopic patternTopic() {
        return new PatternTopic("__keyevent@" + dbNum + "__:expired");
    }

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory redisConnectionFactory) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(200);
        executor.setThreadNamePrefix("redis-key");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();

        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        // 设置监听使用的线程池
        redisMessageListenerContainer.setTaskExecutor(executor);
        return redisMessageListenerContainer;
    }

    @Bean
    public Redisson redisson() {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress("redis://" + dbHost + ":" + dbPort).setDatabase(Integer.parseInt(dbNum));
        if (password != null && !"".equals(password)) {
            singleServerConfig.setPassword(password);
        }
        //心跳
        singleServerConfig.setPingConnectionInterval(1000);
        return (Redisson) Redisson.create(config);
    }
}
