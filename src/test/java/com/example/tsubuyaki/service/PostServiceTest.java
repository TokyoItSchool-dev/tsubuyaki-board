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
import static org.mockito.Mockito.times;
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
    @DisplayName("投稿一覧_latest_Repositoryから最新50件を取得して返す")
    void latest_returnsLatest50FromRepository() {
        List<Post> posts = List.of(
                new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> actual = postService.latest();

        assertThat(actual).isSameAs(posts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("キーワード検索_search_Repositoryから本文一致投稿を取得して返す")
    void search_returnsPostsMatchedByBodyFromRepository() {
        List<Post> posts = List.of(
                new Post("alice", "MTGメモ", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("MTG")).willReturn(posts);

        List<Post> actual = postService.search("MTG");

        assertThat(actual).isSameAs(posts);
        verify(postRepository).findTop50ByBodyContainingOrderByCreatedAtDesc("MTG");
    }

    @Test
    @DisplayName("投稿登録_create_Repositoryへ投稿者本文作成日時を渡して保存する")
    void create_savesPostWithAuthorBodyAndCreatedAt() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        Post actual = postService.create("alice", "hello");

        assertThat(actual.getAuthor()).isEqualTo("alice");
        assertThat(actual.getBody()).isEqualTo("hello");
        assertThat(actual.getCreatedAt()).isNotNull();
        verify(postRepository, times(1)).save(actual);
    }

    @Test
    @DisplayName("投稿詳細_findById_Repositoryから指定idの投稿を取得して返す")
    void findById_returnsPostFromRepository() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Optional<Post> actual = postService.findById(1L);

        assertThat(actual).containsSame(post);
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("いいねトグル_未いいね_いいねを保存する")
    void toggleLike_whenNotLiked_savesLike() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "aaaaaaaa")).willReturn(Optional.empty());

        postService.toggleLike(1L, "aaaaaaaa");

        verify(postLikeRepository).save(any(PostLike.class));
    }

    @Test
    @DisplayName("いいねトグル_同一clientHashが再度押下_いいねを削除する")
    void toggleLike_whenAlreadyLiked_deletesLike() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        PostLike like = new PostLike(post, "aaaaaaaa", Instant.parse("2026-05-23T10:02:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "aaaaaaaa")).willReturn(Optional.of(like));

        postService.toggleLike(1L, "aaaaaaaa");

        verify(postLikeRepository).delete(like);
    }

    @Test
    @DisplayName("いいね数_countLikes_Repositoryから指定投稿のいいね数を返す")
    void countLikes_returnsRepositoryCount() {
        given(postLikeRepository.countByPostId(1L)).willReturn(3L);

        long actual = postService.countLikes(1L);

        assertThat(actual).isEqualTo(3L);
        verify(postLikeRepository).countByPostId(1L);
    }

    @Test
    @DisplayName("いいね状態_isLikedByClient_同じclientHashのいいねがあればtrueを返す")
    void isLikedByClient_whenLikeExists_returnsTrue() {
        given(postLikeRepository.existsByPostIdAndClientHash(1L, "aaaaaaaa")).willReturn(true);

        boolean actual = postService.isLikedByClient(1L, "aaaaaaaa");

        assertThat(actual).isTrue();
        verify(postLikeRepository).existsByPostIdAndClientHash(1L, "aaaaaaaa");
    }
}
