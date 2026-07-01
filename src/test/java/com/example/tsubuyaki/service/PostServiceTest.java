package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    @DisplayName("投稿一覧_取得するとき_Repositoryから新着50件を取得する")
    void 投稿一覧_取得するとき_Repositoryから新着50件を取得する() {
        List<Post> posts = List.of(
                new Post("alice", "new", LocalDateTime.parse("2026-05-23T10:00:00")),
                new Post("bob", "old", LocalDateTime.parse("2026-05-23T09:00:00")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> latestPosts = postService.latest();

        assertThat(latestPosts).isEqualTo(posts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿作成_正常入力_前後空白を除去してRepositoryに保存する")
    void 投稿作成_正常入力_前後空白を除去してRepositoryに保存する() {
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        postService.create("  tanaka  ", "  投稿本文です  ");

        verify(postRepository).save(captor.capture());
        Post savedPost = captor.getValue();
        assertThat(savedPost.getAuthor()).isEqualTo("tanaka");
        assertThat(savedPost.getBody()).isEqualTo("投稿本文です");
        assertThat(savedPost.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("投稿詳細_取得するとき_RepositoryからIDで検索する")
    void 投稿詳細_取得するとき_RepositoryからIDで検索する() {
        Post post = new Post("tanaka", "本文", LocalDateTime.parse("2026-05-23T09:00:00"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Optional<Post> foundPost = postService.findById(1L);

        assertThat(foundPost).contains(post);
        verify(postRepository).findById(1L);
    }
}
