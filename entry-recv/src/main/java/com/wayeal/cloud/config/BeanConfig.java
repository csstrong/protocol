package com.wayeal.cloud.config;

import com.wayeal.cloud.session.SessionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author jian
 * @version 2023-02-24 10:05
 */
@Configuration
public class BeanConfig {

    @Bean
    public SessionManager getSessionManager(){
        return new SessionManager();
    }
}
