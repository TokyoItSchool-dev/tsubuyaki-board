package com.example.tsubuyaki.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AppCssTest {

    @Test
    @DisplayName("削除ボタンCSS_ライトモードとダークモードで見やすい赤色を定義する")
    void 削除ボタンCSS_ライトモードとダークモードで見やすい赤色を定義する() throws IOException {
        String css = Files.readString(Path.of("src/main/resources/static/css/app.css"));

        assertThat(css).contains("--color-danger: #dc2626");
        assertThat(css).contains(".theme-dark");
        assertThat(css).contains("--color-danger: #f87171");
        assertThat(css).contains(".delete-button");
        assertThat(css).contains("background: var(--color-danger)");
    }
}
