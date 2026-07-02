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

    @Test
    @DisplayName("いいねボタン_詳細画面_レイアウトが崩れない固定寸法と横並びCSSを持つ")
    void いいねボタン_詳細画面_レイアウトが崩れない固定寸法と横並びCSSを持つ() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).contains(".post__good-button");
        assertThat(css).contains("display: inline-flex");
        assertThat(css).contains("align-items: center");
        assertThat(css).contains("min-width: 2.5rem");
        assertThat(css).contains("min-height: 2.25rem");
        assertThat(css).contains("white-space: nowrap");
    }

    @Test
    @DisplayName("投稿詳細アクション_いいねボタン追加後も_狭い画面で折り返して表示できる")
    void 投稿詳細アクション_いいねボタン追加後も_狭い画面で折り返して表示できる() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).contains(".post__actions");
        assertThat(css).contains("display: flex");
        assertThat(css).contains("flex-wrap: wrap");
        assertThat(css).contains(".post__like-form");
        assertThat(css).contains("flex: 0 0 auto");
    }

    @Test
    @DisplayName("いいねボタン_いいね済みのとき_黄色で表示するCSSを持つ")
    void いいねボタン_いいね済みのとき_黄色で表示するCSSを持つ() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).contains(".post__good-button--active");
        assertThat(css).contains("background: #facc15");
        assertThat(css).contains("border-color: #eab308");
    }
}
