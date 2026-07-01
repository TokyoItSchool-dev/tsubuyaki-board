package com.example.tsubuyaki.web.dto;

import java.time.LocalDateTime;

public class PostView {

    private final Long id;
    private final String author;
    private final String body;
    private final LocalDateTime createdAt;
    private final long likeCount;

    public PostView(Long id, String author, String body, LocalDateTime createdAt, long likeCount) {
        this.id = id;
        this.author = author;
        this.body = body;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public long getLikeCount() {
        return likeCount;
    }
}
