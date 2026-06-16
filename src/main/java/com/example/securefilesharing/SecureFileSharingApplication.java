package com.example.securefilesharing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SecureFileSharingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecureFileSharingApplication.class, args);
    }
}
