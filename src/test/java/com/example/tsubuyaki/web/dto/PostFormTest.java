package com.example.tsubuyaki.web.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostFormTest {

    @Test
    @DisplayName("PostForm_投稿者と本文を設定したとき_getterで同じ値を返す")
    void PostForm_投稿者と本文を設定したとき_getterで同じ値を返す() {
        PostForm form = new PostForm();

        form.setAuthor("alice");
        form.setBody("本文です");

        assertThat(form.getAuthor()).isEqualTo("alice");
        assertThat(form.getBody()).isEqualTo("本文です");
    }

    @Test
    @DisplayName("PostForm_空白のみの入力_各項目の入力必須エラーは1件だけ返す")
    void PostForm_空白のみの入力_各項目の入力必須エラーは1件だけ返す() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            PostForm halfWidthSpaces = new PostForm();
            halfWidthSpaces.setAuthor("   ");
            halfWidthSpaces.setBody("   ");

            PostForm fullWidthSpaces = new PostForm();
            fullWidthSpaces.setAuthor("　　");
            fullWidthSpaces.setBody("　　");

            assertThat(validator.validate(halfWidthSpaces))
                    .filteredOn(violation -> "author".contentEquals(violation.getPropertyPath().toString()))
                    .hasSize(1);
            assertThat(validator.validate(halfWidthSpaces))
                    .filteredOn(violation -> "body".contentEquals(violation.getPropertyPath().toString()))
                    .hasSize(1);
            assertThat(validator.validate(fullWidthSpaces))
                    .filteredOn(violation -> "author".contentEquals(violation.getPropertyPath().toString()))
                    .hasSize(1);
            assertThat(validator.validate(fullWidthSpaces))
                    .filteredOn(violation -> "body".contentEquals(violation.getPropertyPath().toString()))
                    .hasSize(1);
        }
    }
}
