package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.domain.Post;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_latest_Repositoryの新着50件取得結果を返す")
    void 投稿一覧_latest_Repositoryの新着50件取得結果を返す() {
        List<Post> posts = List.of(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> latest = postService.latest();

        assertThat(latest).isSameAs(posts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿一覧_latestWithLikes_いいね数とclientHashのいいね状態を付与する")
    void 投稿一覧_latestWithLikes_いいね数とclientHashのいいね状態を付与する() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        org.springframework.test.util.ReflectionTestUtils.setField(post, "id", 42L);
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(List.of(post));
        given(postLikeRepository.countByPostId(42L)).willReturn(2L);
        given(postLikeRepository.existsByPostIdAndClientHash(42L, "abcdef12")).willReturn(true);

        List<Post> latest = postService.latestWithLikes("abcdef12");

        assertThat(latest).containsExactly(post);
        assertThat(latest.get(0).getLikeCount()).isEqualTo(2);
        assertThat(latest.get(0).isLiked()).isTrue();
        verify(postLikeRepository).countByPostId(42L);
        verify(postLikeRepository).existsByPostIdAndClientHash(42L, "abcdef12");
    }

    @Test
    @DisplayName("投稿検索_searchWithLikes_本文部分一致の新着50件にいいね状態を付与する")
    void 投稿検索_searchWithLikes_本文部分一致の新着50件にいいね状態を付与する() {
        Post post = new Post("alice", "検索対象の本文", Instant.parse("2026-05-23T10:00:00Z"));
        org.springframework.test.util.ReflectionTestUtils.setField(post, "id", 42L);
        given(postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("検索対象")).willReturn(List.of(post));
        given(postLikeRepository.countByPostId(42L)).willReturn(3L);
        given(postLikeRepository.existsByPostIdAndClientHash(42L, "abcdef12")).willReturn(false);

        List<Post> found = postService.searchWithLikes("検索対象", "abcdef12");

        assertThat(found).containsExactly(post);
        assertThat(found.get(0).getLikeCount()).isEqualTo(3);
        assertThat(found.get(0).isLiked()).isFalse();
        verify(postRepository).findTop50ByBodyContainingOrderByCreatedAtDesc("検索対象");
        verify(postLikeRepository).countByPostId(42L);
        verify(postLikeRepository).existsByPostIdAndClientHash(42L, "abcdef12");
    }

    @Test
    @DisplayName("投稿検索_countSearchResults_本文部分一致の件数を返す")
    void 投稿検索_countSearchResults_本文部分一致の件数を返す() {
        given(postRepository.countByBodyContaining("検索対象")).willReturn(55L);

        long count = postService.countSearchResults("検索対象");

        assertThat(count).isEqualTo(55);
        verify(postRepository).countByBodyContaining("検索対象");
    }

    @Test
    @DisplayName("投稿詳細_findById_Repositoryのid検索結果を返す")
    void 投稿詳細_findById_RepositoryのId検索結果を返す() {
        Post post = new Post("alice", "本文", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findById(42L)).willReturn(Optional.of(post));

        Optional<Post> found = postService.findById(42L);

        assertThat(found).containsSame(post);
        verify(postRepository).findById(42L);
    }

    @Test
    @DisplayName("投稿詳細_findByIdWithLike_いいね数とclientHashのいいね状態を付与する")
    void 投稿詳細_findByIdWithLike_いいね数とclientHashのいいね状態を付与する() {
        Post post = new Post("alice", "本文", Instant.parse("2026-05-23T10:00:00Z"));
        org.springframework.test.util.ReflectionTestUtils.setField(post, "id", 42L);
        given(postRepository.findById(42L)).willReturn(Optional.of(post));
        given(postLikeRepository.countByPostId(42L)).willReturn(1L);
        given(postLikeRepository.existsByPostIdAndClientHash(42L, "abcdef12")).willReturn(false);

        Optional<Post> found = postService.findByIdWithLike(42L, "abcdef12");

        assertThat(found).containsSame(post);
        assertThat(found.orElseThrow().getLikeCount()).isEqualTo(1);
        assertThat(found.orElseThrow().isLiked()).isFalse();
    }

    @Test
    @DisplayName("いいねトグル_未いいねの場合_いいねを保存する")
    void いいねトグル_未いいねの場合_いいねを保存する() {
        Post post = new Post("alice", "本文", Instant.parse("2026-05-23T10:00:00Z"));
        org.springframework.test.util.ReflectionTestUtils.setField(post, "id", 42L);
        given(postRepository.findById(42L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(42L, "abcdef12")).willReturn(Optional.empty());

        Optional<Post> toggled = postService.toggleLike(42L, "abcdef12");

        assertThat(toggled).containsSame(post);
        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        verify(postLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getPost()).isSameAs(post);
        assertThat(captor.getValue().getClientHash()).isEqualTo("abcdef12");
        verify(postLikeRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("いいねトグル_同一clientHashが再押下した場合_いいねを削除する")
    void いいねトグル_同一clientHashが再押下した場合_いいねを削除する() {
        Post post = new Post("alice", "本文", Instant.parse("2026-05-23T10:00:00Z"));
        org.springframework.test.util.ReflectionTestUtils.setField(post, "id", 42L);
        PostLike like = new PostLike(post, "abcdef12", Instant.parse("2026-05-23T10:01:00Z"));
        given(postRepository.findById(42L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(42L, "abcdef12")).willReturn(Optional.of(like));

        Optional<Post> toggled = postService.toggleLike(42L, "abcdef12");

        assertThat(toggled).containsSame(post);
        verify(postLikeRepository).delete(like);
        verify(postLikeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("投稿作成_create_投稿者と本文を現在日時つきで保存する")
    void 投稿作成_create_投稿者と本文を現在日時つきで保存する() {
        Instant before = Instant.now();

        postService.create("alice", "本文");

        Instant after = Instant.now();
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post savedPost = captor.getValue();
        assertThat(savedPost.getAuthor()).isEqualTo("alice");
        assertThat(savedPost.getBody()).isEqualTo("本文");
        assertThat(savedPost.getCreatedAt()).isBetween(before, after);
    }
}
