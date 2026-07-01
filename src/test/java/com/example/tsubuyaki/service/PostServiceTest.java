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
    @DisplayName("Service_投稿一覧_Repositoryの最新50件を返す")
    void latest50_returnsRepositoryLatest50() {
        List<Post> latestPosts = List.of(
                new Post("alice", "new", Instant.parse("2026-05-23T10:00:00Z")),
                new Post("bob", "old", Instant.parse("2026-05-23T09:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(latestPosts);

        List<Post> posts = postService.latest50();

        assertThat(posts).isSameAs(latestPosts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }
}
