package com.example.tsubuyaki.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostFormTest {

    @Test
    @DisplayName("PostForm_投稿者と本文と色を設定したとき_同じ値を取得できる")
    void PostForm_投稿者と本文と色を設定したとき_同じ値を取得できる() {
        PostForm form = new PostForm();

        form.setAuthor("alice");
        form.setBody("hello");
        form.setColor("D9F99D");

        assertThat(form.getAuthor()).isEqualTo("alice");
        assertThat(form.getBody()).isEqualTo("hello");
        assertThat(form.getColor()).isEqualTo("D9F99D");
    }
}
