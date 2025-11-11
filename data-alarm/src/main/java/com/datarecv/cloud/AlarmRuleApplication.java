package com.datarecv.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AlarmRuleApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlarmRuleApplication.class, args);
    }
}
