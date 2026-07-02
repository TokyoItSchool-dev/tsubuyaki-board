package com.example.tsubuyaki.web.dto;

public record PostDetailDto(
        PostDto post,
        long likeCount,
        boolean liked) {
}
