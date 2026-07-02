package com.example.tsubuyaki.service;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(Long id) {
        super("Post not found: id=" + id);
    }
}
