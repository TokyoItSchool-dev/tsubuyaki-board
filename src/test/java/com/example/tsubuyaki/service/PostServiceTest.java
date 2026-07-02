package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    private PostService postService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-05-23T10:00:00Z"), ZoneOffset.UTC);
        this.postService = new PostService(postRepository, clock);
    }

    @Test
    @DisplayName("投稿一覧_latest_Repositoryの新着50件を返す")
    void latest_returnsRepositoryLatest50Posts() {
        List<Post> posts = List.of(
                new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> actual = postService.latest();

        assertThat(actual).isSameAs(posts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿詳細_findById_Repositoryの検索結果を返す")
    void findById_returnsRepositoryResult() {
        Post post = new Post(1L, "alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Optional<Post> actual = postService.findById(1L);

        assertThat(actual).containsSame(post);
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("投稿作成_create_最大IDの次のIDと現在日時を設定してRepositoryに保存する")
    void create_savesPostWithNextIdAndCurrentTimestamp() {
        given(postRepository.findMaxId()).willReturn(10L);

        postService.create("alice", "hello");

        var captor = forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(11L);
        assertThat(captor.getValue().getAuthor()).isEqualTo("alice");
        assertThat(captor.getValue().getBody()).isEqualTo("hello");
        assertThat(captor.getValue().getCreatedAt()).isEqualTo(Instant.parse("2026-05-23T10:00:00Z"));
    }
}
