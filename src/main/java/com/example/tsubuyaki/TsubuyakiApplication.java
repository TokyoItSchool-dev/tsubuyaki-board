package com.example.tsubuyaki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class TsubuyakiApplication {

    /**
     * Spring Boot アプリケーションを起動する。
     *
     * @param args 起動引数
     */
    public static void main(String[] args) {
        SpringApplication.run(TsubuyakiApplication.class, args);
    }

    /**
     * アプリケーション全体で利用する現在日時取得用の Clock を提供する。
     *
     * @return システム標準タイムゾーンの Clock
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
