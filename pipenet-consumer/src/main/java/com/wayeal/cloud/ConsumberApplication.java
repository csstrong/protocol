package com.wayeal.cloud;

import com.wayeal.cloud.utils.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/*
 * @author  chensi
 * @date  2022/7/26
 */
@SpringBootApplication
@Import(SpringUtil.class)
public class ConsumberApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumberApplication.class, args);
    }
}
