package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    private PostService postService;

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T04:30:00Z"), ZoneOffset.UTC);

    @Test
    @DisplayName("投稿一覧_latest_Repositoryの新着50件を返す")
    void 投稿一覧_latest_Repositoryの新着50件を返す() {
        postService = new PostService(postRepository, clock);
        List<Post> expected = List.of(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expected);

        List<Post> actual = postService.latest();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("投稿検索_search_Repositoryの本文検索結果を返す")
    void 投稿検索_search_Repositoryの本文検索結果を返す() {
        postService = new PostService(postRepository, clock);
        List<Post> expected = List.of(new Post("alice", "AI研修", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("AI")).willReturn(expected);

        List<Post> actual = postService.search("AI");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("投稿検索_search_キーワードが空白の場合_新着50件を返す")
    void 投稿検索_search_キーワードが空白の場合_新着50件を返す() {
        postService = new PostService(postRepository, clock);
        List<Post> expected = List.of(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expected);

        List<Post> actual = postService.search("  ");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("投稿詳細_findById_Repositoryの投稿を返す")
    void 投稿詳細_findById_Repositoryの投稿を返す() {
        postService = new PostService(postRepository, clock);
        Post expected = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(expected));

        Post actual = postService.findById(1L);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("新規投稿_create_投稿者本文現在日時を保存する")
    void 新規投稿_create_投稿者本文現在日時を保存する() {
        postService = new PostService(postRepository, clock);

        postService.create("alice", "本文です");

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(captor.capture());
        Post saved = captor.getValue();
        assertThat(saved.getAuthor()).isEqualTo("alice");
        assertThat(saved.getBody()).isEqualTo("本文です");
        assertThat(saved.getCreatedAt()).isEqualTo(Instant.parse("2026-07-01T04:30:00Z"));
    }
}
