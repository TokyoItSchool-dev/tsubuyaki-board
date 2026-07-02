package com.example.tsubuyaki.web.dto;

import com.example.tsubuyaki.domain.Post;

import java.time.Instant;

public record PostDto(
        Long id,
        String author,
        String body,
        Instant createdAt) {

    public static PostDto from(Post post) {
        return new PostDto(post.getId(), post.getAuthor(), post.getBody(), post.getCreatedAt());
    }
}
