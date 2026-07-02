package com.example.tsubuyaki.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotBlankTextValidator implements ConstraintValidator<NotBlankText, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && value.codePoints().anyMatch(codePoint -> !Character.isWhitespace(codePoint));
    }
}
