package com.example.tsubuyaki.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostFormTest {

    @Test
    @DisplayName("投稿フォーム_投稿者本文アバター色_setterで設定した値をgetterで取得できる")
    void accessors_returnConfiguredValues() {
        PostForm form = new PostForm();

        form.setAuthor("tanaka");
        form.setBody("本文です");
        form.setAvatarColor("blue");

        assertThat(form.getAuthor()).isEqualTo("tanaka");
        assertThat(form.getBody()).isEqualTo("本文です");
        assertThat(form.getAvatarColor()).isEqualTo("blue");
    }
}
