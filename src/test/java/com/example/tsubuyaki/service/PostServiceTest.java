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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
        List<Post> expected = List.of(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expected);

        List<Post> posts = postService.latest();

        assertThat(posts).isEqualTo(expected);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿検索_qあり_本文検索Repositoryを呼び結果を返す")
    void 投稿検索_qあり_本文検索Repositoryを呼び結果を返す() {
        List<Post> expected = List.of(new Post("alice", "hello world", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("hello")).willReturn(expected);

        List<Post> posts = postService.list("hello");

        assertThat(posts).isEqualTo(expected);
        verify(postRepository).findTop50ByBodyContainingOrderByCreatedAtDesc("hello");
        verify(postRepository, never()).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿検索_q空白_latestを返す")
    void 投稿検索_q空白_latestを返す() {
        List<Post> expected = List.of(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expected);

        List<Post> posts = postService.list("   ");

        assertThat(posts).isEqualTo(expected);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
        verify(postRepository, never()).findTop50ByBodyContainingOrderByCreatedAtDesc("   ");
    }

    @Test
    @DisplayName("投稿詳細_findById_Repositoryの検索結果を返す")
    void 投稿詳細_findById_Repositoryの検索結果を返す() {
        Post expected = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(expected));

        Optional<Post> post = postService.findById(1L);

        assertThat(post).contains(expected);
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("投稿登録_create_投稿をデフォルトアバター色でRepositoryへ保存する")
    void 投稿登録_create_投稿をデフォルトアバター色でRepositoryへ保存する() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
        Instant before = Instant.now();

        Post created = postService.create("alice", "hello");

        Instant after = Instant.now();
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post saved = captor.getValue();
        assertThat(created).isSameAs(saved);
        assertThat(saved.getAuthor()).isEqualTo("alice");
        assertThat(saved.getBody()).isEqualTo("hello");
        assertThat(saved.getAvatarColor()).isEqualTo("#3498db");
        assertThat(saved.getCreatedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("投稿登録_create_avatarColorつき投稿を保存する")
    void 投稿登録_create_avatarColorつき投稿を保存する() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        postService.create("alice", "hello", "#e91e63");

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getAvatarColor()).isEqualTo("#e91e63");
    }

    @Test
    @DisplayName("投稿登録_create_avatarColor空文字_デフォルト色で保存する")
    void 投稿登録_create_avatarColor空文字_デフォルト色で保存する() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        postService.create("alice", "hello", "   ");

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getAvatarColor()).isEqualTo("#3498db");
    }

    @Test
    @DisplayName("投稿登録_create_avatarColor不正値_デフォルト色で保存する")
    void 投稿登録_create_avatarColor不正値_デフォルト色で保存する() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        postService.create("alice", "hello", "red; color: transparent");

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getAvatarColor()).isEqualTo("#3498db");
    }

    @Test
    @DisplayName("いいねトグル_未いいね_Likeを保存する")
    void いいねトグル_未いいね_Likeを保存する() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "a1b2c3d4")).willReturn(Optional.empty());
        given(postLikeRepository.save(any(PostLike.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(postLikeRepository.countByPostId(1L)).willReturn(1L);

        long likeCount = postService.toggleLike(1L, "a1b2c3d4");

        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        verify(postLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getPost()).isEqualTo(post);
        assertThat(captor.getValue().getClientHash()).isEqualTo("a1b2c3d4");
        assertThat(likeCount).isEqualTo(1);
    }

    @Test
    @DisplayName("いいねトグル_既いいね_Likeを削除する")
    void いいねトグル_既いいね_Likeを削除する() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        PostLike existing = new PostLike(post, "a1b2c3d4");
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "a1b2c3d4")).willReturn(Optional.of(existing));
        given(postLikeRepository.countByPostId(1L)).willReturn(0L);

        long likeCount = postService.toggleLike(1L, "a1b2c3d4");

        verify(postLikeRepository).delete(existing);
        verify(postLikeRepository, never()).save(any(PostLike.class));
        assertThat(likeCount).isEqualTo(0);
    }

    @Test
    @DisplayName("いいねトグル_存在しない投稿id_ResponseStatusExceptionを投げる")
    void いいねトグル_存在しない投稿id_ResponseStatusExceptionを投げる() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.toggleLike(999L, "a1b2c3d4"))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));

        verifyNoInteractions(postLikeRepository);
    }

    @Test
    @DisplayName("いいね数_countLikes_Repositoryの件数を返す")
    void いいね数_countLikes_Repositoryの件数を返す() {
        given(postLikeRepository.countByPostId(1L)).willReturn(3L);

        long likeCount = postService.countLikes(1L);

        assertThat(likeCount).isEqualTo(3);
        verify(postLikeRepository).countByPostId(1L);
    }
}
