package com.nipun.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.nipun.ai", "com.nipun.shared"})
public class AIServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AIServiceApplication.class, args);
    }
}
