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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_latest_Repositoryから新着50件を取得する")
    void 投稿一覧_latest_Repositoryから新着50件を取得する() {
        List<Post> expected = List.of(
                new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expected);

        List<Post> actual = postService.latest();

        assertThat(actual).isEqualTo(expected);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿作成_create_投稿者と本文を保存する")
    void 投稿作成_create_投稿者と本文を保存する() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        Post actual = postService.create("alice", "今日の共有です");

        assertThat(actual.getAuthor()).isEqualTo("alice");
        assertThat(actual.getBody()).isEqualTo("今日の共有です");
        assertThat(actual.getCreatedAt()).isNotNull();
        verify(postRepository).save(any(Post.class));
        verify(postRepository, never()).findTop50ByOrderByCreatedAtDesc();
    }
}
