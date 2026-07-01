package com.example.tsubuyaki.service;

public class PostRegistrationException extends RuntimeException {

    public PostRegistrationException(String message) {
        super(message);
    }

    public PostRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
