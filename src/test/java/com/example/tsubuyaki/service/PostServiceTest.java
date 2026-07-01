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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    private PostService postService;

    @Test
    @DisplayName("投稿作成_投稿者名と本文を受け取ったとき_現在時刻付きの投稿を保存する")
    void 投稿作成_投稿者名と本文を受け取ったとき_現在時刻付きの投稿を保存する() {
        Instant fixedInstant = Instant.parse("2026-05-23T10:15:30Z");
        postService = new PostService(postRepository, Clock.fixed(fixedInstant, ZoneOffset.UTC));

        postService.createPost("alice", "はじめての投稿");

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().getAuthor()).isEqualTo("alice");
        assertThat(postCaptor.getValue().getBody()).isEqualTo("はじめての投稿");
        assertThat(postCaptor.getValue().getCreatedAt()).isEqualTo(fixedInstant);
    }

    @Test
    @DisplayName("投稿一覧_投稿があるとき_Repositoryの最新50件を返す")
    void 投稿一覧_投稿があるとき_Repositoryの最新50件を返す() {
        postService = new PostService(postRepository);
        List<Post> latestPosts = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z")),
                new Post("bob", "古い投稿", Instant.parse("2026-05-23T09:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(latestPosts);

        List<Post> actual = postService.findLatest50Posts();

        assertThat(actual).isEqualTo(latestPosts);
    }
}
