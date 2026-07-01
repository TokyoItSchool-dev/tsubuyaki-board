package com.example.tsubuyaki.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PostListLayoutTest {

    @Test
    @DisplayName("投稿検索_検索フォーム_狭い画面でも折り返して表示できるCSSを持つ")
    void 投稿検索_検索フォーム_狭い画面でも折り返して表示できるCSSを持つ() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).contains(".toolbar");
        assertThat(css).contains("display: flex");
        assertThat(css).contains("flex-wrap: wrap");
        assertThat(css).contains(".toolbar__query");
        assertThat(css).contains("min-width: 0");
    }
}
