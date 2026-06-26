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
    @DisplayName("PostForm_getterSetter_投稿者と本文を保持できる")
    void getterSetter_storesAuthorAndBody() {
        PostForm form = new PostForm();

        form.setAuthor("alice");
        form.setBody("こんにちは");

        assertThat(form.getAuthor()).isEqualTo("alice");
        assertThat(form.getBody()).isEqualTo("こんにちは");
    }

    @Test
    @DisplayName("PostForm_空入力_投稿者と本文の入力エラーになる")
    void validation_whenBlank_hasAuthorAndBodyErrors() {
        PostForm form = new PostForm();
        form.setAuthor("");
        form.setBody("");

        Set<ConstraintViolation<PostForm>> violations = validator.validate(form);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("投稿者名を入力してください", "本文を入力してください");
    }
}
