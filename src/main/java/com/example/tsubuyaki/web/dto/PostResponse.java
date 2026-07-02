package com.example.tsubuyaki.web.dto;

import com.example.tsubuyaki.domain.AvatarColor;
import com.example.tsubuyaki.domain.HashtagText;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

public record PostResponse(
        Long id,
        String author,
        String avatarColor,
        String body,
        String displayBody,
        Instant createdAt,
        List<String> tagNames) {

    public PostResponse(Long id, String author, String body, Instant createdAt) {
        this(id, author, AvatarColor.DEFAULT.name(), body, createdAt, List.of());
    }

    public PostResponse(Long id, String author, String avatarColor, String body, Instant createdAt) {
        this(id, author, avatarColor, body, createdAt, List.of());
    }

    public PostResponse(
            Long id,
            String author,
            String avatarColor,
            String body,
            Instant createdAt,
            List<String> tagNames) {
        this(id, author, avatarColor, body, HashtagText.removeTags(body), createdAt, tagNames);
    }

    public PostResponse {
        displayBody = displayBody == null ? "" : displayBody;
        tagNames = List.copyOf(tagNames);
    }

    public String avatarColorCssClass() {
        return "post__avatar-color--" + AvatarColor.from(avatarColor).name().toLowerCase(Locale.ROOT);
    }
}
