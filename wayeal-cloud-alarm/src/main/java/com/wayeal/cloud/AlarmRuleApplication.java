package com.wayeal.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author jian
 * @version 2022-08-08 10:22
 */
@SpringBootApplication
@EnableCaching
public class AlarmRuleApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlarmRuleApplication.class, args);
    }
}
