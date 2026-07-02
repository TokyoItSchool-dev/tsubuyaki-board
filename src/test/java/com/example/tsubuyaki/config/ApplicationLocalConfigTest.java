package com.example.tsubuyaki.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationLocalConfigTest {

    @Test
    @DisplayName("Oracle接続設定_タイムゾーンをリージョン名として扱わない")
    void Oracle接続設定_タイムゾーンをリージョン名として扱わない() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application-local.yml"));

        Properties properties = yaml.getObject();

        assertThat(properties)
                .containsEntry("spring.datasource.hikari.data-source-properties.oracle.jdbc.timezoneAsRegion",
                        "false");
    }

    @Test
    @DisplayName("共通JDBC設定_タイムゾーンを東京に固定する")
    void 共通JDBC設定_タイムゾーンを東京に固定する() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new FileSystemResource("src/main/resources/application.yml"));

        Properties properties = yaml.getObject();

        assertThat(properties)
                .containsEntry("spring.jpa.properties.hibernate.jdbc.time_zone", "Asia/Tokyo");
    }
}
