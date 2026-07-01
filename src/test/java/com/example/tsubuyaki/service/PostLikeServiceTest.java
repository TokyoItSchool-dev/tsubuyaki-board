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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    // 投稿の存在確認に使うRepositoryをモック化する。
    @Mock
    private PostRepository postRepository;

    // いいねの検索・保存・削除に使うRepositoryをモック化する。
    @Mock
    private PostLikeRepository postLikeRepository;

    // モックRepositoryを注入したServiceをテスト対象にする。
    @InjectMocks
    private PostLikeService postLikeService;

    @Test
    @DisplayName("いいね_初回押下_いいねを追加する")
    void いいね_初回押下_いいねを追加する() {
        Post post = postWithId(42L);
        given(postRepository.findById(42L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(42L, "abcd1234")).willReturn(Optional.empty());

        boolean liked = postLikeService.toggleLike(42L, "abcd1234");

        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        verify(postLikeRepository).save(captor.capture());
        assertThat(liked).isTrue();
        assertThat(captor.getValue().getPost()).isSameAs(post);
        assertThat(captor.getValue().getClientHash()).isEqualTo("abcd1234");
    }

    @Test
    @DisplayName("いいね_同一clientHashで再押下_いいねを解除する")
    void いいね_同一clientHashで再押下_いいねを解除する() {
        Post post = postWithId(42L);
        PostLike existingLike = new PostLike(post, "abcd1234", LocalDateTime.of(2026, 5, 23, 10, 0));
        given(postRepository.findById(42L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(42L, "abcd1234"))
                .willReturn(Optional.of(existingLike));

        boolean liked = postLikeService.toggleLike(42L, "abcd1234");

        verify(postLikeRepository).delete(existingLike);
        verify(postLikeRepository, never()).save(any(PostLike.class));
        assertThat(liked).isFalse();
    }

    @Test
    @DisplayName("いいね_異なるclientHash_別ユーザーとして追加する")
    void いいね_異なるclientHash_別ユーザーとして追加する() {
        Post post = postWithId(42L);
        given(postRepository.findById(42L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(42L, "bbbb2222")).willReturn(Optional.empty());

        postLikeService.toggleLike(42L, "bbbb2222");

        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        verify(postLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getClientHash()).isEqualTo("bbbb2222");
    }

    @Test
    @DisplayName("いいね_存在しない投稿ID_PostNotFoundExceptionを投げる")
    void いいね_存在しない投稿ID_PostNotFoundExceptionを投げる() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postLikeService.toggleLike(999L, "abcd1234"))
                .isInstanceOf(PostNotFoundException.class);

        verify(postLikeRepository, never()).findByPostIdAndClientHash(999L, "abcd1234");
    }

    @Test
    @DisplayName("いいね数_countLikes_Repositoryから件数を取得する")
    void いいね数_countLikes_Repositoryから件数を取得する() {
        given(postLikeRepository.countByPostId(42L)).willReturn(3L);

        long likeCount = postLikeService.countLikes(42L);

        assertThat(likeCount).isEqualTo(3L);
        verify(postLikeRepository).countByPostId(42L);
    }

    private Post postWithId(Long id) {
        Post post = new Post("alice", "本文です", LocalDateTime.of(2026, 5, 23, 10, 0));
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }
}
