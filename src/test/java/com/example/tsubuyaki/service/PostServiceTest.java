package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
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
    @DisplayName("Service_投稿作成_投稿者と本文と作成日時を保存する")
    void Service_投稿作成_投稿者と本文と作成日時を保存する() {
        Instant before = Instant.now();

        postService.create("alice", "今日の共有です");

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post saved = captor.getValue();
        assertThat(saved.getAuthor()).isEqualTo("alice");
        assertThat(saved.getBody()).isEqualTo("今日の共有です");
        assertThat(saved.getCreatedAt()).isBetween(before, Instant.now());
    }

    @Test
    @DisplayName("Service_投稿詳細取得_ID検索をRepositoryに委譲する")
    void Service_投稿詳細取得_ID検索をRepositoryに委譲する() {
        Post post = new Post("alice", "詳細", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Optional<Post> actual = postService.findById(1L);

        assertThat(actual).contains(post);
        verify(postRepository).findById(1L);
    }
}
