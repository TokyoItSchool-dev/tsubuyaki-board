package com.example.tsubuyaki.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostFormTest {

    @Test
    @DisplayName("投稿フォーム_値を設定したとき_同じ値を取得できる")
    void 投稿フォーム_値を設定したとき_同じ値を取得できる() {
        PostForm form = new PostForm();

        form.setAuthor("alice");
        form.setBody("hello");

        assertThat(form.getAuthor()).isEqualTo("alice");
        assertThat(form.getBody()).isEqualTo("hello");
    }
}
