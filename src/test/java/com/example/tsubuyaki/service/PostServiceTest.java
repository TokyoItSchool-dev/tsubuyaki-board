package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
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
