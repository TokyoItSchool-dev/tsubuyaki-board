package com.example.tsubuyaki.domain;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PostTest {

    @Test
    @DisplayName("投稿カラー_color_指定した6桁の色コードを保持する")
    void 投稿カラー_color_指定した6桁の色コードを保持する() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"), "E0F2FE");

        assertThat(post.getColor()).isEqualTo("E0F2FE");
    }

    @Test
    @DisplayName("投稿削除_markDeleted_deletedAtを保持する")
    void 投稿削除_markDeleted_deletedAtを保持する() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"),
                "E0F2FE", "abc12345");

        post.markDeleted(Instant.parse("2026-05-23T11:00:00Z"));

        assertThat(post.getClientHash()).isEqualTo("abc12345");
        assertThat(post.getDeletedAt()).isEqualTo(Instant.parse("2026-05-23T11:00:00Z"));
    }

    @Test
    @DisplayName("投稿日時_createdAt_DBのTIMESTAMPとして読み書きする")
    void 投稿日時_createdAt_DBのTIMESTAMPとして読み書きする() throws NoSuchFieldException {
        JdbcTypeCode jdbcTypeCode = Post.class
                .getDeclaredField("createdAt")
                .getAnnotation(JdbcTypeCode.class);

        assertThat(jdbcTypeCode).isNotNull();
        assertThat(jdbcTypeCode.value()).isEqualTo(SqlTypes.TIMESTAMP);
    }
}
