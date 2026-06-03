package com.nipun.curriculum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.nipun.curriculum", "com.nipun.shared"})
public class CurriculumServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CurriculumServiceApplication.class, args);
    }
}
