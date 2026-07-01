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
    @DisplayName("投稿一覧_取得するとき_Repositoryの新着50件取得を使う")
    void findLatest50_取得するとき_Repositoryの新着50件取得を使う() {
        List<Post> expected = List.of(new Post(
                "alice",
                "hello",
                Instant.parse("2026-06-30T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expected);

        List<Post> actual = postService.findLatest50();

        assertThat(actual).isSameAs(expected);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿詳細_IDを指定したとき_RepositoryのfindByIdを使う")
    void findById_IDを指定したとき_RepositoryのfindByIdを使う() {
        Optional<Post> expected = Optional.of(new Post(
                "alice",
                "詳細表示の本文です",
                Instant.parse("2026-06-30T10:00:00Z")));
        given(postRepository.findById(10L)).willReturn(expected);

        Optional<Post> actual = postService.findById(10L);

        assertThat(actual).isSameAs(expected);
        verify(postRepository).findById(10L);
    }

    @Test
    @DisplayName("いいね_未登録のとき_いいねを登録する")
    void toggleLike_未登録のとき_いいねを登録する() {
        postService.toggleLike(10L, "abc12345");

        assertThat(postService.countLikes(10L)).isEqualTo(1L);
    }

    @Test
    @DisplayName("いいね_登録済みのとき_いいねを削除する")
    void toggleLike_登録済みのとき_いいねを削除する() {
        postService.toggleLike(10L, "abc12345");
        postService.toggleLike(10L, "abc12345");

        assertThat(postService.countLikes(10L)).isZero();
    }

    @Test
    @DisplayName("いいね_別ユーザーが押したとき_件数が増える")
    void toggleLike_別ユーザーが押したとき_件数が増える() {
        postService.toggleLike(10L, "abc12345");
        postService.toggleLike(10L, "def67890");

        assertThat(postService.countLikes(10L)).isEqualTo(2L);
    }

    @Test
    @DisplayName("投稿登録_投稿者と本文を受け取ったとき_現在時刻付きの投稿を保存する")
    void create_投稿者と本文を受け取ったとき_現在時刻付きの投稿を保存する() {
        Instant before = Instant.now();

        postService.create("alice", "本日の共有です");

        Instant after = Instant.now();
        verify(postRepository).save(argThat(post ->
                "alice".equals(post.getAuthor())
                        && "本日の共有です".equals(post.getBody())
                        && !post.getCreatedAt().isBefore(before)
                        && !post.getCreatedAt().isAfter(after)));
    }
}
