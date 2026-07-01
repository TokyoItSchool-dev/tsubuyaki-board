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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_取得するとき_Repositoryの新着50件を返す")
    void 投稿一覧_取得するとき_Repositoryの新着50件を返す() {
        List<Post> expectedPosts = List.of(
                new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expectedPosts);

        List<Post> posts = postService.latest();

        assertThat(posts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿登録_有効な値のとき_Postを保存する")
    void 投稿登録_有効な値のとき_Postを保存する() {
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        postService.create("alice", "こんにちは");

        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();
        assertThat(savedPost.getAuthor()).isEqualTo("alice");
        assertThat(savedPost.getBody()).isEqualTo("こんにちは");
        assertThat(savedPost.getCreatedAt()).isNotNull();
    }
}
