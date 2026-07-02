package com.example.tsubuyaki.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PostTest {

    @Test
    @DisplayName("Post_生成したとき_投稿者_本文_アバター色_投稿日を取得できる")
    void Post_生成したとき_投稿者_本文_アバター色_投稿日を取得できる() {
        LocalDateTime createdAt = LocalDateTime.parse("2026-05-23T10:00:00");
        Post post = new Post("alice", "hello", "#3366cc", createdAt);

        assertThat(post.getAuthor()).isEqualTo("alice");
        assertThat(post.getBody()).isEqualTo("hello");
        assertThat(post.getAvatarColor()).isEqualTo("#3366cc");
        assertThat(post.getCreatedAt()).isEqualTo(createdAt);
        assertThat(post.getId()).isNull();
    }

    @Test
    @DisplayName("Post_アバター色未指定で生成したとき_デフォルト色を取得できる")
    void Post_アバター色未指定で生成したとき_デフォルト色を取得できる() {
        Post post = new Post("alice", "hello", LocalDateTime.parse("2026-05-23T10:00:00"));

        assertThat(post.getAvatarColor()).isEqualTo("#cccccc");
    }

    @Test
    @DisplayName("Post_equals_IDが未採番の別インスタンス同士_同じ値として扱う")
    void Post_equals_IDが未採番の別インスタンス同士_同じ値として扱う() {
        LocalDateTime createdAt = LocalDateTime.parse("2026-05-23T10:00:00");
        Post post = new Post("alice", "hello", createdAt);
        Post other = new Post("alice", "hello", createdAt);

        assertThat(post)
                .isEqualTo(post)
                .isEqualTo(other)
                .isNotEqualTo("post");
        assertThat(post.hashCode()).isEqualTo(other.hashCode());
    }
}
