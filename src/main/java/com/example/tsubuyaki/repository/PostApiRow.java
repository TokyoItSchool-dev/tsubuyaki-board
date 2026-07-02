package com.example.tsubuyaki.repository;

import java.time.Instant;

public record PostApiRow(
        Long id,
        String author,
        String body,
        String avatarColor,
        Instant createdAt,
        Instant updatedAt,
        String tagName,
        long likesCount) {
}
