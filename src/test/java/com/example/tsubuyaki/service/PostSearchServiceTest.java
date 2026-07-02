package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.testsupport.PostTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostSearchServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostSearchService postSearchService;

    @Test
    @DisplayName("投稿検索_qなし_Repositoryから新着50件を取得する")
    void 投稿検索_qなし_Repositoryから新着50件を取得する() {
        List<Post> expectedPosts = List.of(post(1L, "alice", "hello", 0));
        given(postRepository.findTop50ByDeletedAtOrderByCreatedAtDesc(Post.NOT_DELETED))
                .willReturn(expectedPosts);

        List<Post> actualPosts = postSearchService.search(null);

        assertThat(actualPosts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByDeletedAtOrderByCreatedAtDesc(Post.NOT_DELETED);
        verify(postRepository, never())
                .findTop50ByDeletedAtAndBodyContainingIgnoreCaseOrderByCreatedAtDesc(Post.NOT_DELETED, "hello");
    }

    @Test
    @DisplayName("投稿検索_qあり_本文検索とタグ検索をマージして新着順で返す")
    void 投稿検索_qあり_本文検索とタグ検索をマージして新着順で返す() {
        Post oldBodyPost = post(1L, "alice", "keyword old", 0);
        Post newTagPost = post(2L, "bob", "tag new", 2);
        given(postRepository.findTop50ByDeletedAtAndBodyContainingIgnoreCaseOrderByCreatedAtDesc(
                Post.NOT_DELETED, "keyword"))
                .willReturn(List.of(oldBodyPost));
        given(postRepository.findByTagNameAndPostDeletedAtOrderByCreatedAtDesc(
                any(String.class), any(Integer.class), any(Pageable.class)))
                .willReturn(List.of(newTagPost));

        List<Post> actualPosts = postSearchService.search("keyword");

        assertThat(actualPosts).extracting(Post::getBody)
                .containsExactly("tag new", "keyword old");
        verify(postRepository).findByTagNameAndPostDeletedAtOrderByCreatedAtDesc(
                any(String.class), any(Integer.class), any(Pageable.class));
    }

    @Test
    @DisplayName("投稿検索_qがハッシュタグ_タグ名から先頭のシャープを外す")
    void 投稿検索_qがハッシュタグ_タグ名から先頭のシャープを外す() {
        given(postRepository.findTop50ByDeletedAtAndBodyContainingIgnoreCaseOrderByCreatedAtDesc(
                Post.NOT_DELETED, "#spring"))
                .willReturn(Collections.emptyList());
        given(postRepository.findByTagNameAndPostDeletedAtOrderByCreatedAtDesc(
                any(String.class), any(Integer.class), any(Pageable.class)))
                .willReturn(Collections.emptyList());

        postSearchService.search("#spring");

        verify(postRepository).findByTagNameAndPostDeletedAtOrderByCreatedAtDesc(
                org.mockito.ArgumentMatchers.eq("spring"),
                org.mockito.ArgumentMatchers.eq(Post.NOT_DELETED),
                any(Pageable.class));
    }

    @Test
    @DisplayName("投稿検索_本文検索とタグ検索で同一投稿_重複を除外する")
    void 投稿検索_本文検索とタグ検索で同一投稿_重複を除外する() {
        Post post = post(42L, "alice", "keyword #keyword", 0);
        given(postRepository.findTop50ByDeletedAtAndBodyContainingIgnoreCaseOrderByCreatedAtDesc(
                Post.NOT_DELETED, "keyword"))
                .willReturn(List.of(post));
        given(postRepository.findByTagNameAndPostDeletedAtOrderByCreatedAtDesc(
                any(String.class), any(Integer.class), any(Pageable.class)))
                .willReturn(List.of(post));

        List<Post> actualPosts = postSearchService.search("keyword");

        assertThat(actualPosts).containsExactly(post);
    }

    private Post post(Long id, String author, String body, int minutes) {
        return PostTestFactory.postWithId(id, author, body,
                LocalDateTime.of(2026, 5, 23, 10, 0).plusMinutes(minutes));
    }
}
