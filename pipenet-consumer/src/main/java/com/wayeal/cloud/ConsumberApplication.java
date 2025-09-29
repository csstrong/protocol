package com.wayeal.cloud;

import com.wayeal.cloud.utils.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SpringUtil.class)
public class ConsumberApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumberApplication.class, args);
    }
}
