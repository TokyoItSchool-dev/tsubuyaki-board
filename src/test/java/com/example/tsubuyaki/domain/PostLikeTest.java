package com.example.tsubuyaki.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PostLikeTest {

    @Test
    @DisplayName("いいねEntity_生成したとき_値を取得できる")
    void いいねEntity_生成したとき_値を取得できる() {
        Instant createdAt = Instant.parse("2026-05-23T10:01:00Z");
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        PostLike postLike = new PostLike(post, "abcd1234", createdAt);
        ReflectionTestUtils.setField(postLike, "id", 10L);

        assertThat(postLike.getId()).isEqualTo(10L);
        assertThat(postLike.getPost()).isSameAs(post);
        assertThat(postLike.getClientHash()).isEqualTo("abcd1234");
        assertThat(postLike.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("いいねEntity_equals_IDが同じとき_同一とみなす")
    void いいねEntity_equals_IDが同じとき_同一とみなす() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        PostLike first = new PostLike(post, "abcd1234", Instant.parse("2026-05-23T10:01:00Z"));
        PostLike second = new PostLike(post, "efgh5678", Instant.parse("2026-05-23T10:02:00Z"));
        ReflectionTestUtils.setField(first, "id", 10L);
        ReflectionTestUtils.setField(second, "id", 10L);

        assertThat(first).isEqualTo(first);
        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
    }

    @Test
    @DisplayName("いいねEntity_equals_型が違うとき_同一とみなさない")
    void いいねEntity_equals_型が違うとき_同一とみなさない() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        PostLike postLike = new PostLike(post, "abcd1234", Instant.parse("2026-05-23T10:01:00Z"));

        assertThat(postLike).isNotEqualTo("abcd1234");
    }
}
