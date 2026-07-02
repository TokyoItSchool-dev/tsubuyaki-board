package com.example.tsubuyaki.web.dto;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostDetail;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "投稿一覧APIの投稿レスポンス")
public record ApiPostResponse(
        @Schema(description = "投稿ID", example = "1")
        Long id,

        @Schema(description = "投稿者名", example = "alice")
        String author,

        @Schema(description = "アバター色", example = "BLUE")
        String avatarColor,

        @Schema(description = "本文", example = "API の共有です")
        String body,

        @Schema(description = "投稿日時", example = "2026-06-26T09:00:00Z")
        Instant createdAt,

        @Schema(description = "いいね数", example = "3")
        long likeCount) {

    public static ApiPostResponse from(PostDetail detail) {
        Post post = detail.post();
        return new ApiPostResponse(
                post.getId(),
                post.getAuthor(),
                post.getAvatarColor(),
                post.getBody(),
                post.getCreatedAt(),
                detail.likeCount());
    }
}
