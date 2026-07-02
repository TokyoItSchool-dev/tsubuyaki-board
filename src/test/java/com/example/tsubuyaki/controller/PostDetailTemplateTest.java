package com.example.tsubuyaki.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PostDetailTemplateTest {

    @Test
    @DisplayName("投稿詳細_いいね数がnullの場合_0へフォールバックする")
    void detail_whenLikeCountIsNull_fallsBackToZero() throws Exception {
        String template = new ClassPathResource("templates/posts/detail.html")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(template).contains("likeCount ?: 0");
        assertThat(template).doesNotContain("いいね ${likeCount}");
    }
}
