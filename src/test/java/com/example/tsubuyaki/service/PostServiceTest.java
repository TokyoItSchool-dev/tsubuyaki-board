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
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
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
    @DisplayName("投稿一覧_取得するとき_Repositoryの新着50件を返す")
    void latest_returnsRepositoryLatest50() {
        List<Post> posts = List.of(
                new Post("alice", "新しい投稿", LocalDateTime.parse("2026-05-23T10:00:00")),
                new Post("bob", "古い投稿", LocalDateTime.parse("2026-05-23T09:00:00")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> actual = postService.latest();

        assertThat(actual).isSameAs(posts);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿詳細_存在するidの場合_RepositoryのfindById結果を返す")
    void findById_whenPostExists_returnsRepositoryPost() {
        Post post = new Post("alice", "詳細本文", LocalDateTime.parse("2026-05-23T10:00:00"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Optional<Post> actual = postService.findById(1L);

        assertThat(actual).containsSame(post);
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("投稿詳細_存在しないidの場合_空のOptionalを返す")
    void findById_whenPostDoesNotExist_returnsEmptyOptional() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        Optional<Post> actual = postService.findById(999L);

        assertThat(actual).isEmpty();
        verify(postRepository).findById(999L);
    }

    @Test
    @DisplayName("投稿詳細_存在するidの場合_いいね数と自分のいいね状態を返す")
    void findDetail_whenPostExists_returnsPostWithLikeStatus() {
        Post post = new Post("alice", "詳細本文", LocalDateTime.parse("2026-05-23T10:00:00"));
        PostLike like = new PostLike(post, "a1b2c3d4", LocalDateTime.now());
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.countByPostId(1L)).willReturn(3L);
        given(postLikeRepository.findByPostIdAndClientHash(1L, "a1b2c3d4")).willReturn(Optional.of(like));

        Optional<PostDetail> actual = postService.findDetail(1L, "a1b2c3d4");

        assertThat(actual).isPresent();
        assertThat(actual.get().post()).isSameAs(post);
        assertThat(actual.get().likeCount()).isEqualTo(3);
        assertThat(actual.get().liked()).isTrue();
        verify(postRepository).findById(1L);
        verify(postLikeRepository).countByPostId(1L);
        verify(postLikeRepository).findByPostIdAndClientHash(1L, "a1b2c3d4");
    }

    @Test
    @DisplayName("投稿詳細_存在しないidの場合_空のOptionalを返す")
    void findDetail_whenPostDoesNotExist_returnsEmptyOptional() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        Optional<PostDetail> actual = postService.findDetail(999L, "a1b2c3d4");

        assertThat(actual).isEmpty();
        verify(postRepository).findById(999L);
        verifyNoInteractions(postLikeRepository);
    }

    @Test
    @DisplayName("いいねトグル_未いいねの場合_いいねを登録してliked_trueを返す")
    void toggleLike_whenNotLiked_savesLikeAndReturnsLikedTrue() {
        Post post = new Post("alice", "本文", LocalDateTime.parse("2026-05-23T10:00:00"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "a1b2c3d4")).willReturn(Optional.empty());

        Optional<LikeToggleResult> actual = postService.toggleLike(1L, "a1b2c3d4");

        assertThat(actual).contains(new LikeToggleResult(true));
        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        verify(postLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getPost()).isSameAs(post);
        assertThat(captor.getValue().getClientHash()).isEqualTo("a1b2c3d4");
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("いいねトグル_いいね済みの場合_いいねを削除してliked_falseを返す")
    void toggleLike_whenAlreadyLiked_deletesLikeAndReturnsLikedFalse() {
        Post post = new Post("alice", "本文", LocalDateTime.parse("2026-05-23T10:00:00"));
        PostLike like = new PostLike(post, "a1b2c3d4", LocalDateTime.now());
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "a1b2c3d4")).willReturn(Optional.of(like));

        Optional<LikeToggleResult> actual = postService.toggleLike(1L, "a1b2c3d4");

        assertThat(actual).contains(new LikeToggleResult(false));
        verify(postLikeRepository).delete(like);
    }

    @Test
    @DisplayName("いいねトグル_存在しない投稿の場合_空のOptionalを返す")
    void toggleLike_whenPostDoesNotExist_returnsEmptyOptional() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        Optional<LikeToggleResult> actual = postService.toggleLike(999L, "a1b2c3d4");

        assertThat(actual).isEmpty();
        verify(postRepository).findById(999L);
        verifyNoInteractions(postLikeRepository);
    }

    @Test
    @DisplayName("投稿登録_投稿データを登録するとき_Repositoryに投稿者本文作成日時を渡す")
    void create_savesPostWithAuthorBodyAndCreatedAt() {
        postService.create("alice", "本文");

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post savedPost = captor.getValue();
        assertThat(savedPost.getAuthor()).isEqualTo("alice");
        assertThat(savedPost.getBody()).isEqualTo("本文");
        assertThat(savedPost.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("投稿登録_Repository保存に失敗した場合_投稿登録例外を送出する")
    void create_whenRepositorySaveFails_throwsPostRegistrationException() {
        given(postRepository.save(org.mockito.ArgumentMatchers.any(Post.class)))
                .willThrow(new DataAccessResourceFailureException("DB error"));

        assertThatThrownBy(() -> postService.create("alice", "本文"))
                .isInstanceOf(PostRegistrationException.class)
                .hasMessage("投稿の登録に失敗しました");
    }
}
