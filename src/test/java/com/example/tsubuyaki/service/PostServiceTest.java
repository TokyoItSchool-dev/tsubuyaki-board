package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PostServiceTest {

    private final PostRepository postRepository = mock(PostRepository.class);

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T05:00:00Z"), ZoneOffset.UTC);

    private final PostService postService = new PostService(postRepository, clock);

    @Test
    @DisplayName("投稿一覧_latest_Repositoryから新着50件を取得して返す")
    void 投稿一覧_latest_Repositoryから新着50件を取得して返す() {
        List<Post> expected = List.of(
                new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expected);

        List<Post> actual = postService.latest();

        assertThat(actual).isSameAs(expected);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿登録_create_投稿者本文現在時刻を保存する")
    void 投稿登録_create_投稿者本文現在時刻を保存する() {
        postService.create("alice", "こんにちは");

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getAuthor()).isEqualTo("alice");
        assertThat(captor.getValue().getBody()).isEqualTo("こんにちは");
        assertThat(captor.getValue().getCreatedAt()).isEqualTo(Instant.parse("2026-07-01T05:00:00Z"));
    }
    @Test
    @DisplayName("投稿詳細_findById_Repositoryから指定idの投稿を取得する")
    void 投稿詳細_findById_Repositoryから指定idの投稿を取得する() {
        Post expected = new Post("alice", "詳細本文", Instant.parse("2026-07-01T05:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(expected));

        Optional<Post> actual = postService.findById(1L);

        assertThat(actual).containsSame(expected);
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("キーワード検索_search_本文部分一致の新着50件をRepositoryから取得する")
    void キーワード検索_search_本文部分一致の新着50件をRepositoryから取得する() {
        List<Post> expected = List.of(
                new Post("alice", "Spring Boot のメモ", Instant.parse("2026-07-01T05:00:00Z")));
        given(postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("Spring"))
                .willReturn(expected);

        List<Post> actual = postService.search("Spring");

        assertThat(actual).isSameAs(expected);
        verify(postRepository).findTop50ByBodyContainingOrderByCreatedAtDesc("Spring");
    }

    @Test
    @DisplayName("投稿登録_create_avatarColorを指定するとその値を保存する")
    void 投稿登録_create_avatarColorを指定するとその値を保存する() {
        postService.create("alice", "こんにちは", "orange");

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getAvatarColor()).isEqualTo("orange");
    }

    @Test
    @DisplayName("投稿登録_create_avatarColor未指定ならデフォルト色を保存する")
    void 投稿登録_create_avatarColor未指定ならデフォルト色を保存する() {
        postService.create("alice", "こんにちは", "");

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getAvatarColor()).isEqualTo("gray");
    }
}
