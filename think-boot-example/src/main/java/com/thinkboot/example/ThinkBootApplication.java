package com.thinkboot.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = {
    "com.thinkboot.example.mapper",
    "com.thinkboot.web.mapper"
})
public class ThinkBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThinkBootApplication.class, args);
        System.out.println("ThinkBoot started successfully!");
    }
}