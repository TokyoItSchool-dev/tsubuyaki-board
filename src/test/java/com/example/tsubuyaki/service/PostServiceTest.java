package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.LikeRepository;
import com.example.tsubuyaki.repository.PostEntity;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private TagService tagService;

    @Test
    @DisplayName("投稿一覧_latest_Repositoryから新着50件を取得する")
    void 投稿一覧_latest_Repositoryから新着50件を取得する() {
        PostService postService = new PostService(postRepository, likeRepository, tagService, fixedClock());
        given(postRepository.findTop50ByOrderByCreatedAtDescIdDesc()).willReturn(List.of(
                new PostEntity(1L, "alice", "BLUE", "hello", Instant.parse("2026-06-26T09:00:00Z"))));

        List<Post> actual = postService.latest();

        assertThat(actual)
                .extracting(Post::getAuthor)
                .containsExactly("alice");
        assertThat(actual.get(0).getAvatarColor()).isEqualTo("BLUE");
        assertThat(actual.get(0).getId()).isEqualTo(1L);
        verify(postRepository).findTop50ByOrderByCreatedAtDescIdDesc();
    }

    @Test
    @DisplayName("投稿検索_searchPosts_本文部分一致で検索する")
    void 投稿検索_searchPosts_本文部分一致で検索する() {
        PostService postService = new PostService(postRepository, likeRepository, tagService, fixedClock());
        given(postRepository.findTop50ByBodyContainingOrderByCreatedAtDescIdDesc("Spring")).willReturn(List.of(
                new PostEntity(1L, "alice", "GREEN", "Spring Boot の共有です", Instant.parse("2026-06-26T09:00:00Z"))));

        List<Post> actual = postService.searchPosts("Spring");

        assertThat(actual)
                .extracting(Post::getBody)
                .containsExactly("Spring Boot の共有です");
        assertThat(actual.get(0).getAvatarColor()).isEqualTo("GREEN");
        verify(postRepository).findTop50ByBodyContainingOrderByCreatedAtDescIdDesc("Spring");
        verify(postRepository, never()).findTop50ByOrderByCreatedAtDescIdDesc();
    }

    @Test
    @DisplayName("投稿検索_searchPosts_空文字は通常一覧を返す")
    void 投稿検索_searchPosts_空文字は通常一覧を返す() {
        PostService postService = new PostService(postRepository, likeRepository, tagService, fixedClock());
        given(postRepository.findTop50ByOrderByCreatedAtDescIdDesc()).willReturn(List.of(
                new PostEntity(1L, "alice", "BLUE", "hello", Instant.parse("2026-06-26T09:00:00Z"))));

        List<Post> actual = postService.searchPosts("  ");

        assertThat(actual)
                .extracting(Post::getBody)
                .containsExactly("hello");
        verify(postRepository).findTop50ByOrderByCreatedAtDescIdDesc();
        verify(postRepository, never()).findTop50ByBodyContainingOrderByCreatedAtDescIdDesc(anyString());
    }

    @Test
    @DisplayName("投稿一覧_findPosts_空文字は通常一覧を返す")
    void 投稿一覧_findPosts_空文字は通常一覧を返す() {
        PostService postService = new PostService(postRepository, likeRepository, tagService, fixedClock());
        given(postRepository.findTop50ByOrderByCreatedAtDescIdDesc()).willReturn(List.of(
                new PostEntity(1L, "alice", "BLUE", "hello", Instant.parse("2026-06-26T09:00:00Z"))));

        List<Post> actual = postService.findPosts("  ");

        assertThat(actual)
                .extracting(Post::getBody)
                .containsExactly("hello");
        verify(postRepository).findTop50ByOrderByCreatedAtDescIdDesc();
        verify(postRepository, never()).findTop50ByBodyContainingOrderByCreatedAtDescIdDesc(anyString());
    }

    @Test
    @DisplayName("投稿作成_create_投稿者と本文を保存する")
    void 投稿作成_create_投稿者と本文を保存する() {
        PostService postService = new PostService(postRepository, likeRepository, tagService, fixedClock());
        given(tagService.resolveTags("今日の共有です #Java #Java")).willReturn(List.of(new TagEntity(1L, "Java")));
        given(postRepository.save(any(PostEntity.class))).willAnswer(invocation -> {
            PostEntity entity = invocation.getArgument(0);
            return new PostEntity(
                    10L,
                    entity.getAuthor(),
                    entity.getAvatarColor(),
                    entity.getBody(),
                    entity.getCreatedAt(),
                    entity.getTags());
        });

        Post actual = postService.create(" alice ", "ORANGE", " 今日の共有です #Java #Java ");

        assertThat(actual.getId()).isEqualTo(10L);
        assertThat(actual.getAuthor()).isEqualTo("alice");
        assertThat(actual.getAvatarColor()).isEqualTo("ORANGE");
        assertThat(actual.getBody()).isEqualTo("今日の共有です #Java #Java");
        assertThat(actual.getTagNames()).containsExactly("Java");
        assertThat(actual.getCreatedAt()).isEqualTo(Instant.parse("2026-06-26T09:00:00Z"));
        verify(tagService).resolveTags("今日の共有です #Java #Java");
        verify(postRepository).save(any(PostEntity.class));
        verify(postRepository, never()).findTop50ByOrderByCreatedAtDescIdDesc();
    }

    @Test
    @DisplayName("投稿作成_タグなし本文_投稿だけを保存できる")
    void 投稿作成_タグなし本文_投稿だけを保存できる() {
        PostService postService = new PostService(postRepository, likeRepository, tagService, fixedClock());
        given(tagService.resolveTags("タグはありません")).willReturn(List.of());
        given(postRepository.save(any(PostEntity.class))).willAnswer(invocation -> {
            PostEntity entity = invocation.getArgument(0);
            return new PostEntity(
                    11L,
                    entity.getAuthor(),
                    entity.getAvatarColor(),
                    entity.getBody(),
                    entity.getCreatedAt(),
                    entity.getTags());
        });

        Post actual = postService.create("alice", "BLUE", "タグはありません");

        assertThat(actual.getId()).isEqualTo(11L);
        assertThat(actual.getTagNames()).isEmpty();
        verify(tagService).resolveTags("タグはありません");
    }

    @Test
    @DisplayName("投稿作成_空白のみ_保存せず例外を投げる")
    void 投稿作成_空白のみ_保存せず例外を投げる() {
        PostService postService = new PostService(postRepository, likeRepository, tagService, fixedClock());

        assertThatThrownBy(() -> postService.create("   ", "BLUE", "　　"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("author");

        verify(postRepository, never()).save(any(PostEntity.class));
    }

    @Test
    @DisplayName("投稿作成_不正なアバター色_保存せず例外を投げる")
    void 投稿作成_不正なアバター色_保存せず例外を投げる() {
        PostService postService = new PostService(postRepository, likeRepository, tagService, fixedClock());

        assertThatThrownBy(() -> postService.create("alice", "BLACK", "本文があります"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BLACK");

        verify(postRepository, never()).save(any(PostEntity.class));
    }

    @Test
    @DisplayName("投稿詳細_getById_存在しないidは専用例外を投げる")
    void 投稿詳細_getById_存在しないidは専用例外を投げる() {
        PostService postService = new PostService(postRepository, likeRepository, tagService, fixedClock());
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getById(999L))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("投稿詳細_getDetail_投稿といいね数をまとめて返す")
    void 投稿詳細_getDetail_投稿といいね数をまとめて返す() {
        PostService postService = new PostService(postRepository, likeRepository, tagService, fixedClock());
        given(postRepository.findById(1L)).willReturn(Optional.of(
                new PostEntity(1L, "alice", "PURPLE", "hello", Instant.parse("2026-06-26T09:00:00Z"))));
        given(likeRepository.countByPostId(1L)).willReturn(15L);

        PostDetail actual = postService.getDetail(1L);

        assertThat(actual.post().getAuthor()).isEqualTo("alice");
        assertThat(actual.likeCount()).isEqualTo(15L);
        verify(postRepository).findById(1L);
        verify(likeRepository).countByPostId(1L);
    }

    @Test
    @DisplayName("タグ別一覧_findPostsByTag_指定タグの投稿を返す")
    void タグ別一覧_findPostsByTag_指定タグの投稿を返す() {
        PostService postService = new PostService(postRepository, likeRepository, tagService, fixedClock());
        given(postRepository.findTop50ByTagsNameOrderByCreatedAtDescIdDesc("Java")).willReturn(List.of(
                new PostEntity(1L, "alice", "BLUE", "Javaの共有 #Java", Instant.parse("2026-06-26T09:00:00Z"),
                        List.of(new TagEntity(1L, "Java")))));

        List<Post> actual = postService.findPostsByTag("Java");

        assertThat(actual)
                .extracting(Post::getBody)
                .containsExactly("Javaの共有 #Java");
        assertThat(actual.get(0).getTagNames()).containsExactly("Java");
        verify(postRepository).findTop50ByTagsNameOrderByCreatedAtDescIdDesc("Java");
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-06-26T09:00:00Z"), ZoneOffset.UTC);
    }
}
