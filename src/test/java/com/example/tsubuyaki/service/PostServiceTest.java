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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
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
    @DisplayName("投稿一覧_最新投稿取得_Repositoryの新着50件取得結果を返す")
    void latest_returnsRepositoryLatest50() {
        List<Post> posts = List.of(
                new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> actual = postService.latest();

        assertThat(actual).isSameAs(posts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿検索_キーワードあり_Repositoryの本文部分一致検索結果を返す")
    void search_whenQueryHasText_returnsRepositorySearchResult() {
        List<Post> posts = List.of(
                new Post("alice", "abcを含む投稿", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postRepository.findByBodyContainingOrderByCreatedAtDesc("abc")).willReturn(posts);

        List<Post> actual = postService.search("abc");

        assertThat(actual).isSameAs(posts);
        verify(postRepository).findByBodyContainingOrderByCreatedAtDesc("abc");
        verify(postRepository, never()).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿検索_キーワード空_通常の新着50件を返す")
    void search_whenQueryIsEmpty_returnsLatest50() {
        List<Post> posts = List.of(
                new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> actual = postService.search("");

        assertThat(actual).isSameAs(posts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
        verify(postRepository, never()).findByBodyContainingOrderByCreatedAtDesc(anyString());
    }

    @Test
    @DisplayName("投稿作成_入力正常_Postに変換してRepositoryへ保存する")
    void create_whenValid_savesPost() {
        postService.create("alice", "本文です");

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post savedPost = captor.getValue();
        assertThat(savedPost.getAuthor()).isEqualTo("alice");
        assertThat(savedPost.getBody()).isEqualTo("本文です");
        assertThat(savedPost.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("投稿詳細_id指定_RepositoryのfindById結果を返す")
    void findById_returnsRepositoryResult() {
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T01:00:00Z"));
        given(postRepository.findById(42L)).willReturn(Optional.of(post));

        Optional<Post> actual = postService.findById(42L);

        assertThat(actual).containsSame(post);
        verify(postRepository).findById(42L);
    }

    @Test
    @DisplayName("投稿編集_存在するid_投稿を更新してRepositoryへ保存する")
    void update_whenPostExists_updatesAndSavesPost() {
        Post post = new Post("alice", "更新前本文です", Instant.parse("2026-05-23T01:00:00Z"));
        given(postRepository.findById(42L)).willReturn(Optional.of(post));

        Optional<Post> actual = postService.update(42L, "bob", "更新後本文です");

        assertThat(actual).containsSame(post);
        assertThat(post.getAuthor()).isEqualTo("bob");
        assertThat(post.getBody()).isEqualTo("更新後本文です");
        verify(postRepository).findById(42L);
        verify(postRepository).save(post);
    }

    @Test
    @DisplayName("投稿編集_存在しないid_保存せず空を返す")
    void update_whenPostDoesNotExist_returnsEmpty() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        Optional<Post> actual = postService.update(999L, "bob", "更新後本文です");

        assertThat(actual).isEmpty();
        verify(postRepository).findById(999L);
        verify(postRepository, never()).save(org.mockito.ArgumentMatchers.any(Post.class));
    }

    @Test
    @DisplayName("いいね_未いいねの場合_いいねを保存してtrueを返す")
    void toggleLike_whenNotLiked_savesLikeAndReturnsTrue() {
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T01:00:00Z"));
        given(postRepository.findById(42L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(42L, "abcd1234")).willReturn(Optional.empty());

        Optional<Boolean> actual = postService.toggleLike(42L, "abcd1234");

        assertThat(actual).contains(true);
        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        verify(postLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getPost()).isSameAs(post);
        assertThat(captor.getValue().getClientHash()).isEqualTo("abcd1234");
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("いいね_いいね済みの場合_いいねを削除してfalseを返す")
    void toggleLike_whenAlreadyLiked_deletesLikeAndReturnsFalse() {
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T01:00:00Z"));
        PostLike like = new PostLike(post, "abcd1234", Instant.parse("2026-05-23T02:00:00Z"));
        given(postRepository.findById(42L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(42L, "abcd1234")).willReturn(Optional.of(like));

        Optional<Boolean> actual = postService.toggleLike(42L, "abcd1234");

        assertThat(actual).contains(false);
        verify(postLikeRepository).delete(like);
        verify(postLikeRepository, never()).save(org.mockito.ArgumentMatchers.any(PostLike.class));
    }

    @Test
    @DisplayName("いいね_存在しない投稿の場合_保存削除せず空を返す")
    void toggleLike_whenPostDoesNotExist_returnsEmpty() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        Optional<Boolean> actual = postService.toggleLike(999L, "abcd1234");

        assertThat(actual).isEmpty();
        verify(postLikeRepository, never()).save(org.mockito.ArgumentMatchers.any(PostLike.class));
    }

    @Test
    @DisplayName("いいね数_投稿id指定_Repositoryの件数を返す")
    void countLikes_returnsRepositoryCount() {
        given(postLikeRepository.countByPostId(42L)).willReturn(3L);

        long actual = postService.countLikes(42L);

        assertThat(actual).isEqualTo(3L);
        verify(postLikeRepository).countByPostId(42L);
    }

    @Test
    @DisplayName("いいね状態_投稿idとclientHash指定_Repositoryの存在判定を返す")
    void likedBy_returnsRepositoryExistsResult() {
        given(postLikeRepository.existsByPostIdAndClientHash(42L, "abcd1234")).willReturn(true);

        boolean actual = postService.likedBy(42L, "abcd1234");

        assertThat(actual).isTrue();
        verify(postLikeRepository).existsByPostIdAndClientHash(42L, "abcd1234");
    }
}
