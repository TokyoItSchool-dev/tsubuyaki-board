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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_最新投稿取得_Repositoryの新着50件取得結果を返す")
    void latest_returnsLatestFiftyFromRepository() {
        List<Post> latestPosts = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(latestPosts);

        List<Post> actual = postService.latest();

        assertThat(actual).isEqualTo(latestPosts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }
}
