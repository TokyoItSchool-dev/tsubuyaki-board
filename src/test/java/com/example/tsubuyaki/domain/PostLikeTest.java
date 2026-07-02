package com.example.tsubuyaki.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PostLikeTest {

    @Test
    @DisplayName("PostLike_値を渡して生成_getterで同じ値を取得できる")
    void PostLike_値を渡して生成_getterで同じ値を取得できる() {
        Post post = new Post("alice", "本文", "#2563EB", Instant.parse("2026-05-23T10:00:00Z"));
        Instant createdAt = Instant.parse("2026-05-23T10:01:00Z");

        PostLike like = new PostLike(post, "abcdef12", createdAt);
        ReflectionTestUtils.setField(like, "id", 1L);

        assertThat(like.getId()).isEqualTo(1L);
        assertThat(like.getPost()).isSameAs(post);
        assertThat(like.getClientHash()).isEqualTo("abcdef12");
        assertThat(like.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("PostLike_JPAコンストラクタ_初期値はnullになる")
    void PostLike_JPAコンストラクタ_初期値はNullになる() {
        PostLike like = new PostLike();

        assertThat(like.getId()).isNull();
        assertThat(like.getPost()).isNull();
        assertThat(like.getClientHash()).isNull();
        assertThat(like.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("PostLike_equals_同じインスタンスならtrueを返す")
    void PostLike_equals_同じインスタンスならTrueを返す() {
        PostLike like = newPostLikeWithId(1L);

        assertThat(like.equals(like)).isTrue();
    }

    @Test
    @DisplayName("PostLike_equals_PostLike以外ならfalseを返す")
    void PostLike_equals_PostLike以外ならFalseを返す() {
        PostLike like = newPostLikeWithId(1L);

        assertThat(like).isNotEqualTo("not-post-like");
    }

    @Test
    @DisplayName("PostLike_equals_idが同じならtrueを返しhashCodeも一致する")
    void PostLike_equals_idが同じならTrueを返しHashCodeも一致する() {
        PostLike left = newPostLikeWithId(1L);
        PostLike right = newPostLikeWithId(1L);

        assertThat(left).isEqualTo(right);
        assertThat(left.hashCode()).isEqualTo(right.hashCode());
    }

    @Test
    @DisplayName("PostLike_equals_idが異なるならfalseを返す")
    void PostLike_equals_idが異なるならFalseを返す() {
        PostLike left = newPostLikeWithId(1L);
        PostLike right = newPostLikeWithId(2L);

        assertThat(left).isNotEqualTo(right);
    }

    private static PostLike newPostLikeWithId(Long id) {
        Post post = new Post("alice", "本文", "#2563EB", Instant.parse("2026-05-23T10:00:00Z"));
        PostLike like = new PostLike(post, "abcdef12", Instant.parse("2026-05-23T10:01:00Z"));
        ReflectionTestUtils.setField(like, "id", id);
        return like;
    }
}
