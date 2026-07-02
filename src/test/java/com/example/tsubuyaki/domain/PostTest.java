package com.example.tsubuyaki.domain;

import jakarta.persistence.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PostTest {

    @Test
    @DisplayName("同一性_未永続の投稿同士_idがnullでも等価にしない")
    void 同一性_未永続の投稿同士_idがnullでも等価にしない() {
        Post first = new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z"));
        Post second = new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z"));

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("投稿作成_前後空白_正規化して保持する")
    void 投稿作成_前後空白_正規化して保持する() {
        Post post = new Post(" alice ", " hello ", Instant.parse("2026-06-26T09:00:00Z"));

        assertThat(post.getAuthor()).isEqualTo("alice");
        assertThat(post.getBody()).isEqualTo("hello");
    }

    @Test
    @DisplayName("表示用本文_本文にタグがあるとき_タグ文字列を除去する")
    void 表示用本文_本文にタグがあるとき_タグ文字列を除去する() {
        Post post = new Post("alice", """
                今日の共有です #java #spring
                #社内勉強会
                次の行です
                """, Instant.parse("2026-06-26T09:00:00Z"));

        assertThat(post.getBody()).contains("#java", "#spring", "#社内勉強会");
        assertThat(post.getDisplayBody()).isEqualTo("""
                今日の共有です
                次の行です""");
    }

    @Test
    @DisplayName("責務分離_Domain_PostはJPA Entityではない")
    void 責務分離_Domain_PostはJPA_Entityではない() {
        assertThat(Post.class.getAnnotation(Entity.class)).isNull();
    }

    @Test
    @DisplayName("セキュリティ_Postは継承できない")
    void セキュリティ_Postは継承できない() {
        assertThat(Modifier.isFinal(Post.class.getModifiers())).isTrue();
    }
}
