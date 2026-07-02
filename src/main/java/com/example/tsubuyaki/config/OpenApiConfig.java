package com.example.tsubuyaki.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "社内つぶやきボード API",
        version = "v1",
        description = "投稿一覧を取得する研修用 REST API"))
public class OpenApiConfig {
}
