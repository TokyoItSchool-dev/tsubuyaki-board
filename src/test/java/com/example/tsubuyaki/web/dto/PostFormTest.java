package com.example.tsubuyaki.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostFormTest {

    @Test
    @DisplayName("投稿フォーム_投稿者アバター色本文_設定した値を取得できる")
    void 投稿フォーム_投稿者アバター色本文_設定した値を取得できる() {
        PostForm form = new PostForm();

        form.setAuthor("alice");
        form.setAvatarColor("GREEN");
        form.setBody("hello");

        assertThat(form.getAuthor()).isEqualTo("alice");
        assertThat(form.getAvatarColor()).isEqualTo("GREEN");
        assertThat(form.getBody()).isEqualTo("hello");
    }

    @Test
    @DisplayName("投稿フォーム_初期値_avatarColorはBLUE")
    void 投稿フォーム_初期値_avatarColorはBLUE() {
        PostForm form = new PostForm();

        assertThat(form.getAvatarColor()).isEqualTo("BLUE");
    }
}
