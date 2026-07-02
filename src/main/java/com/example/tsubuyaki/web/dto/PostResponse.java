package com.example.tsubuyaki.web.dto;

import com.example.tsubuyaki.domain.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String author,
        String body,
        LocalDateTime createdAt) {

    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getAuthor(),
                post.getBody(),
                post.getCreatedAt());
    }
}
