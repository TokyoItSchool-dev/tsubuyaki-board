package com.example.tsubuyaki.service.dto;

import java.time.Instant;
import java.util.List;

public record PostApiResponse(
        Long id,
        String author,
        String body,
        String avatarColor,
        Instant createdAt,
        Instant updatedAt,
        List<String> tags,
        long likesCount) {
}
