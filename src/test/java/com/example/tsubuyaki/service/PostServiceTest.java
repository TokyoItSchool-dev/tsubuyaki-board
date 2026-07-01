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
    @DisplayName("投稿一覧_latest_Repositoryの新着50件取得結果を返す")
    void 投稿一覧_latest_Repositoryの新着50件取得結果を返す() {
        List<Post> posts = List.of(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> latest = postService.latest();

        assertThat(latest).isSameAs(posts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿詳細_findById_Repositoryのid検索結果を返す")
    void 投稿詳細_findById_RepositoryのId検索結果を返す() {
        Post post = new Post("alice", "本文", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findById(42L)).willReturn(Optional.of(post));

        Optional<Post> found = postService.findById(42L);

        assertThat(found).containsSame(post);
        verify(postRepository).findById(42L);
    }

    @Test
    @DisplayName("投稿作成_create_投稿者と本文を現在日時つきで保存する")
    void 投稿作成_create_投稿者と本文を現在日時つきで保存する() {
        Instant before = Instant.now();

        postService.create("alice", "本文");

        Instant after = Instant.now();
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post savedPost = captor.getValue();
        assertThat(savedPost.getAuthor()).isEqualTo("alice");
        assertThat(savedPost.getBody()).isEqualTo("本文");
        assertThat(savedPost.getCreatedAt()).isBetween(before, after);
    }
}
