/*
 * 社内つぶやきボード Spring Boot アプリケーションの起動クラス。
 */
package com.example.tsubuyaki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class TsubuyakiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TsubuyakiApplication.class, args);
    }

    /**
     * アプリケーション内で現在時刻を取得するための基準時計を提供する。
     *
     * <p>投稿作成日時や論理削除日時はこの {@link Clock} 経由で取得し、
     * テストでは固定時刻へ差し替えられるようにする。</p>
     *
     * @return システム既定タイムゾーンの時計
     */
    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
