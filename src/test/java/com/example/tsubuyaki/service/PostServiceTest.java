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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_latest_Repositoryから新着50件を取得して返す")
    void latest_returnsLatest50FromRepository() {
        List<Post> expectedPosts = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z")),
                new Post("bob", "古い投稿", Instant.parse("2026-05-23T09:00:00Z"))
        );
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expectedPosts);

        List<Post> posts = postService.latest();

        assertThat(posts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿作成_create_投稿者と本文をPostとして保存する")
    void create_savesPostWithAuthorAndBody() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        postService.create("alice", "朝の共有です");

        verify(postRepository).save(argThat(post ->
                "alice".equals(post.getAuthor())
                        && "朝の共有です".equals(post.getBody())
                        && post.getCreatedAt() != null));
    }

    @Test
    @DisplayName("投稿詳細_findById_RepositoryからIDで投稿を取得して返す")
    void findById_returnsPostFromRepository() {
        Post expectedPost = new Post("alice", "朝の共有です", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(expectedPost));

        Optional<Post> post = postService.findById(1L);

        assertThat(post).containsSame(expectedPost);
        verify(postRepository).findById(1L);
    }
}
