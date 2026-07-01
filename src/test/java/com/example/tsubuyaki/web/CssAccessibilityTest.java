package com.example.tsubuyaki.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CssAccessibilityTest {

    @Test
    @DisplayName("CSS_色弱配慮_赤緑に依存せずフォーカス表示と中立色を使う")
    void css_usesColorBlindFriendlyPalette() throws IOException {
        String css = Files.readString(Path.of("src/main/resources/static/css/app.css"));

        assertThat(css).contains("--color-accent: #2563eb");
        assertThat(css).contains("--color-focus: #f59e0b");
        assertThat(css).contains(":focus-visible");
        assertThat(css).contains(".post__body--truncated:hover::after");
        assertThat(css).contains(".post__body--truncated:focus-visible::after");
        assertThat(css).doesNotContain("#ff0000", "#00ff00", "red", "green");
    }

    @Test
    @DisplayName("CSS_投稿フォーム_textareaの縦リサイズにフォーム背景が追従する")
    void css_postFormBackgroundFollowsTextareaResize() throws IOException {
        String css = Files.readString(Path.of("src/main/resources/static/css/app.css"));

        assertThat(css).contains(".post-form {");
        assertThat(css).contains("width: 100%;");
        assertThat(css).contains("box-sizing: border-box;");
        assertThat(css).contains("height: auto;");
        assertThat(css).contains("overflow: visible;");
        assertThat(css).contains(".form-field {");
        assertThat(css).contains("min-width: 0;");
        assertThat(css).contains("overflow: hidden;");
        assertThat(css).contains(".form-field input,\n.form-field textarea {");
        assertThat(css).contains(".form-field textarea {");
        assertThat(css).contains("resize: vertical;");
        assertThat(css).contains("max-width: 100%;");
    }
}
