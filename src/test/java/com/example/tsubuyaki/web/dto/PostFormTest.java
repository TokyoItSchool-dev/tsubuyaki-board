package com.example.tsubuyaki.web.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PostFormTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("PostForm_投稿者と本文_設定した値を取得できる")
    void PostForm_投稿者と本文_設定した値を取得できる() {
        PostForm form = new PostForm();

        form.setAuthor("alice");
        form.setBody("本文です");
        form.setAvatarColor("blue");

        assertThat(form.getAuthor()).isEqualTo("alice");
        assertThat(form.getBody()).isEqualTo("本文です");
        assertThat(form.getAvatarColor()).isEqualTo("blue");
    }

    @Test
    @DisplayName("PostForm_authorが範囲外または空白のみ_エラーになる")
    void PostForm_authorが範囲外または空白のみ_エラーになる() {
        PostForm blankAuthor = new PostForm();
        blankAuthor.setAuthor("   ");
        blankAuthor.setBody("本文です");

        PostForm tooLongAuthor = new PostForm();
        tooLongAuthor.setAuthor("a".repeat(31));
        tooLongAuthor.setBody("本文です");

        assertThat(messagesOf(validator.validate(blankAuthor)))
                .contains("投稿者名を入力してください");
        assertThat(messagesOf(validator.validate(tooLongAuthor)))
                .contains("投稿者名は 30 文字以内で入力してください");
    }

    @Test
    @DisplayName("PostForm_avatarColorが空_エラーにならない")
    void PostForm_avatarColorが空_エラーにならない() {
        PostForm form = new PostForm();
        form.setAuthor("alice");
        form.setBody("本文です");
        form.setAvatarColor("");

        assertThat(propertyNamesOf(validator.validate(form)))
                .doesNotContain("avatarColor");
    }

    @Test
    @DisplayName("PostForm_bodyが範囲外または空白のみ_エラーになる")
    void PostForm_bodyが範囲外または空白のみ_エラーになる() {
        PostForm blankBody = new PostForm();
        blankBody.setAuthor("alice");
        blankBody.setBody("   ");

        PostForm tooLongBody = new PostForm();
        tooLongBody.setAuthor("alice");
        tooLongBody.setBody("a".repeat(281));

        assertThat(messagesOf(validator.validate(blankBody)))
                .contains("本文を入力してください");
        assertThat(messagesOf(validator.validate(tooLongBody)))
                .contains("本文は 280 文字以内で入力してください");
    }

    private static Set<String> messagesOf(Set<ConstraintViolation<PostForm>> violations) {
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());
    }

    private static Set<String> propertyNamesOf(Set<ConstraintViolation<PostForm>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());
    }
}
