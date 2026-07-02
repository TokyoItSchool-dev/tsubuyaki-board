package com.example.tsubuyaki.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public final class Post {

    private static final int AUTHOR_MAX_LENGTH = 30;
    private static final int BODY_MAX_LENGTH = 280;

    private final Long id;

    private final String author;

    private final String avatarColor;

    private final String body;

    private final Instant createdAt;

    private final List<String> tagNames;

    public Post(String author, String body, Instant createdAt) {
        this(null, author, AvatarColor.DEFAULT.name(), body, createdAt);
    }

    public Post(String author, String avatarColor, String body, Instant createdAt) {
        this(null, author, avatarColor, body, createdAt);
    }

    public Post(Long id, String author, String body, Instant createdAt) {
        this(id, author, AvatarColor.DEFAULT.name(), body, createdAt);
    }

    public Post(Long id, String author, String avatarColor, String body, Instant createdAt) {
        this(id, author, avatarColor, body, createdAt, List.of());
    }

    public Post(Long id, String author, String avatarColor, String body, Instant createdAt, List<String> tagNames) {
        this.id = id;
        this.author = normalizeRequired(author, "author", AUTHOR_MAX_LENGTH);
        this.avatarColor = AvatarColor.from(avatarColor).name();
        this.body = normalizeRequired(body, "body", BODY_MAX_LENGTH);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.tagNames = List.copyOf(tagNames);
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public String getBody() {
        return body;
    }

    public String getDisplayBody() {
        return HashtagText.removeTags(body);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<String> getTagNames() {
        return tagNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Post other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    private static String normalizeRequired(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        String normalized = value.strip();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must be " + maxLength + " characters or less");
        }
        return normalized;
    }
}
