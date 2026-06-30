package com.example.tsubuyaki.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostFormTest {

    @Test
    @DisplayName("PostForm_投稿者と本文_設定した値を取得できる")
    void PostForm_投稿者と本文_設定した値を取得できる() {
        PostForm form = new PostForm();

        form.setAuthor("alice");
        form.setBody("本文です");

        assertThat(form.getAuthor()).isEqualTo("alice");
        assertThat(form.getBody()).isEqualTo("本文です");
    }
}
