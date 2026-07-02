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

    @Mock
    private PostLikeRepository postLikeRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_取得するとき_Repositoryの新着50件を返す")
    void 投稿一覧_取得するとき_Repositoryの新着50件を返す() {
        List<Post> expectedPosts = List.of(
                new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(expectedPosts);

        List<Post> posts = postService.latest();

        assertThat(posts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿検索_キーワード指定ありのとき_本文部分一致検索結果を返す")
    void 投稿検索_キーワード指定ありのとき_本文部分一致検索結果を返す() {
        List<Post> expectedPosts = List.of(
                new Post("alice", "Springの話題", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc("Spring"))
                .willReturn(expectedPosts);

        List<Post> posts = postService.search(" Spring ");

        assertThat(posts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc("Spring");
    }

    @Test
    @DisplayName("投稿検索_キーワード空白のとき_新着50件を返す")
    void 投稿検索_キーワード空白のとき_新着50件を返す() {
        List<Post> expectedPosts = List.of(
                new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(expectedPosts);

        List<Post> posts = postService.search("   ");

        assertThat(posts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿登録_有効な値とアバター色のとき_Postを保存する")
    void 投稿登録_有効な値とアバター色のとき_Postを保存する() {
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        postService.create("alice", "こんにちは", "#3366cc");

        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();
        assertThat(savedPost.getAuthor()).isEqualTo("alice");
        assertThat(savedPost.getBody()).isEqualTo("こんにちは");
        assertThat(savedPost.getAvatarColor()).isEqualTo("#3366cc");
        assertThat(savedPost.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("投稿詳細_取得するとき_Repositoryの検索結果を返す")
    void 投稿詳細_取得するとき_Repositoryの検索結果を返す() {
        Post post = new Post("alice", "詳細本文", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));

        Optional<Post> foundPost = postService.findById(1L);

        assertThat(foundPost).containsSame(post);
        verify(postRepository).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    @DisplayName("投稿削除_存在するIDのとき_deletedAtを設定してtrueを返す")
    void 投稿削除_存在するIDのとき_deletedAtを設定してtrueを返す() {
        Post post = new Post("alice", "削除対象", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));

        boolean deleted = postService.delete(1L);

        assertThat(deleted).isTrue();
        assertThat(post.getDeletedAt()).isNotNull();
        verify(postRepository).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    @DisplayName("投稿削除_存在しないIDのとき_falseを返す")
    void 投稿削除_存在しないIDのとき_falseを返す() {
        given(postRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

        boolean deleted = postService.delete(999L);

        assertThat(deleted).isFalse();
        verify(postRepository).findByIdAndDeletedAtIsNull(999L);
    }
}
