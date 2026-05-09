package com.thinkboot.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ThinkBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThinkBootApplication.class, args);
        System.out.println("ThinkBoot started successfully!");
    }
}