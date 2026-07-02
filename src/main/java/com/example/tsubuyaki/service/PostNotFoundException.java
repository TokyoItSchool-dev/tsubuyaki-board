package com.example.tsubuyaki.service;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException(Long postId) {
        super("Post not found: " + postId);
    }
}
