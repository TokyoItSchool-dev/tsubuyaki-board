package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostComment;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostCommentRepository;
import com.example.tsubuyaki.repository.PostLikeRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostCommentRepository postCommentRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿登録_正常な入力の場合_投稿を保存する")
    void 投稿登録_正常な入力の場合_投稿を保存する() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
        Instant before = Instant.now();

        postService.createPost("alice", "本文です", "purple");

        Instant after = Instant.now();
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(captor.capture());
        Post savedPost = captor.getValue();
        assertThat(savedPost.getAuthor()).isEqualTo("alice");
        assertThat(savedPost.getBody()).isEqualTo("本文です");
        assertThat(savedPost.getAvatarColor()).isEqualTo("purple");
        assertThat(savedPost.getCreatedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("投稿検索_キーワードを指定した場合_本文検索結果を返す")
    void 投稿検索_キーワードを指定した場合_本文検索結果を返す() {
        List<Post> searchResults = List.of(
                new Post("alice", "検索キーワードを含む投稿", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postRepository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("検索キーワード"))
                .willReturn(searchResults);

        List<Post> posts = postService.searchPosts("検索キーワード");

        assertThat(posts).isSameAs(searchResults);
        then(postRepository).should().findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("検索キーワード");
    }

    @Test
    @DisplayName("投稿件数_論理削除されていない投稿数を返す")
    void 投稿件数_論理削除されていない投稿数を返す() {
        given(postRepository.countByDeletedAtIsNull()).willReturn(3L);

        long count = postService.countActivePosts();

        assertThat(count).isEqualTo(3L);
        then(postRepository).should().countByDeletedAtIsNull();
    }

    @Test
    @DisplayName("コメント一覧_投稿idに対応するコメントを新しい順で返す")
    void コメント一覧_投稿idに対応するコメントを新しい順で返す() {
        List<PostComment> comments = List.of(
                new PostComment(1L, "alice", "新しいコメント", "red", Instant.parse("2026-05-23T10:00:00Z")),
                new PostComment(1L, "bob", "古いコメント", "green", Instant.parse("2026-05-23T09:00:00Z"))
        );
        given(postCommentRepository.findByPostIdOrderByCreatedAtDesc(1L)).willReturn(comments);

        List<PostComment> foundComments = postService.findComments(1L);

        assertThat(foundComments).isSameAs(comments);
        then(postCommentRepository).should().findByPostIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("コメント件数_投稿idに対応するコメント件数を返す")
    void コメント件数_投稿idに対応するコメント件数を返す() {
        given(postCommentRepository.countByPostId(1L)).willReturn(2L);

        long count = postService.countComments(1L);

        assertThat(count).isEqualTo(2L);
        then(postCommentRepository).should().countByPostId(1L);
    }

    @Test
    @DisplayName("コメント投稿_正常な入力の場合_コメントを保存する")
    void コメント投稿_正常な入力の場合_コメントを保存する() {
        given(postCommentRepository.save(any(PostComment.class))).willAnswer(invocation -> invocation.getArgument(0));
        Instant before = Instant.now();

        postService.createComment(1L, "alice", "コメント本文", "purple");

        Instant after = Instant.now();
        ArgumentCaptor<PostComment> captor = ArgumentCaptor.forClass(PostComment.class);
        then(postCommentRepository).should().save(captor.capture());
        PostComment savedComment = captor.getValue();
        assertThat(savedComment.getPostId()).isEqualTo(1L);
        assertThat(savedComment.getAuthor()).isEqualTo("alice");
        assertThat(savedComment.getBody()).isEqualTo("コメント本文");
        assertThat(savedComment.getAvatarColor()).isEqualTo("purple");
        assertThat(savedComment.getCreatedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("コメント削除_コメントidに対応するコメントを削除する")
    void コメント削除_コメントidに対応するコメントを削除する() {
        postService.deleteComment(11L);

        then(postCommentRepository).should().deleteById(11L);
    }

    @Test
    @DisplayName("投稿削除_存在する投稿の場合_deletedAtに削除日時を設定する")
    void 投稿削除_存在する投稿の場合_deletedAtに削除日時を設定する() {
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        Instant before = Instant.now();

        postService.deletePost(1L);

        Instant after = Instant.now();
        assertThat(post.getDeletedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("いいね切り替え_未登録の場合_いいねを登録する")
    void いいね切り替え_未登録の場合_いいねを登録する() {
        given(postLikeRepository.existsByPostIdAndClientHash(1L, "abcd1234")).willReturn(false);

        postService.toggleLike(1L, "abcd1234");

        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        then(postLikeRepository).should().save(captor.capture());
        PostLike savedLike = captor.getValue();
        assertThat(savedLike.getPostId()).isEqualTo(1L);
        assertThat(savedLike.getClientHash()).isEqualTo("abcd1234");
    }

    @Test
    @DisplayName("いいね切り替え_同一clientHashが登録済みの場合_いいねを解除する")
    void いいね切り替え_同一clientHashが登録済みの場合_いいねを解除する() {
        given(postLikeRepository.existsByPostIdAndClientHash(1L, "abcd1234")).willReturn(true);

        postService.toggleLike(1L, "abcd1234");

        then(postLikeRepository).should().deleteByPostIdAndClientHash(1L, "abcd1234");
    }
}
