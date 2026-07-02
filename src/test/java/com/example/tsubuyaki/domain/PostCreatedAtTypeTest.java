package com.example.tsubuyaki.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PostCreatedAtTypeTest {

    @Test
    @DisplayName("投稿日時_createdAt_TIMESTAMP型に合わせLocalDateTimeで扱う")
    void 投稿日時_createdAt_TIMESTAMP型に合わせLocalDateTimeで扱う() throws NoSuchMethodException {
        assertThat(Post.class.getMethod("getCreatedAt").getReturnType()).isEqualTo(LocalDateTime.class);
    }
}
