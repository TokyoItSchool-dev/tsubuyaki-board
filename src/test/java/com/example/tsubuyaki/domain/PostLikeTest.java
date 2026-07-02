package com.example.tsubuyaki.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PostLikeTest {

    @Test
    @DisplayName("PostLike_コンストラクタ_postとclientHashを保持しidはnullにする")
    void PostLike_コンストラクタ_postとclientHashを保持しidはNullにする() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));

        PostLike postLike = new PostLike(post, "a1b2c3d4");

        assertThat(postLike.getId()).isNull();
        assertThat(postLike.getPost()).isSameAs(post);
        assertThat(postLike.getClientHash()).isEqualTo("a1b2c3d4");
    }

    @Test
    @DisplayName("PostLike_JPA用コンストラクタ_空インスタンスを作成できる")
    void PostLike_JPA用コンストラクタ_空インスタンスを作成できる() {
        PostLike postLike = new PostLike();

        assertThat(postLike.getId()).isNull();
        assertThat(postLike.getPost()).isNull();
        assertThat(postLike.getClientHash()).isNull();
    }

    @Test
    @DisplayName("PostLike_getId_idを返す")
    void PostLike_getId_idを返す() {
        PostLike postLike = new PostLike(null, "a1b2c3d4");
        ReflectionTestUtils.setField(postLike, "id", 1L);

        assertThat(postLike.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("PostLike_equals_同じインスタンスなら等しい")
    void PostLike_equals_同じインスタンスなら等しい() {
        PostLike postLike = new PostLike(null, "a1b2c3d4");
        ReflectionTestUtils.setField(postLike, "id", 1L);

        assertThat(postLike).isEqualTo(postLike);
    }

    @Test
    @DisplayName("PostLike_equals_同じidなら等しくhashCodeも等しい")
    void PostLike_equals_同じidなら等しくhashCodeも等しい() {
        PostLike first = new PostLike(null, "a1b2c3d4");
        PostLike second = new PostLike(null, "e5f6a7b8");
        ReflectionTestUtils.setField(first, "id", 1L);
        ReflectionTestUtils.setField(second, "id", 1L);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    @DisplayName("PostLike_equals_idが異なるなら等しくない")
    void PostLike_equals_idが異なるなら等しくない() {
        PostLike first = new PostLike(null, "a1b2c3d4");
        PostLike second = new PostLike(null, "a1b2c3d4");
        ReflectionTestUtils.setField(first, "id", 1L);
        ReflectionTestUtils.setField(second, "id", 2L);

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("PostLike_equals_PostLike以外なら等しくない")
    void PostLike_equals_PostLike以外なら等しくない() {
        PostLike postLike = new PostLike(null, "a1b2c3d4");
        ReflectionTestUtils.setField(postLike, "id", 1L);

        assertThat(postLike).isNotEqualTo("a1b2c3d4");
    }

    @Test
    @DisplayName("PostLike_hashCode_idがnullでも計算できる")
    void PostLike_hashCode_idがNullでも計算できる() {
        PostLike postLike = new PostLike(null, "a1b2c3d4");

        assertThat(postLike.hashCode()).isEqualTo(new PostLike(null, "e5f6a7b8").hashCode());
    }
}
