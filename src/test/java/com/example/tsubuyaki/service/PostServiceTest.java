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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_latest_Repositoryから新着50件を取得して返す")
    void latest_returnsLatest50FromRepository() {
        List<Post> expectedPosts = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z")),
                new Post("bob", "古い投稿", Instant.parse("2026-05-23T09:00:00Z"))
        );
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expectedPosts);

        List<Post> posts = postService.latest();

        assertThat(posts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿検索_search_キーワード指定時はbody部分一致の新着50件を返す")
    void search_whenKeywordProvided_returnsMatchedPostsFromRepository() {
        List<Post> expectedPosts = List.of(
                new Post("alice", "検索対象の新しい投稿", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("検索対象")).willReturn(expectedPosts);

        List<Post> posts = postService.search("検索対象");

        assertThat(posts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByBodyContainingOrderByCreatedAtDesc("検索対象");
    }

    @Test
    @DisplayName("投稿検索_search_q未指定なら通常一覧と同じ結果を返す")
    void search_whenQueryIsNull_returnsLatestPosts() {
        List<Post> expectedPosts = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expectedPosts);

        List<Post> posts = postService.search(null);

        assertThat(posts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    @DisplayName("投稿検索_search_q空文字なら通常一覧と同じ結果を返す")
    void search_whenQueryIsEmpty_returnsLatestPosts() {
        List<Post> expectedPosts = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expectedPosts);

        List<Post> posts = postService.search("");

        assertThat(posts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    @DisplayName("投稿検索_search_q空白のみなら通常一覧と同じ結果を返す")
    void search_whenQueryIsBlank_returnsLatestPosts() {
        List<Post> expectedPosts = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expectedPosts);

        List<Post> posts = postService.search("   ");

        assertThat(posts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    @DisplayName("投稿作成_create_投稿者と本文をPostとして保存する")
    void create_savesPostWithAuthorAndBody() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        postService.create("alice", "朝の共有です");

        verify(postRepository).save(argThat(post ->
                "alice".equals(post.getAuthor())
                        && "朝の共有です".equals(post.getBody())
                        && post.getCreatedAt() != null));
    }

    @Test
    @DisplayName("投稿作成_create_アバター色を指定すると選択した色を保存する")
    void create_whenAvatarColorSelected_savesSelectedColor() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        postService.create("alice", "朝の共有です", "blue");

        verify(postRepository).save(argThat(post ->
                "alice".equals(post.getAuthor())
                        && "朝の共有です".equals(post.getBody())
                        && "blue".equals(post.getAvatarColor())));
    }

    @Test
    @DisplayName("投稿作成_create_アバター色未選択ならデフォルト色を保存する")
    void create_whenAvatarColorNotSelected_savesDefaultColor() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        postService.create("alice", "朝の共有です", null);

        verify(postRepository).save(argThat(post ->
                "alice".equals(post.getAuthor())
                        && "朝の共有です".equals(post.getBody())
                        && "gray".equals(post.getAvatarColor())));
    }

    @Test
    @DisplayName("投稿詳細_findById_RepositoryからIDで投稿を取得して返す")
    void findById_returnsPostFromRepository() {
        Post expectedPost = new Post("alice", "朝の共有です", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(expectedPost));

        Optional<Post> post = postService.findById(1L);

        assertThat(post).containsSame(expectedPost);
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("いいね_初回_toggleLikeでいいねを保存する")
    void toggleLike_whenFirstLike_savesLike() {
        Post post = new Post("alice", "朝の共有です", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "aaaaaaaa")).willReturn(Optional.empty());

        boolean toggled = postService.toggleLike(1L, "aaaaaaaa");

        assertThat(toggled).isTrue();
        verify(postLikeRepository).save(argThat(like ->
                like.getPost() == post && "aaaaaaaa".equals(like.getClientHash())));
    }

    @Test
    @DisplayName("いいね_同じclientHashで再度toggleLike_いいねを削除する")
    void toggleLike_whenSameClientHashLikesAgain_deletesLike() {
        Post post = new Post("alice", "朝の共有です", Instant.parse("2026-05-23T10:00:00Z"));
        PostLike like = new PostLike(post, "aaaaaaaa");
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "aaaaaaaa")).willReturn(Optional.of(like));

        boolean toggled = postService.toggleLike(1L, "aaaaaaaa");

        assertThat(toggled).isTrue();
        verify(postLikeRepository).delete(like);
        verify(postLikeRepository, never()).save(any(PostLike.class));
    }

    @Test
    @DisplayName("いいね_存在しない投稿ID_toggleLikeはfalseを返す")
    void toggleLike_whenPostDoesNotExist_returnsFalse() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        boolean toggled = postService.toggleLike(999L, "aaaaaaaa");

        assertThat(toggled).isFalse();
        verify(postLikeRepository, never()).save(any(PostLike.class));
    }

    @Test
    @DisplayName("いいね数_countLikes_Repositoryから投稿IDで件数を取得する")
    void countLikes_returnsCountFromRepository() {
        given(postLikeRepository.countByPostId(1L)).willReturn(3L);

        long count = postService.countLikes(1L);

        assertThat(count).isEqualTo(3L);
        verify(postLikeRepository).countByPostId(1L);
    }
}
