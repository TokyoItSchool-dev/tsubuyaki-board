package com.example.tsubuyaki.web.dto;

import com.example.tsubuyaki.domain.Post;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Schema(description = "投稿一覧 API の投稿レスポンス")
public record ApiPostResponse(
        @Schema(description = "投稿ID", example = "1")
        Long id,

        @Schema(description = "投稿者名", example = "alice")
        String author,

        @Schema(description = "本文", example = "#java の投稿")
        String body,

        @Schema(description = "アバター色", example = "green")
        String avatarColor,

        @Schema(description = "作成日時", example = "2026-06-26T10:00:00Z")
        Instant createdAt,

        @Schema(description = "タグ一覧")
        List<ApiTagResponse> tags) {

    public static ApiPostResponse from(Post post) {
        List<ApiTagResponse> tagResponses = post.getTags().stream()
                .sorted(Comparator.comparing(com.example.tsubuyaki.domain.Tag::getName))
                .map(ApiTagResponse::from)
                .toList();
        return new ApiPostResponse(
                post.getId(),
                post.getAuthor(),
                post.getBody(),
                post.getAvatarColor(),
                post.getCreatedAt(),
                tagResponses);
    }
}
