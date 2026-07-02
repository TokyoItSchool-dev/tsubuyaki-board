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
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.never;
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
    @DisplayName("投稿一覧_latest_Repositoryの新着50件を返す")
    void 投稿一覧_latest_Repositoryの新着50件を返す() {
        List<Post> posts = List.of(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> latestPosts = postService.latest();

        assertThat(latestPosts).isSameAs(posts);
        verify(postRepository).findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿検索_キーワードあり_Repositoryの本文検索を返す")
    void 投稿検索_キーワードあり_Repositoryの本文検索を返す() {
        List<Post> posts = List.of(new Post("alice", "共有です", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByDeletedAtIsNullAndBodyContainingIgnoreCaseOrderByCreatedAtDesc("共有"))
                .willReturn(posts);

        List<Post> searchResults = postService.search("共有");

        assertThat(searchResults).isSameAs(posts);
        verify(postRepository).findTop50ByDeletedAtIsNullAndBodyContainingIgnoreCaseOrderByCreatedAtDesc("共有");
    }

    @Test
    @DisplayName("投稿検索_前後空白あり_trimして検索する")
    void 投稿検索_前後空白あり_trimして検索する() {
        List<Post> posts = List.of(new Post("alice", "共有です", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByDeletedAtIsNullAndBodyContainingIgnoreCaseOrderByCreatedAtDesc("共有"))
                .willReturn(posts);

        List<Post> searchResults = postService.search("  共有  ");

        assertThat(searchResults).isSameAs(posts);
        verify(postRepository).findTop50ByDeletedAtIsNullAndBodyContainingIgnoreCaseOrderByCreatedAtDesc("共有");
    }

    @Test
    @DisplayName("投稿検索_空白のみ_latestを返す")
    void 投稿検索_空白のみ_latestを返す() {
        List<Post> posts = List.of(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> searchResults = postService.search("   ");

        assertThat(searchResults).isSameAs(posts);
        verify(postRepository).findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
        verify(postRepository, never())
                .findTop50ByDeletedAtIsNullAndBodyContainingIgnoreCaseOrderByCreatedAtDesc(anyString());
    }

    @Test
    @DisplayName("投稿検索_null_latestを返す")
    void 投稿検索_null_latestを返す() {
        List<Post> posts = List.of(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> searchResults = postService.search(null);

        assertThat(searchResults).isSameAs(posts);
        verify(postRepository).findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
        verify(postRepository, never())
                .findTop50ByDeletedAtIsNullAndBodyContainingIgnoreCaseOrderByCreatedAtDesc(anyString());
    }

    @Test
    @DisplayName("投稿検索_空文字_latestを返す")
    void 投稿検索_空文字_latestを返す() {
        List<Post> posts = List.of(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> searchResults = postService.search("");

        assertThat(searchResults).isSameAs(posts);
        verify(postRepository).findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
        verify(postRepository, never())
                .findTop50ByDeletedAtIsNullAndBodyContainingIgnoreCaseOrderByCreatedAtDesc(anyString());
    }

    @Test
    @DisplayName("投稿詳細_findById_Repositoryの検索結果を返す")
    void 投稿詳細_findById_Repositoryの検索結果を返す() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(10L)).willReturn(Optional.of(post));

        Optional<Post> actual = postService.findById(10L);

        assertThat(actual).containsSame(post);
        verify(postRepository).findByIdAndDeletedAtIsNull(10L);
    }

    @Test
    @DisplayName("投稿詳細_findById_存在しないとき空を返す")
    void 投稿詳細_findById_存在しないとき空を返す() {
        given(postRepository.findByIdAndDeletedAtIsNull(404L)).willReturn(Optional.empty());

        Optional<Post> actual = postService.findById(404L);

        assertThat(actual).isEmpty();
        verify(postRepository).findByIdAndDeletedAtIsNull(404L);
    }

    @Test
    @DisplayName("投稿登録_create_指定したアバター色で保存する")
    void 投稿登録_create_指定したアバター色で保存する() {
        var postCaptor = forClass(Post.class);

        postService.create("alice", "hello", "blue");

        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().getAuthor()).isEqualTo("alice");
        assertThat(postCaptor.getValue().getBody()).isEqualTo("hello");
        assertThat(postCaptor.getValue().getAvatarColor()).isEqualTo("blue");
        assertThat(postCaptor.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("投稿登録_create_アバター色null_既定色grayで保存する")
    void 投稿登録_create_アバター色null_既定色grayで保存する() {
        var postCaptor = forClass(Post.class);

        postService.create("alice", "hello", null);

        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().getAvatarColor()).isEqualTo("gray");
    }

    @Test
    @DisplayName("投稿登録_create_アバター色空白_既定色grayで保存する")
    void 投稿登録_create_アバター色空白_既定色grayで保存する() {
        var postCaptor = forClass(Post.class);

        postService.create("alice", "hello", "   ");

        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().getAvatarColor()).isEqualTo("gray");
    }

    @Test
    @DisplayName("投稿削除_存在する投稿_削除日時を設定する")
    void 投稿削除_存在する投稿_削除日時を設定する() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(10L)).willReturn(Optional.of(post));

        postService.deletePost(10L);

        assertThat(post.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("投稿削除_存在しないID_例外を投げる")
    void 投稿削除_存在しないID_例外を投げる() {
        given(postRepository.findByIdAndDeletedAtIsNull(404L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.deletePost(404L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("post not found: 404");
    }

    @Test
    @DisplayName("いいねトグル_未いいね_いいねを登録する")
    void いいねトグル_未いいね_いいねを登録する() {
        given(postRepository.existsByIdAndDeletedAtIsNull(10L)).willReturn(true);
        given(postLikeRepository.existsByPostIdAndClientHash(10L, "abcdef12")).willReturn(false);

        postService.toggleLike(10L, "abcdef12");

        verify(postLikeRepository).save(new PostLike(10L, "abcdef12"));
        verify(postLikeRepository, never()).deleteByPostIdAndClientHash(10L, "abcdef12");
    }

    @Test
    @DisplayName("いいねトグル_いいね済み_いいねを削除する")
    void いいねトグル_いいね済み_いいねを削除する() {
        given(postRepository.existsByIdAndDeletedAtIsNull(10L)).willReturn(true);
        given(postLikeRepository.existsByPostIdAndClientHash(10L, "abcdef12")).willReturn(true);

        postService.toggleLike(10L, "abcdef12");

        verify(postLikeRepository).deleteByPostIdAndClientHash(10L, "abcdef12");
    }

    @Test
    @DisplayName("いいね数_countLikes_Repositoryの件数を返す")
    void いいね数_countLikes_Repositoryの件数を返す() {
        given(postLikeRepository.countByPostId(10L)).willReturn(3L);

        long likeCount = postService.countLikes(10L);

        assertThat(likeCount).isEqualTo(3);
        verify(postLikeRepository).countByPostId(10L);
    }

    @Test
    @DisplayName("いいね状態_likedBy_Repositoryの存在確認結果を返す")
    void いいね状態_likedBy_Repositoryの存在確認結果を返す() {
        given(postLikeRepository.existsByPostIdAndClientHash(10L, "abcdef12")).willReturn(true);

        boolean likedByClient = postService.likedBy(10L, "abcdef12");

        assertThat(likedByClient).isTrue();
        verify(postLikeRepository).existsByPostIdAndClientHash(10L, "abcdef12");
    }
}
