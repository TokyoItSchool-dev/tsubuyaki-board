package com.example.tsubuyaki.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PostListStyleTest {

    @Test
    @DisplayName("投稿一覧_検索ボックス_枠なし下線付きでプレースホルダーをグレー表示する")
    void 投稿一覧_検索ボックス_枠なし下線付きでプレースホルダーをグレー表示する() throws IOException {
        String css = Files.readString(Path.of("src/main/resources/static/css/app.css"));

        assertThat(css).contains(".search-box");
        assertThat(css).contains("border: 0;");
        assertThat(css).contains("border-bottom: 1px solid var(--color-border);");
        assertThat(css).contains(".search-box__input::placeholder");
        assertThat(css).contains("color: var(--color-muted);");
    }
}
