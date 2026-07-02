package com.example.tsubuyaki.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class JpaInstantMappingConfigTest {

    @Test
    @DisplayName("日時マッピング_Instant型_既存DDLに合わせてTIMESTAMPとして扱う")
    void instantMapping_usesTimestampJdbcType() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new FileSystemResource("src/main/resources/application.yml"));

        Properties properties = yaml.getObject();

        assertThat(properties)
                .isNotNull()
                .containsEntry(
                        "spring.jpa.properties.hibernate.type.preferred_instant_jdbc_type",
                        "TIMESTAMP");
    }
}
