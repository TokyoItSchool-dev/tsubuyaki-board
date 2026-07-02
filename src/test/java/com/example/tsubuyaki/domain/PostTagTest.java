package com.example.tsubuyaki.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PostTagTest {

    @Test
    @DisplayName("PostTag_生成時_投稿とタグを保持する")
    void constructor_keepsValues() {
        Post post = new Post("alice", "#java 本文です", Instant.parse("2026-05-23T10:00:00Z"));
        Tag tag = new Tag("java");

        PostTag postTag = new PostTag(post, tag);

        assertThat(postTag.getId()).isNull();
        assertThat(postTag.getPost()).isSameAs(post);
        assertThat(postTag.getTag()).isSameAs(tag);
    }

    @Test
    @DisplayName("PostTag_equals_同一インスタンス_trueを返す")
    void equals_whenSameInstance_returnsTrue() {
        PostTag postTag = newPostTag("java");

        assertThat(postTag).isEqualTo(postTag);
    }

    @Test
    @DisplayName("PostTag_equals_型が異なる場合_falseを返す")
    void equals_whenOtherType_returnsFalse() {
        PostTag postTag = newPostTag("java");

        assertThat(postTag).isNotEqualTo("postTag");
    }

    @Test
    @DisplayName("PostTag_equals_ID未採番同士_別タグ紐付けとして扱う")
    void equals_whenBothIdsAreNull_returnsFalse() {
        PostTag postTag = newPostTag("java");
        PostTag other = newPostTag("spring");

        assertThat(postTag).isNotEqualTo(other);
    }

    @Test
    @DisplayName("PostTag_equals_片方だけID採番済み_別タグ紐付けとして扱う")
    void equals_whenEitherIdIsNull_returnsFalse() {
        PostTag assignedId = newPostTag("java");
        PostTag nullId = newPostTag("spring");
        ReflectionTestUtils.setField(assignedId, "id", 1L);

        assertThat(assignedId).isNotEqualTo(nullId);
        assertThat(nullId).isNotEqualTo(assignedId);
    }

    @Test
    @DisplayName("PostTag_equals_ID採番済み同士_IDで同値判定する")
    void equals_whenBothIdsAreAssigned_comparesId() {
        PostTag postTag = newPostTag("java");
        PostTag sameId = newPostTag("spring");
        PostTag otherId = newPostTag("oracle");
        ReflectionTestUtils.setField(postTag, "id", 1L);
        ReflectionTestUtils.setField(sameId, "id", 1L);
        ReflectionTestUtils.setField(otherId, "id", 2L);

        assertThat(postTag).isEqualTo(sameId);
        assertThat(postTag).isNotEqualTo(otherId);
        assertThat(postTag).hasSameHashCodeAs(sameId);
    }

    private PostTag newPostTag(String tagName) {
        Post post = new Post("alice", "#" + tagName + " 本文です", Instant.parse("2026-05-23T10:00:00Z"));
        return new PostTag(post, new Tag(tagName));
    }
}
