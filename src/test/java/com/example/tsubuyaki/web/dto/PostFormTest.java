package com.example.tsubuyaki.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostFormTest {

    @Test
    @DisplayName("PostForm_値を設定したとき_getterで同じ値を取得できる")
    void PostForm_値を設定したとき_getterで同じ値を取得できる() {
        PostForm form = new PostForm();

        form.setAuthor("alice");
        form.setBody("hello");
        form.setAuthorIconColor("#F97316");

        assertThat(form.getAuthor()).isEqualTo("alice");
        assertThat(form.getBody()).isEqualTo("hello");
        assertThat(form.getAuthorIconColor()).isEqualTo("#F97316");
    }

    @Test
    @DisplayName("PostForm_生成直後_左端のアイコン色が初期値になる")
    void PostForm_生成直後_左端のアイコン色が初期値になる() {
        PostForm form = new PostForm();

        assertThat(form.getAuthorIconColor()).isEqualTo("#2563EB");
    }
}
