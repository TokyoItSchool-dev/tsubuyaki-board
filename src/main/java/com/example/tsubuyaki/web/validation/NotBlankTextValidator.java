package com.example.tsubuyaki.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotBlankTextValidator implements ConstraintValidator<NotBlankText, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && value.codePoints().anyMatch(codePoint -> !isBlankCodePoint(codePoint));
    }

    private static boolean isBlankCodePoint(int codePoint) {
        return Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint);
    }
}
