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
                new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expectedPosts);

        // Service の latest を実行し、Repository から取得した投稿一覧を受け取る。
        List<Post> actualPosts = postService.latest();

        // Repository の戻り値をそのまま返し、最新50件取得メソッドを呼んだことを確認する。
        assertThat(actualPosts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿作成_create_PostをRepositoryに保存する")
    void 投稿作成_create_PostをRepositoryに保存する() {
        // 保存対象の Post エンティティを用意する。
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T10:00:00Z"));

        // Service の create を実行し、Repository.save へ処理を委譲させる。
        postService.create(post);

        // Repository.save に渡された Post が、Service に渡したものと同じであることを確認する。
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(post);
    }
}
