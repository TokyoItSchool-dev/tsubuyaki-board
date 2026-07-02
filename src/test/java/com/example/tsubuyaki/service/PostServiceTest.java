package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    private PostService postService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-26T01:23:45Z"), ZoneId.of("Asia/Tokyo"));
        postService = new PostService(postRepository, clock);
    }

    @Test
    @DisplayName("投稿一覧_取得時_Repositoryの最新50件を返す")
    void 投稿一覧_取得時_Repositoryの最新50件を返す() {
        List<Post> posts = List.of(
                new Post("suzuki", "new title", "new", LocalDateTime.of(2026, 6, 26, 10, 0)),
                new Post("tanaka", "old title", "old", LocalDateTime.of(2026, 6, 26, 9, 0)));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> actual = postService.latest();

        assertThat(actual).containsExactlyElementsOf(posts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿検索_検索ワードあり_本文部分一致の最新50件を返す")
    void 投稿検索_検索ワードあり_本文部分一致の最新50件を返す() {
        List<Post> posts = List.of(
                new Post("suzuki", "検索タイトル", "AI研修のメモ", LocalDateTime.of(2026, 7, 2, 9, 0)));
        given(postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("AI研修")).willReturn(posts);

        List<Post> actual = postService.searchByBody(" AI研修 ");

        assertThat(actual).containsExactlyElementsOf(posts);
        verify(postRepository).findTop50ByBodyContainingOrderByCreatedAtDesc("AI研修");
    }

    @Test
    @DisplayName("投稿検索_検索ワードが空白のみ_IllegalArgumentExceptionを投げる")
    void 投稿検索_検索ワードが空白のみ_IllegalArgumentExceptionを投げる() {
        assertThatThrownBy(() -> postService.searchByBody("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("検索ワードを入力してください");
    }

    @Test
    @DisplayName("投稿登録_保存時_現在時刻を設定してRepositoryへ保存する")
    void 投稿登録_保存時_現在時刻を設定してRepositoryへ保存する() {
        postService.create("suzuki", "タイトル", "本文");

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post saved = captor.getValue();
        assertThat(saved.getAuthor()).isEqualTo("suzuki");
        assertThat(saved.getTitle()).isEqualTo("タイトル");
        assertThat(saved.getBody()).isEqualTo("本文");
        assertThat(saved.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 26, 10, 23, 45));
    }
}
