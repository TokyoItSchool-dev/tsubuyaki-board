package com.example.tsubuyaki.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostFormTest {

    @Test
    @DisplayName("PostForm_値を設定したとき_投稿者と本文とアバター色を取得できる")
    void PostForm_値を設定したとき_投稿者と本文とアバター色を取得できる() {
        PostForm form = new PostForm();

        form.setAuthor("alice");
        form.setBody("hello");
        form.setAvatarColor("#3366cc");

        assertThat(form.getAuthor()).isEqualTo("alice");
        assertThat(form.getBody()).isEqualTo("hello");
        assertThat(form.getAvatarColor()).isEqualTo("#3366cc");
    }

    @Test
    @DisplayName("PostForm_生成したとき_アバター色の初期値を持つ")
    void PostForm_生成したとき_アバター色の初期値を持つ() {
        PostForm form = new PostForm();

        assertThat(form.getAvatarColor()).isEqualTo("#ffffff");
    }
}
