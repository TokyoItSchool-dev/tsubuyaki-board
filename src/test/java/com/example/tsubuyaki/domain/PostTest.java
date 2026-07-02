package com.example.tsubuyaki.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PostTest {

    @Test
    @DisplayName("投稿日時_OracleのTIMESTAMPに合わせてタイムゾーンなしの型で保持する")
    void 投稿日時_OracleのTIMESTAMPに合わせてタイムゾーンなしの型で保持する() throws Exception {
        assertThat(Post.class.getDeclaredField("createdAt").getType()).isEqualTo(LocalDateTime.class);
        assertThat(Post.class.getDeclaredField("deletedAt").getType()).isEqualTo(LocalDateTime.class);
    }

    @Test
    @DisplayName("投稿日時_公開APIではUTCのInstantとして扱える")
    void 投稿日時_公開APIではUTCのInstantとして扱える() {
        Instant createdAt = Instant.parse("2026-07-01T07:22:13.665Z");
        Instant deletedAt = Instant.parse("2026-07-01T08:22:13.665Z");
        Post post = new Post("alice", "本文", createdAt);

        post.markDeleted(deletedAt);

        assertThat(post.getCreatedAt()).isEqualTo(createdAt);
        assertThat(post.getDeletedAt()).isEqualTo(deletedAt);
    }
}
