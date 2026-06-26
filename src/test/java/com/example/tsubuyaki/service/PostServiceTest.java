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
import static org.mockito.ArgumentMatchers.any;
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
    void latest_returnsRepositoryResult() {
        List<Post> repositoryResult = List.of(
                new Post("alice", "最新投稿", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(repositoryResult);

        List<Post> actual = postService.latest();

        assertThat(actual).isSameAs(repositoryResult);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿登録_create_投稿者と本文をPostとして保存する")
    void create_savesPostWithAuthorAndBody() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        Post created = postService.create("alice", "投稿テスト");

        assertThat(created.getAuthor()).isEqualTo("alice");
        assertThat(created.getBody()).isEqualTo("投稿テスト");
        assertThat(created.getCreatedAt()).isNotNull();
        verify(postRepository).save(any(Post.class));
    }
}
