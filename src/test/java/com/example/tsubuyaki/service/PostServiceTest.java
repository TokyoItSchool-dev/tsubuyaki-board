package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.domain.User;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.UserRepository;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿取得_存在するidのとき_Repositoryの結果を返す")
    void 投稿取得_存在するidのとき_Repositoryの結果を返す() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Optional<Post> result = postService.findById(1L);

        assertThat(result).contains(post);
    }

    @Test
    @DisplayName("投稿検索_キーワードあり_本文検索Repositoryを呼ぶ")
    void 投稿検索_キーワードあり_本文検索Repositoryを呼ぶ() {
        List<Post> posts = List.of(new Post("alice", "xxx を含む投稿",
                Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("xxx")).willReturn(posts);

        List<Post> result = postService.searchByBody("xxx");

        assertThat(result).isSameAs(posts);
        verify(postRepository).findTop50ByBodyContainingOrderByCreatedAtDesc("xxx");
        verify(postRepository, never()).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿検索_キーワードが空白のみ_最新50件を返す")
    void 投稿検索_キーワードが空白のみ_最新50件を返す() {
        List<Post> posts = List.of(new Post("alice", "最新投稿",
                Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> result = postService.searchByBody("   ");

        assertThat(result).isSameAs(posts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
        verify(postRepository, never()).findTop50ByBodyContainingOrderByCreatedAtDesc(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("投稿作成_投稿者と本文とアバター色を指定したとき_投稿を保存する")
    void 投稿作成_投稿者と本文とアバター色を指定したとき_投稿を保存する() {
        given(postRepository.save(org.mockito.ArgumentMatchers.any(Post.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(userRepository.findByName("alice")).willReturn(Optional.empty());
        given(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        Post created = postService.create("alice", "こんにちは", "#ef4444");

        assertThat(created.getAuthor()).isEqualTo("alice");
        assertThat(created.getBody()).isEqualTo("こんにちは");
        assertThat(created.getAvatarColor()).isEqualTo("#ef4444");
        assertThat(created.getUser().getName()).isEqualTo("alice");
        assertThat(created.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("投稿作成_新規ユーザーでアバター色が空白のとき_デフォルト色で保存する")
    void 投稿作成_新規ユーザーでアバター色が空白のとき_デフォルト色で保存する() {
        given(postRepository.save(org.mockito.ArgumentMatchers.any(Post.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(userRepository.findByName("alice")).willReturn(Optional.empty());
        given(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        Post created = postService.create("alice", "こんにちは", "   ");

        assertThat(created.getAvatarColor()).isEqualTo(User.DEFAULT_AVATAR_COLOR);
    }

    @Test
    @DisplayName("投稿作成_既存ユーザーでアバター色が空白のとき_既存色を維持する")
    void 投稿作成_既存ユーザーでアバター色が空白のとき_既存色を維持する() {
        User user = new User("alice", "#22c55e");
        given(userRepository.findByName("alice")).willReturn(Optional.of(user));
        given(postRepository.save(org.mockito.ArgumentMatchers.any(Post.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        Post created = postService.create("alice", "こんにちは", "");

        assertThat(created.getUser()).isSameAs(user);
        assertThat(created.getAvatarColor()).isEqualTo("#22c55e");
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    @DisplayName("いいね切替_未いいねのとき_いいねを保存する")
    void いいね切替_未いいねのとき_いいねを保存する() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postLikeRepository.existsByPostIdAndClientHash(1L, "abcd1234")).willReturn(false);
        given(postRepository.getReferenceById(1L)).willReturn(post);

        postService.toggleLike(1L, "abcd1234");

        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        verify(postLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getPost()).isSameAs(post);
        assertThat(captor.getValue().getClientHash()).isEqualTo("abcd1234");
        verify(postLikeRepository, never()).deleteByPostIdAndClientHash(1L, "abcd1234");
    }

    @Test
    @DisplayName("いいね切替_いいね済みのとき_いいねを削除する")
    void いいね切替_いいね済みのとき_いいねを削除する() {
        given(postLikeRepository.existsByPostIdAndClientHash(1L, "abcd1234")).willReturn(true);

        postService.toggleLike(1L, "abcd1234");

        verify(postLikeRepository).deleteByPostIdAndClientHash(1L, "abcd1234");
        verify(postRepository, never()).getReferenceById(1L);
        verify(postLikeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("いいね数取得_投稿idを指定したとき_Repositoryの件数を返す")
    void いいね数取得_投稿idを指定したとき_Repositoryの件数を返す() {
        given(postLikeRepository.countByPostId(1L)).willReturn(3L);

        long count = postService.countLikes(1L);

        assertThat(count).isEqualTo(3L);
    }
}
