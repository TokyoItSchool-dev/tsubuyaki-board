package com.example.tsubuyaki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class TsubuyakiApplication {

    private static final String APPLICATION_TIME_ZONE = "Asia/Tokyo";

    public static void main(String[] args) {
        configureDefaultTimeZone();
        SpringApplication.run(TsubuyakiApplication.class, args);
    }

    static void configureDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(APPLICATION_TIME_ZONE));
    }
}
