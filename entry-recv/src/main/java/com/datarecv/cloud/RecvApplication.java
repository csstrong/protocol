package com.datarecv.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RecvApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecvApplication.class, args);
    }
}
