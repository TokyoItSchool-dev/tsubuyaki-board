package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;

import java.util.List;

public final class PostEntityMapper {

    private PostEntityMapper() {
    }

    public static Post toDomain(PostEntity entity) {
        return new Post(
                entity.getId(),
                entity.getAuthor(),
                entity.getAvatarColor(),
                entity.getBody(),
                entity.getCreatedAt(),
                entity.getTags().stream()
                        .map(TagEntity::getName)
                        .toList());
    }

    public static PostEntity toEntity(Post post) {
        return new PostEntity(
                post.getId(),
                post.getAuthor(),
                post.getAvatarColor(),
                post.getBody(),
                post.getCreatedAt(),
                List.of());
    }

    public static PostEntity toEntity(Post post, List<TagEntity> tags) {
        return new PostEntity(
                post.getId(),
                post.getAuthor(),
                post.getAvatarColor(),
                post.getBody(),
                post.getCreatedAt(),
                tags);
    }

}
