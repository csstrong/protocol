package com.wayeal.cloud.config;

import com.wayeal.cloud.session.SessionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public SessionManager getSessionManager(){
        return new SessionManager();
    }
}
