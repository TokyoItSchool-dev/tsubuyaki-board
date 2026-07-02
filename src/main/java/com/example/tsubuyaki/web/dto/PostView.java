package com.example.tsubuyaki.web.dto;

import com.example.tsubuyaki.domain.PostBackgroundColor;

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

    private PostView(Builder builder) {
        this.id = builder.id;
        this.author = builder.author;
        this.body = builder.body;
        this.createdAt = builder.createdAt;
        this.likeCount = builder.likeCount;
        this.backgroundColor = PostBackgroundColor.normalize(builder.backgroundColor);
        this.canDelete = builder.canDelete;
        this.canEdit = builder.canEdit;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder(Long id, String author, String body, LocalDateTime createdAt) {
        return new Builder(id, author, body, createdAt);
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

    public String getBackgroundColorClass() {
        return PostBackgroundColor.cssClass(backgroundColor);
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

    public static final class Builder {

        private final Long id;
        private final String author;
        private final String body;
        private final LocalDateTime createdAt;
        private long likeCount;
        private String backgroundColor = PostBackgroundColor.DEFAULT;
        private boolean canDelete;
        private boolean canEdit;
        private LocalDateTime updatedAt;

        private Builder(Long id, String author, String body, LocalDateTime createdAt) {
            this.id = id;
            this.author = author;
            this.body = body;
            this.createdAt = createdAt;
        }

        public Builder likeCount(long likeCount) {
            this.likeCount = likeCount;
            return this;
        }

        public Builder backgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder canDelete(boolean canDelete) {
            this.canDelete = canDelete;
            return this;
        }

        public Builder canEdit(boolean canEdit) {
            this.canEdit = canEdit;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public PostView build() {
            return new PostView(this);
        }
    }
}
