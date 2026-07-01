package com.example.tsubuyaki.domain;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostTest {

    @Test
    @DisplayName("投稿日時_createdAt_DBのTIMESTAMPとして読み書きする")
    void 投稿日時_createdAt_DBのTIMESTAMPとして読み書きする() throws NoSuchFieldException {
        JdbcTypeCode jdbcTypeCode = Post.class
                .getDeclaredField("createdAt")
                .getAnnotation(JdbcTypeCode.class);

        assertThat(jdbcTypeCode).isNotNull();
        assertThat(jdbcTypeCode.value()).isEqualTo(SqlTypes.TIMESTAMP);
    }
}
