package com.example.tsubuyaki.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotBlankTextValidator.class)
public @interface NotBlankText {

    String message() default "入力してください";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
