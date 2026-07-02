package com.example.tsubuyaki.web.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostFormTest {

    private final Validator validator = createValidator();

    @Test
    @DisplayName("投稿フォーム_投稿者と本文を設定したとき_同じ値を取得できる")
    void getters_whenAuthorAndBodyAreSet_returnSameValues() {
        PostForm form = new PostForm();

        form.setAuthor("alice");
        form.setBody("hello");

        assertThat(form.getAuthor()).isEqualTo("alice");
        assertThat(form.getBody()).isEqualTo("hello");
    }

    @Test
    @DisplayName("投稿フォーム_投稿者と本文が空欄のとき_入力エラーになる")
    void validation_whenAuthorAndBodyAreEmpty_returnsErrors() {
        PostForm form = new PostForm();
        form.setAuthor("");
        form.setBody("");

        var violations = validator.validate(form);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("author", "body");
        assertThat(violations)
                .extracting(violation -> violation.getMessage())
                .contains("投稿者名を入力してください", "本文を入力してください");
    }

    @Test
    @DisplayName("投稿フォーム_投稿者と本文が最大文字数を超えるとき_入力エラーになる")
    void validation_whenAuthorAndBodyExceedMaxLength_returnsErrors() {
        PostForm form = new PostForm();
        form.setAuthor("a".repeat(31));
        form.setBody("b".repeat(281));

        var violations = validator.validate(form);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("author", "body");
        assertThat(violations)
                .extracting(violation -> violation.getMessage())
                .contains("投稿者名は 30 文字以内で入力してください", "本文は 280 文字以内で入力してください");
    }

    private static Validator createValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator();
        }
    }
}
