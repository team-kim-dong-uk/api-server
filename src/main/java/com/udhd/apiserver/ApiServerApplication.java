package com.udhd.apiserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class ApiServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiServerApplication.class, args);
    }

}
