package com.example.tsubuyaki.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PostTest {

    @Test
    @DisplayName("Post_生成時_投稿者内容投稿日アバター色を保持する")
    void constructor_keepsValues() {
        Instant createdAt = Instant.parse("2026-05-23T10:00:00Z");

        Post post = new Post("alice", "hello", "purple", createdAt);

        assertThat(post.getId()).isNull();
        assertThat(post.getAuthor()).isEqualTo("alice");
        assertThat(post.getBody()).isEqualTo("hello");
        assertThat(post.getAvatarColor()).isEqualTo("purple");
        assertThat(post.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Post_equals_ID未採番同士_別投稿として扱う")
    void equals_withNullIds_returnsFalse() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        Post other = new Post("bob", "good morning", Instant.parse("2026-05-24T10:00:00Z"));

        assertThat(post).isEqualTo(post);
        assertThat(post).isNotEqualTo(other);
        assertThat(post).isNotEqualTo("post");
    }

    @Test
    @DisplayName("Post_equals_ID採番済み同士_IDで同値判定する")
    void equals_withIds_comparesId() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        Post sameId = new Post("bob", "good morning", Instant.parse("2026-05-24T10:00:00Z"));
        Post otherId = new Post("carol", "good evening", Instant.parse("2026-05-25T10:00:00Z"));
        ReflectionTestUtils.setField(post, "id", 1L);
        ReflectionTestUtils.setField(sameId, "id", 1L);
        ReflectionTestUtils.setField(otherId, "id", 2L);

        assertThat(post).isEqualTo(sameId);
        assertThat(post).isNotEqualTo(otherId);
        assertThat(post).hasSameHashCodeAs(sameId);
    }

    @Test
    @DisplayName("Post_更新時_投稿者と本文とアバター色を変更し投稿日を維持する")
    void update_changesAuthorBodyAndAvatarColorOnly() {
        Instant createdAt = Instant.parse("2026-05-23T10:00:00Z");
        Post post = new Post("alice", "hello", "blue", createdAt);

        post.update("bob", "更新後本文です", "green");

        assertThat(post.getAuthor()).isEqualTo("bob");
        assertThat(post.getBody()).isEqualTo("更新後本文です");
        assertThat(post.getAvatarColor()).isEqualTo("green");
        assertThat(post.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("Post_削除時_deletedAtに削除日時を保持する")
    void delete_setsDeletedAt() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        Instant deletedAt = Instant.parse("2026-05-24T10:00:00Z");

        post.delete(deletedAt);

        assertThat(post.getDeletedAt()).isEqualTo(deletedAt);
    }
}
