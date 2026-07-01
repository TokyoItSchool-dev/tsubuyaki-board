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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
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
        List<Post> expected = List.of(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expected);

        List<Post> posts = postService.latest();

        assertThat(posts).isEqualTo(expected);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿登録_create_投稿を作成してRepositoryへ保存する")
    void 投稿登録_create_投稿を作成してRepositoryへ保存する() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
        Instant before = Instant.now();

        Post created = postService.create("alice", "hello");

        Instant after = Instant.now();
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post saved = captor.getValue();
        assertThat(created).isSameAs(saved);
        assertThat(saved.getAuthor()).isEqualTo("alice");
        assertThat(saved.getBody()).isEqualTo("hello");
        assertThat(saved.getCreatedAt()).isBetween(before, after);
    }
}
