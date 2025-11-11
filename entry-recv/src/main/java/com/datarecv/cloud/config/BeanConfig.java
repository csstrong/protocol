package com.datarecv.cloud.config;

import com.datarecv.cloud.session.SessionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public SessionManager getSessionManager(){
        return new SessionManager();
    }
}
