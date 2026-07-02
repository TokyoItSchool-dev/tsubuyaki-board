package com.example.tsubuyaki.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostFormTest {

    @Test
    @DisplayName("投稿フォーム_値を設定したとき_getterで同じ値を取得できる")
    void postForm_値を設定したとき_getterで同じ値を取得できる() {
        PostForm form = new PostForm();

        form.setAuthor("alice");
        form.setBody("本日の共有です");

        assertThat(form.getAuthor()).isEqualTo("alice");
        assertThat(form.getBody()).isEqualTo("本日の共有です");
    }
}
