package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.testsupport.PostTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostSearchService postSearchService;

    @Mock
    private TagService tagService;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_latest_Repositoryから新着50件を取得する")
    void 投稿一覧_latest_Repositoryから新着50件を取得する() {
        List<Post> expectedPosts = List.of(
                PostTestFactory.post("alice", "hello"));
        given(postRepository.findTop50ByDeletedAtOrderByCreatedAtDesc(Post.NOT_DELETED))
                .willReturn(expectedPosts);

        List<Post> actualPosts = postService.latest();

        assertThat(actualPosts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByDeletedAtOrderByCreatedAtDesc(Post.NOT_DELETED);
    }

    @Test
    @DisplayName("投稿検索_search_PostSearchServiceへ委譲する")
    void 投稿検索_search_PostSearchServiceへ委譲する() {
        List<Post> expectedPosts = List.of(
                PostTestFactory.post("alice", "keywordを含む本文"));
        given(postSearchService.search("keyword")).willReturn(expectedPosts);

        List<Post> actualPosts = postService.search("keyword");

        assertThat(actualPosts).isSameAs(expectedPosts);
        verify(postSearchService).search("keyword");
    }

    @Test
    @DisplayName("投稿作成_create_PostをRepositoryに保存しタグ保存へ委譲する")
    void 投稿作成_create_PostをRepositoryに保存しタグ保存へ委譲する() {
        Post post = PostTestFactory.post("alice", "本文です #spring");
        given(postRepository.save(post)).willReturn(post);

        postService.create(post);

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(post);
        verify(tagService).saveTagsFor(post);
    }

    @Test
    @DisplayName("投稿詳細_findById_Repositoryから指定IDの投稿を取得する")
    void 投稿詳細_findById_Repositoryから指定IDの投稿を取得する() {
        Post expectedPost = PostTestFactory.post("alice", "詳細本文");
        given(postRepository.findByIdAndDeletedAt(42L, Post.NOT_DELETED)).willReturn(Optional.of(expectedPost));

        Optional<Post> actualPost = postService.findById(42L);

        assertThat(actualPost).containsSame(expectedPost);
        verify(postRepository).findByIdAndDeletedAt(42L, Post.NOT_DELETED);
    }

    @Test
    @DisplayName("投稿削除_delete_削除フラグを1にして保存する")
    void 投稿削除_delete_削除フラグを1にして保存する() {
        Post post = PostTestFactory.postWithId(42L, "alice", "削除する本文");
        given(postRepository.findByIdAndDeletedAt(42L, Post.NOT_DELETED)).willReturn(Optional.of(post));

        postService.delete(42L);

        assertThat(post.getDeletedAt()).isEqualTo(Post.DELETED);
        verify(postRepository).save(post);
    }

    @Test
    @DisplayName("投稿削除_delete_削除済み投稿はPostNotFoundExceptionを投げる")
    void 投稿削除_delete_削除済み投稿はPostNotFoundExceptionを投げる() {
        given(postRepository.findByIdAndDeletedAt(42L, Post.NOT_DELETED)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.delete(42L))
                .isInstanceOf(PostNotFoundException.class);
    }
}
