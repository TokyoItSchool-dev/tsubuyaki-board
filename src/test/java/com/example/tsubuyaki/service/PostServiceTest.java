package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.domain.PostLikeId;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.web.dto.PostView;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    private PostService postService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-05-23T10:00:00Z"), ZoneOffset.UTC);
        this.postService = new PostService(postRepository, postLikeRepository, clock);
    }

    @Test
    @DisplayName("投稿一覧_latest_いいね0件として新着50件を返す")
    void latest_whenNoLikes_returnsLatestPostsWithZeroLikeCount() {
        List<Post> posts = List.of(
                new Post(1L, "alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(posts);
        given(postLikeRepository.findCountsByPostIdIn(List.of(1L))).willReturn(List.of());

        List<PostView> actual = postService.latest();

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0).getId()).isEqualTo(1L);
        assertThat(actual.get(0).getLikeCount()).isZero();
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
        verify(postLikeRepository).findCountsByPostIdIn(List.of(1L));
    }

    @Test
    @DisplayName("投稿一覧_latest_複数投稿のいいね数を取得して返す")
    void latest_whenMultiplePosts_returnsLikeCounts() {
        List<Post> posts = List.of(
                new Post(1L, "alice", "hello", Instant.parse("2026-05-23T10:00:00Z")),
                new Post(2L, "bob", "hi", Instant.parse("2026-05-23T11:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(posts);
        given(postLikeRepository.findCountsByPostIdIn(List.of(1L, 2L))).willReturn(List.of(
                likeCount(1L, 2L),
                likeCount(2L, 5L)));

        List<PostView> actual = postService.latest();

        assertThat(actual).extracting(PostView::getId).containsExactly(1L, 2L);
        assertThat(actual).extracting(PostView::getLikeCount).containsExactly(2L, 5L);
    }

    @Test
    @DisplayName("投稿詳細_findById_Repositoryの検索結果を返す")
    void findById_returnsRepositoryResult() {
        Post post = new Post(1L, "alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.countByIdPostId(1L)).willReturn(3L);

        Optional<PostView> actual = postService.findById(1L);

        assertThat(actual).get()
                .satisfies(postView -> {
                    assertThat(postView.getId()).isEqualTo(1L);
                    assertThat(postView.getLikeCount()).isEqualTo(3L);
                });
        verify(postRepository).findById(1L);
        verify(postLikeRepository).countByIdPostId(1L);
    }

    @Test
    @DisplayName("投稿詳細_findById_clientHashがいいね済みならtrueを返す")
    void findById_whenClientHashAlreadyLiked_returnsLikedByCurrentClient() {
        Post post = new Post(1L, "alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        PostLikeId id = new PostLikeId(1L, "abcd1234");
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.countByIdPostId(1L)).willReturn(3L);
        given(postLikeRepository.existsById(id)).willReturn(true);

        Optional<PostView> actual = postService.findById(1L, "abcd1234");

        assertThat(actual).get()
                .satisfies(postView -> {
                    assertThat(postView.getLikeCount()).isEqualTo(3L);
                    assertThat(postView.isLikedByCurrentClient()).isTrue();
                });
        verify(postLikeRepository).existsById(id);
    }

    @Test
    @DisplayName("いいね_toggleLike_clientHashがある場合_いいね数が減る")
    void toggleLike_whenClientHashExists_decreasesLikeCount() {
        Post post = new Post(1L, "alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        PostLikeId id = new PostLikeId(1L, "abcd1234");
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.existsById(id)).willReturn(true);
        given(postLikeRepository.countByIdPostId(1L)).willReturn(2L);

        Optional<PostView> actual = postService.toggleLike(1L, "abcd1234");

        assertThat(actual).get()
                .extracting(PostView::getLikeCount)
                .isEqualTo(2L);
        verify(postLikeRepository).deleteById(id);
        verify(postLikeRepository, never()).save(org.mockito.ArgumentMatchers.any(PostLike.class));
    }

    @Test
    @DisplayName("いいね_toggleLike_clientHashがない場合_いいね数が増える")
    void toggleLike_whenClientHashDoesNotExist_increasesLikeCount() {
        Post post = new Post(1L, "alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        PostLikeId id = new PostLikeId(1L, "abcd1234");
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.existsById(id)).willReturn(false);
        given(postLikeRepository.countByIdPostId(1L)).willReturn(1L);

        Optional<PostView> actual = postService.toggleLike(1L, "abcd1234");

        assertThat(actual).get()
                .extracting(PostView::getLikeCount)
                .isEqualTo(1L);
        var captor = forClass(PostLike.class);
        verify(postLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(id);
        assertThat(captor.getValue().getCreatedAt()).isEqualTo(Instant.parse("2026-05-23T10:00:00Z"));
    }

    @Test
    @DisplayName("投稿作成_create_最大IDの次のIDと現在日時を設定してRepositoryに保存する")
    void create_savesPostWithNextIdAndCurrentTimestamp() {
        given(postRepository.findMaxId()).willReturn(10L);

        postService.create("alice", "hello");

        var captor = forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(11L);
        assertThat(captor.getValue().getAuthor()).isEqualTo("alice");
        assertThat(captor.getValue().getBody()).isEqualTo("hello");
        assertThat(captor.getValue().getCreatedAt()).isEqualTo(Instant.parse("2026-05-23T10:00:00Z"));
    }

    private static PostLikeRepository.PostLikeCount likeCount(Long postId, Long likeCount) {
        return new PostLikeRepository.PostLikeCount() {
            @Override
            public Long getPostId() {
                return postId;
            }

            @Override
            public Long getLikeCount() {
                return likeCount;
            }
        };
    }
}
