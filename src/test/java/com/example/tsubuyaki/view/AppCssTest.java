package com.example.tsubuyaki.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class AppCssTest {

    @Test
    @DisplayName("投稿フォーム_エラーメッセージ_赤文字で表示する")
    void 投稿フォーム_エラーメッセージ_赤文字で表示する() throws IOException {
        String css = new String(
                getClass().getResourceAsStream("/static/css/app.css").readAllBytes(),
                StandardCharsets.UTF_8);

        assertThat(css).contains(".error", "color: #b91c1c");
    }
}
