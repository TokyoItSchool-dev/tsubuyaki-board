package com.example.tsubuyaki.web.mapper;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.web.dto.PostResponse;

import java.util.List;

public final class PostMapper {

    private PostMapper() {
    }

    public static PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getAuthor(),
                post.getAvatarColor(),
                post.getBody(),
                post.getDisplayBody(),
                post.getCreatedAt(),
                post.getTagNames());
    }

    public static List<PostResponse> toResponseList(List<Post> posts) {
        return posts.stream()
                .map(PostMapper::toResponse)
                .toList();
    }
}
