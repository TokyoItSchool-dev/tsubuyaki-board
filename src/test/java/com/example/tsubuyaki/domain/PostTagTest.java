package com.example.tsubuyaki.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PostTagTest {

    @Test
    @DisplayName("PostTag_コンストラクタ_postとtagを保持しPostへ登録する")
    void PostTag_コンストラクタ_postとtagを保持しPostへ登録する() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        Tag tag = new Tag("研修");

        PostTag postTag = new PostTag(post, tag);

        assertThat(postTag.getId()).isNull();
        assertThat(postTag.getPost()).isSameAs(post);
        assertThat(postTag.getTag()).isSameAs(tag);
        assertThat(post.getTags()).containsExactly(tag);
    }

    @Test
    @DisplayName("PostTag_コンストラクタ_postがnullでも作成できる")
    void PostTag_コンストラクタ_postがNullでも作成できる() {
        Tag tag = new Tag("研修");

        PostTag postTag = new PostTag(null, tag);

        assertThat(postTag.getPost()).isNull();
        assertThat(postTag.getTag()).isSameAs(tag);
    }

    @Test
    @DisplayName("PostTag_JPA用コンストラクタ_空インスタンスを作成できる")
    void PostTag_JPA用コンストラクタ_空インスタンスを作成できる() {
        PostTag postTag = new PostTag();

        assertThat(postTag.getId()).isNull();
        assertThat(postTag.getPost()).isNull();
        assertThat(postTag.getTag()).isNull();
    }

    @Test
    @DisplayName("PostTag_equals_同じidなら等しい")
    void PostTag_equals_同じidなら等しい() {
        PostTag first = new PostTag(null, new Tag("java"));
        PostTag second = new PostTag(null, new Tag("spring"));
        ReflectionTestUtils.setField(first, "id", 1L);
        ReflectionTestUtils.setField(second, "id", 1L);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    @DisplayName("PostTag_equals_idが異なるまたはPostTag以外なら等しくない")
    void PostTag_equals_idが異なるまたはPostTag以外なら等しくない() {
        PostTag first = new PostTag(null, new Tag("java"));
        PostTag second = new PostTag(null, new Tag("java"));
        ReflectionTestUtils.setField(first, "id", 1L);
        ReflectionTestUtils.setField(second, "id", 2L);

        assertThat(first).isEqualTo(first);
        assertThat(first).isNotEqualTo(second);
        assertThat(first).isNotEqualTo("java");
    }
}
