package com.example.tsubuyaki.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostFormTest {

    @Test
    @DisplayName("PostForm_getterSetter_投稿者と本文とアバター色を保持する")
    void getterSetter_keepsAuthorBodyAndAvatarColor() {
        PostForm form = new PostForm();

        form.setAuthor("alice");
        form.setBody("hello");
        form.setAvatarColor("purple");

        assertThat(form.getAuthor()).isEqualTo("alice");
        assertThat(form.getBody()).isEqualTo("hello");
        assertThat(form.getAvatarColor()).isEqualTo("purple");
    }
}
