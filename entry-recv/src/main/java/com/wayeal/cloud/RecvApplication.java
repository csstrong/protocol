package com.wayeal.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author jian
 * @version 2023-02-15 14:50
 */
@SpringBootApplication
@EnableAsync
public class RecvApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecvApplication.class, args);
    }
}
