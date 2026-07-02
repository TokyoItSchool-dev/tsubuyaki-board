package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PostEntityMapperTest {

    @Test
    @DisplayName("EntityMapper_EntityからDomainへ_同じ値を移す")
    void EntityMapper_EntityからDomainへ_同じ値を移す() {
        PostEntity entity = new PostEntity(
                1L,
                "alice",
                "PURPLE",
                "hello",
                Instant.parse("2026-06-26T09:00:00Z"),
                List.of(new TagEntity(1L, "Java")));

        Post post = PostEntityMapper.toDomain(entity);

        assertThat(post.getId()).isEqualTo(1L);
        assertThat(post.getAuthor()).isEqualTo("alice");
        assertThat(post.getAvatarColor()).isEqualTo("PURPLE");
        assertThat(post.getBody()).isEqualTo("hello");
        assertThat(post.getCreatedAt()).isEqualTo(Instant.parse("2026-06-26T09:00:00Z"));
        assertThat(post.getTagNames()).containsExactly("Java");
    }

    @Test
    @DisplayName("EntityMapper_DomainからEntityへ_同じ値を移す")
    void EntityMapper_DomainからEntityへ_同じ値を移す() {
        Post post = new Post(1L, "alice", "ORANGE", "hello", Instant.parse("2026-06-26T09:00:00Z"));

        PostEntity entity = PostEntityMapper.toEntity(post);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getAuthor()).isEqualTo("alice");
        assertThat(entity.getAvatarColor()).isEqualTo("ORANGE");
        assertThat(entity.getBody()).isEqualTo("hello");
        assertThat(entity.getCreatedAt()).isEqualTo(Instant.parse("2026-06-26T09:00:00Z"));
    }

    @Test
    @DisplayName("EntityMapper_DomainからEntityへ_渡されたタグEntityを関連付ける")
    void EntityMapper_DomainからEntityへ_渡されたタグEntityを関連付ける() {
        Post post = new Post(
                1L,
                "alice",
                "ORANGE",
                "hello #Java",
                Instant.parse("2026-06-26T09:00:00Z"),
                List.of("Java"));
        TagEntity java = new TagEntity(1L, "Java");

        PostEntity entity = PostEntityMapper.toEntity(post, List.of(java));

        assertThat(entity.getTags()).containsExactly(java);
    }
}
