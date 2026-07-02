package com.example.tsubuyaki.web.dto;

import com.example.tsubuyaki.domain.Tag;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "投稿タグレスポンス")
public record ApiTagResponse(
        @Schema(description = "タグID", example = "10")
        Long id,

        @Schema(description = "タグ名", example = "java")
        String name) {

    public static ApiTagResponse from(Tag tag) {
        return new ApiTagResponse(tag.getId(), tag.getName());
    }
}
