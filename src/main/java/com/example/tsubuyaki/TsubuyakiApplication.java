package com.example.tsubuyaki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TsubuyakiApplication {

    // Spring Boot アプリケーションを起動し、Controller/Service/Repository を利用可能にする。
    public static void main(String[] args) {
        SpringApplication.run(TsubuyakiApplication.class, args);
    }
}
