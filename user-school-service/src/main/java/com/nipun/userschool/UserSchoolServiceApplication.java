package com.nipun.userschool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.nipun.userschool", "com.nipun.shared"})
public class UserSchoolServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserSchoolServiceApplication.class, args);
    }
}
