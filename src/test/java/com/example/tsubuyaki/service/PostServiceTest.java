package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagService tagService;

    private PostService postService;

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T04:30:00Z"), ZoneOffset.UTC);

    @Test
    @DisplayName("投稿一覧_latest_Repositoryの新着50件を返す")
    void 投稿一覧_latest_Repositoryの新着50件を返す() {
        postService = new PostService(postRepository, tagService, clock);
        List<Post> expected = List.of(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(expected);

        List<Post> actual = postService.latest();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("投稿検索_search_Repositoryの本文検索結果を返す")
    void 投稿検索_search_Repositoryの本文検索結果を返す() {
        postService = new PostService(postRepository, tagService, clock);
        List<Post> expected = List.of(new Post("alice", "AI研修", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc("AI")).willReturn(expected);

        List<Post> actual = postService.search("AI");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("投稿検索_search_キーワードが空白の場合_新着50件を返す")
    void 投稿検索_search_キーワードが空白の場合_新着50件を返す() {
        postService = new PostService(postRepository, tagService, clock);
        List<Post> expected = List.of(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(expected);

        List<Post> actual = postService.search("  ");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("投稿詳細_findById_Repositoryの投稿を返す")
    void 投稿詳細_findById_Repositoryの投稿を返す() {
        postService = new PostService(postRepository, tagService, clock);
        Post expected = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(expected));

        Post actual = postService.findById(1L);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("タグ検索_findByIdsInOrder_指定されたID順で投稿を返す")
    void タグ検索_findByIdsInOrder_指定されたID順で投稿を返す() {
        postService = new PostService(postRepository, tagService, clock);
        Post older = new Post("alice", "older", Instant.parse("2026-05-23T10:00:00Z"));
        Post newer = new Post("bob", "newer", Instant.parse("2026-05-23T11:00:00Z"));
        org.springframework.test.util.ReflectionTestUtils.setField(older, "id", 1L);
        org.springframework.test.util.ReflectionTestUtils.setField(newer, "id", 2L);
        given(postRepository.findByIdInAndDeletedAtIsNull(List.of(2L, 1L))).willReturn(List.of(older, newer));

        List<Post> actual = postService.findByIdsInOrder(List.of(2L, 1L));

        assertThat(actual).containsExactly(newer, older);
    }

    @Test
    @DisplayName("新規投稿_create_投稿者本文色現在日時を保存する")
    void 新規投稿_create_投稿者本文色現在日時を保存する() {
        postService = new PostService(postRepository, tagService, clock);
        given(postRepository.save(org.mockito.ArgumentMatchers.any(Post.class)))
                .willAnswer(invocation -> {
                    Post post = invocation.getArgument(0);
                    org.springframework.test.util.ReflectionTestUtils.setField(post, "id", 1L);
                    return post;
                });

        postService.create("alice", "本文です #Java", "FCE7F3", "abc12345");

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(captor.capture());
        Post saved = captor.getValue();
        assertThat(saved.getAuthor()).isEqualTo("alice");
        assertThat(saved.getBody()).isEqualTo("本文です #Java");
        assertThat(saved.getColor()).isEqualTo("FCE7F3");
        assertThat(saved.getClientHash()).isEqualTo("abc12345");
        assertThat(saved.getCreatedAt()).isEqualTo(Instant.parse("2026-07-01T04:30:00Z"));
        then(tagService).should().createForPost(1L, "本文です #Java");
    }

    @Test
    @DisplayName("投稿削除_delete_同一clientHashの場合_deletedAtを現在日時にする")
    void 投稿削除_delete_同一clientHashの場合_deletedAtを現在日時にする() {
        postService = new PostService(postRepository, tagService, clock);
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T10:00:00Z"),
                "E0F2FE", "abc12345");
        given(postRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));

        boolean deleted = postService.delete(1L, "abc12345");

        assertThat(deleted).isTrue();
        assertThat(post.getDeletedAt()).isEqualTo(Instant.parse("2026-07-01T04:30:00Z"));
    }

    @Test
    @DisplayName("投稿削除_delete_clientHashが異なる場合_削除しない")
    void 投稿削除_delete_clientHashが異なる場合_削除しない() {
        postService = new PostService(postRepository, tagService, clock);
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T10:00:00Z"),
                "E0F2FE", "abc12345");
        given(postRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));

        boolean deleted = postService.delete(1L, "def67890");

        assertThat(deleted).isFalse();
        assertThat(post.getDeletedAt()).isNull();
    }
}
