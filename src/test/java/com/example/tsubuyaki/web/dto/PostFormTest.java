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
    @DisplayName("投稿作成_本文にscriptタグがある場合_入力エラーにする")
    void body_withScriptTag_isInvalid() {
        PostForm form = new PostForm();
        form.setAuthor("alice");
        form.setBody("<script>alert('危険')</script>");

        Set<ConstraintViolation<PostForm>> violations = validator.validate(form);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("本文に script タグは入力できません");
    }
}
