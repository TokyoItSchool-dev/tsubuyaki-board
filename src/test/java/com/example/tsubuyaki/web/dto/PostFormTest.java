package com.example.tsubuyaki.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostFormTest {

    @Test
    @DisplayName("投稿フォーム_投稿者と本文_設定した値を取得できる")
    void accessors_returnConfiguredValues() {
        PostForm form = new PostForm();

        form.setAuthor("alice");
        form.setBody("朝の共有です");

        assertThat(form.getAuthor()).isEqualTo("alice");
        assertThat(form.getBody()).isEqualTo("朝の共有です");
    }
}
