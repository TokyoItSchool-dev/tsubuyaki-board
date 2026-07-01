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

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_最新投稿があるとき_Repositoryから最新50件を新着順で取得する")
    void latest_whenPostsExist_returnsLatest50PostsInNewestOrder() {
        Post newerPost = new Post("alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z"));
        Post olderPost = new Post("bob", "古い投稿", Instant.parse("2026-05-23T09:00:00Z"));
        List<Post> latestPosts = List.of(newerPost, olderPost);
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(latestPosts);

        List<Post> actual = postService.latest();

        assertThat(actual).containsExactly(newerPost, olderPost);
    }
}
