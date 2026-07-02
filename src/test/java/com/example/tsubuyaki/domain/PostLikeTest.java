package com.example.tsubuyaki.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PostLikeTest {

    @Test
    @DisplayName("PostLike_生成時_投稿clientHash作成日時を保持する")
    void constructor_keepsValues() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        Instant createdAt = Instant.parse("2026-05-23T11:00:00Z");

        PostLike like = new PostLike(post, "abcd1234", createdAt);

        assertThat(like.getId()).isNull();
        assertThat(like.getPost()).isSameAs(post);
        assertThat(like.getClientHash()).isEqualTo("abcd1234");
        assertThat(like.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("PostLike_equals_ID未採番同士_別いいねとして扱う")
    void equals_withNullIds_returnsFalse() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        PostLike like = new PostLike(post, "abcd1234", Instant.parse("2026-05-23T11:00:00Z"));
        PostLike other = new PostLike(post, "efgh5678", Instant.parse("2026-05-23T12:00:00Z"));

        assertThat(like).isEqualTo(like);
        assertThat(like).isNotEqualTo(other);
        assertThat(like).isNotEqualTo("postLike");
    }

    @Test
    @DisplayName("PostLike_equals_片方だけID採番済み_別いいねとして扱う")
    void equals_whenEitherIdIsNull_returnsFalse() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        PostLike assignedId = new PostLike(post, "abcd1234", Instant.parse("2026-05-23T11:00:00Z"));
        PostLike nullId = new PostLike(post, "efgh5678", Instant.parse("2026-05-23T12:00:00Z"));
        ReflectionTestUtils.setField(assignedId, "id", 1L);

        assertThat(assignedId).isNotEqualTo(nullId);
        assertThat(nullId).isNotEqualTo(assignedId);
    }

    @Test
    @DisplayName("PostLike_equals_ID採番済み同士_IDで同値判定する")
    void equals_withIds_comparesId() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        PostLike like = new PostLike(post, "abcd1234", Instant.parse("2026-05-23T11:00:00Z"));
        PostLike sameId = new PostLike(post, "efgh5678", Instant.parse("2026-05-23T12:00:00Z"));
        PostLike otherId = new PostLike(post, "ijkl9012", Instant.parse("2026-05-23T13:00:00Z"));
        ReflectionTestUtils.setField(like, "id", 1L);
        ReflectionTestUtils.setField(sameId, "id", 1L);
        ReflectionTestUtils.setField(otherId, "id", 2L);

        assertThat(like).isEqualTo(sameId);
        assertThat(like).isNotEqualTo(otherId);
        assertThat(like).hasSameHashCodeAs(sameId);
    }
}
