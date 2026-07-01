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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    // Repository をモックにし、DB に接続せず Service の呼び出しだけを検証する。
    @Mock
    private PostRepository postRepository;

    // モック Repository を注入した Service をテスト対象にする。
    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_latest_Repositoryから新着50件を取得する")
    void 投稿一覧_latest_Repositoryから新着50件を取得する() {
        // Repository が返す投稿一覧を固定し、Service の戻り値と比較できるようにする。
        List<Post> expectedPosts = List.of(
                new Post("alice", "hello", LocalDateTime.of(2026, 5, 23, 10, 0)));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expectedPosts);

        // Service の latest を実行し、Repository から取得した投稿一覧を受け取る。
        List<Post> actualPosts = postService.latest();

        // Repository の戻り値をそのまま返し、最新50件取得メソッドを呼んだことを確認する。
        assertThat(actualPosts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿検索_qがnull_Repositoryから新着50件を取得する")
    void 投稿検索_qがnull_Repositoryから新着50件を取得する() {
        // q が未指定の場合は既存の一覧と同じ投稿一覧を返す。
        List<Post> expectedPosts = List.of(
                new Post("alice", "hello", LocalDateTime.of(2026, 5, 23, 10, 0)));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expectedPosts);

        List<Post> actualPosts = postService.search(null);

        assertThat(actualPosts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
        verify(postRepository, never()).findTop50ByBodyContainingIgnoreCaseOrderByCreatedAtDesc("hello");
    }

    @Test
    @DisplayName("投稿検索_qが空文字_Repositoryから新着50件を取得する")
    void 投稿検索_qが空文字_Repositoryから新着50件を取得する() {
        // q が空文字の場合も検索条件なしとして扱う。
        List<Post> expectedPosts = List.of(
                new Post("alice", "hello", LocalDateTime.of(2026, 5, 23, 10, 0)));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expectedPosts);

        List<Post> actualPosts = postService.search("");

        assertThat(actualPosts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿検索_qあり_本文LIKE検索を実行する")
    void 投稿検索_qあり_本文LIKE検索を実行する() {
        // q が指定された場合は本文に対するLIKE検索へ分岐する。
        List<Post> expectedPosts = List.of(
                new Post("alice", "keywordを含む本文", LocalDateTime.of(2026, 5, 23, 10, 0)));
        given(postRepository.findTop50ByBodyContainingIgnoreCaseOrderByCreatedAtDesc("keyword"))
                .willReturn(expectedPosts);

        List<Post> actualPosts = postService.search("keyword");

        assertThat(actualPosts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByBodyContainingIgnoreCaseOrderByCreatedAtDesc("keyword");
        verify(postRepository, never()).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿作成_create_PostをRepositoryに保存する")
    void 投稿作成_create_PostをRepositoryに保存する() {
        // 保存対象の Post エンティティを用意する。
        Post post = new Post("alice", "本文です", LocalDateTime.of(2026, 5, 23, 10, 0));

        // Service の create を実行し、Repository.save へ処理を委譲させる。
        postService.create(post);

        // Repository.save に渡された Post が、Service に渡したものと同じであることを確認する。
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(post);
    }

    @Test
    @DisplayName("投稿詳細_findById_Repositoryから指定IDの投稿を取得する")
    void 投稿詳細_findById_Repositoryから指定IDの投稿を取得する() {
        // Repository が指定IDの投稿を返す状況を作る。
        Post expectedPost = new Post("alice", "詳細本文", LocalDateTime.of(2026, 5, 23, 10, 0));
        given(postRepository.findById(42L)).willReturn(Optional.of(expectedPost));

        // Service の findById を実行し、Repository から取得した Optional を受け取る。
        Optional<Post> actualPost = postService.findById(42L);

        // Repository の戻り値をそのまま返し、指定IDで検索したことを確認する。
        assertThat(actualPost).containsSame(expectedPost);
        verify(postRepository).findById(42L);
    }
}
