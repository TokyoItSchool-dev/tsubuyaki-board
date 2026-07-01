package com.example.tsubuyaki.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostFormTest {

    @Test
    @DisplayName("PostForm_投稿者と本文を設定したとき_getterで同じ値を返す")
    void postForm_投稿者と本文を設定したとき_getterで同じ値を返す() {
        PostForm form = new PostForm();

        form.setAuthor("alice");
        form.setBody("今日の共有です");

        assertThat(form.getAuthor()).isEqualTo("alice");
        assertThat(form.getBody()).isEqualTo("今日の共有です");
    }
}
