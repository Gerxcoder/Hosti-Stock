package com.hostistock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HostiStockApplication {

    public static void main(String[] args) {
        SpringApplication.run(HostiStockApplication.class, args);
    }
}