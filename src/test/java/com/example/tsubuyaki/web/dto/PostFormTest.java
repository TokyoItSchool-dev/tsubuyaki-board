package com.example.tsubuyaki.web.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PostFormTest {

    @Test
    @DisplayName("PostForm_投稿者と本文とアバターカラー_設定した値を取得できる")
    void getters_returnValuesSetBySetters() {
        PostForm form = new PostForm();

        form.setAuthor("alice");
        form.setBody("hello");
        form.setAvatarColor("blue");

        assertThat(form.getAuthor()).isEqualTo("alice");
        assertThat(form.getBody()).isEqualTo("hello");
        assertThat(form.getAvatarColor()).isEqualTo("blue");
    }

    @Test
    @DisplayName("PostForm_アバターカラー_赤青黄色と未選択を許可する")
    void avatarColor_whenAllowedValues_hasNoValidationErrors() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        assertThat(validateAvatarColor(validator, "red")).isEmpty();
        assertThat(validateAvatarColor(validator, "blue")).isEmpty();
        assertThat(validateAvatarColor(validator, "yellow")).isEmpty();
        assertThat(validateAvatarColor(validator, "")).isEmpty();
    }

    @Test
    @DisplayName("PostForm_アバターカラー_赤青黄色以外はエラーにする")
    void avatarColor_whenUnsupportedValue_hasValidationError() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        Set<ConstraintViolation<PostForm>> violations = validateAvatarColor(validator, "green");

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactly("アバターカラーは赤・青・黄色から選択してください。");
    }

    private Set<ConstraintViolation<PostForm>> validateAvatarColor(Validator validator, String avatarColor) {
        PostForm form = new PostForm();
        form.setAuthor("alice");
        form.setBody("hello");
        form.setAvatarColor(avatarColor);
        return validator.validateProperty(form, "avatarColor");
    }
}
