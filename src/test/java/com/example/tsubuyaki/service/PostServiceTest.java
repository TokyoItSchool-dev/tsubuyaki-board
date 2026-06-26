package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
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
import static org.mockito.Mockito.never;
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
    @DisplayName("投稿一覧_latest_Repositoryの新着50件を返す")
    void latest_returnsRepositoryResult() {
        List<Post> repositoryResult = List.of(
                new Post("alice", "最新投稿", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(repositoryResult);

        List<Post> actual = postService.latest();

        assertThat(actual).isSameAs(repositoryResult);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿登録_create_投稿者と本文をPostとして保存する")
    void create_savesPostWithAuthorAndBody() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        Post created = postService.create("alice", "投稿テスト");

        assertThat(created.getAuthor()).isEqualTo("alice");
        assertThat(created.getBody()).isEqualTo("投稿テスト");
        assertThat(created.getCreatedAt()).isNotNull();
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("投稿詳細_findById_Repositoryの検索結果を返す")
    void findById_returnsRepositoryResult() {
        Post post = new Post("alice", "詳細本文", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Optional<Post> actual = postService.findById(1L);

        assertThat(actual).containsSame(post);
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("いいね_toggleLike_未いいねなら保存する")
    void toggleLike_whenNotLiked_savesLike() {
        given(postLikeRepository.findByPostIdAndClientHash(1L, "abcd1234")).willReturn(Optional.empty());

        postService.toggleLike(1L, "abcd1234");

        verify(postLikeRepository).save(any(PostLike.class));
    }

    @Test
    @DisplayName("いいね_toggleLike_既にいいね済みなら削除する")
    void toggleLike_whenAlreadyLiked_deletesLike() {
        PostLike like = new PostLike(1L, "abcd1234", Instant.parse("2026-05-23T10:00:00Z"));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "abcd1234")).willReturn(Optional.of(like));

        postService.toggleLike(1L, "abcd1234");

        verify(postLikeRepository).delete(like);
        verify(postLikeRepository, never()).save(any(PostLike.class));
    }

    @Test
    @DisplayName("いいね_countLikes_Repositoryの件数を返す")
    void countLikes_returnsRepositoryCount() {
        given(postLikeRepository.countByPostId(1L)).willReturn(3L);

        long count = postService.countLikes(1L);

        assertThat(count).isEqualTo(3L);
        verify(postLikeRepository).countByPostId(1L);
    }
}
