package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_latest_Repositoryの新着50件を返す")
    void 投稿一覧_latest_Repositoryの新着50件を返す() {
        List<Post> posts = List.of(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> latestPosts = postService.latest();

        assertThat(latestPosts).isSameAs(posts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿詳細_findById_Repositoryの検索結果を返す")
    void 投稿詳細_findById_Repositoryの検索結果を返す() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findById(10L)).willReturn(Optional.of(post));

        Optional<Post> actual = postService.findById(10L);

        assertThat(actual).containsSame(post);
        verify(postRepository).findById(10L);
    }

    @Test
    @DisplayName("投稿詳細_findById_存在しないとき空を返す")
    void 投稿詳細_findById_存在しないとき空を返す() {
        given(postRepository.findById(404L)).willReturn(Optional.empty());

        Optional<Post> actual = postService.findById(404L);

        assertThat(actual).isEmpty();
        verify(postRepository).findById(404L);
    }
}
