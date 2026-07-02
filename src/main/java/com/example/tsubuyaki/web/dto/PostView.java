package com.example.tsubuyaki.web.dto;

import java.time.LocalDateTime;

public class PostView {

    private final Long id;
    private final String author;
    private final String body;
    private final LocalDateTime createdAt;
    private final long likeCount;
    private final String backgroundColor;
    private final boolean canDelete;
    private final boolean canEdit;
    private final LocalDateTime updatedAt;

    public PostView(
            Long id,
            String author,
            String body,
            LocalDateTime createdAt,
            long likeCount,
            String backgroundColor) {
        this(id, author, body, createdAt, likeCount, backgroundColor, false);
    }

    public PostView(
            Long id,
            String author,
            String body,
            LocalDateTime createdAt,
            long likeCount,
            String backgroundColor,
            boolean canDelete) {
        this(id, author, body, createdAt, likeCount, backgroundColor, canDelete, false, null);
    }

    public PostView(
            Long id,
            String author,
            String body,
            LocalDateTime createdAt,
            long likeCount,
            String backgroundColor,
            boolean canDelete,
            boolean canEdit,
            LocalDateTime updatedAt) {
        this.id = id;
        this.author = author;
        this.body = body;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.backgroundColor = backgroundColor;
        this.canDelete = canDelete;
        this.canEdit = canEdit;
        this.updatedAt = updatedAt;
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

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
