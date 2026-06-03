package com.nipun.whatsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.nipun.whatsapp", "com.nipun.shared"})
public class WhatsAppServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WhatsAppServiceApplication.class, args);
    }
}
