package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostLikeRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("Service_latest_Repositoryの最新50件を返す")
    void latest_呼び出されたとき_Repositoryの最新50件を返す() {
        List<Post> repositoryResult = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-06-26T10:00:00Z")),
                new Post("bob", "古い投稿", Instant.parse("2026-06-26T09:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(repositoryResult);

        List<Post> latestPosts = postService.latest();

        assertThat(latestPosts).isSameAs(repositoryResult);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Service_create_投稿者と本文_作成日時を付けて保存する")
    void create_投稿者と本文_作成日時を付けて保存する() {
        given(postRepository.save(any(Post.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        Post savedPost = postService.create("alice", "M3 の投稿");

        assertThat(savedPost.getAuthor()).isEqualTo("alice");
        assertThat(savedPost.getBody()).isEqualTo("M3 の投稿");
        assertThat(savedPost.getCreatedAt()).isNotNull();
        verify(postRepository).save(savedPost);
    }

    @Test
    @DisplayName("Service_findById_id指定でRepositoryの投稿を返す")
    void findById_id指定でRepositoryの投稿を返す() {
        Post post = new Post("alice", "M4 の詳細投稿", Instant.parse("2026-06-26T11:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Optional<Post> foundPost = postService.findById(1L);

        assertThat(foundPost).containsSame(post);
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("Service_findById_存在しないid_空を返す")
    void findById_存在しないid_空を返す() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        Optional<Post> foundPost = postService.findById(999L);

        assertThat(foundPost).isEmpty();
        verify(postRepository).findById(999L);
    }
}
