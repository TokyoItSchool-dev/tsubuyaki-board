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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @InjectMocks
    private PostLikeService postLikeService;

    @Test
    @DisplayName("いいねService_未登録clientHash_新しいいいねを保存する")
    void いいねService_未登録clientHash_新しいいいねを保存する() {
        // 存在する投稿idとして扱うため、PostRepositoryが投稿を返すように設定する。
        Post post = new Post("alice", "Serviceテスト", LocalDateTime.parse("2026-06-01T09:00:00"));
        given(postRepository.findByIdForUpdate(1L)).willReturn(Optional.of(post));
        // 未登録clientHashとして扱うため、PostLikeRepositoryが空を返すように設定する。
        given(postLikeRepository.findByPostIdAndClientHash(1L, "abcd1234")).willReturn(Optional.empty());
        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);

        postLikeService.toggle(1L, "abcd1234");

        // 未登録clientHashの場合、新しいPostLikeが保存されることを検証する。
        verify(postLikeRepository).save(captor.capture());
        // 保存対象のclientHashがServiceへ渡した値と一致することを検証する。
        assertThat(captor.getValue().getClientHash()).isEqualTo("abcd1234");
        // 未登録clientHashの場合、削除処理が呼ばれないことを検証する。
        verify(postLikeRepository, never()).delete(org.mockito.ArgumentMatchers.any(PostLike.class));
    }

    @Test
    @DisplayName("いいねService_登録済みclientHash_既存いいねを削除する")
    void いいねService_登録済みclientHash_既存いいねを削除する() {
        // 存在する投稿idとして扱うため、PostRepositoryが投稿を返すように設定する。
        Post post = new Post("alice", "Serviceテスト", LocalDateTime.parse("2026-06-01T09:00:00"));
        given(postRepository.findByIdForUpdate(1L)).willReturn(Optional.of(post));
        // 登録済みclientHashとして扱うため、既存PostLikeをRepositoryから返す。
        PostLike existingLike = new PostLike(post, "abcd1234", LocalDateTime.parse("2026-06-01T10:00:00"));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "abcd1234")).willReturn(Optional.of(existingLike));

        postLikeService.toggle(1L, "abcd1234");

        // 登録済みclientHashの場合、既存PostLikeが削除されることを検証する。
        verify(postLikeRepository).delete(existingLike);
        // 登録済みclientHashの場合、新規保存が呼ばれないことを検証する。
        verify(postLikeRepository, never()).save(org.mockito.ArgumentMatchers.any(PostLike.class));
    }

    @Test
    @DisplayName("いいねService_存在しない投稿id_PostNotFoundExceptionを投げる")
    void いいねService_存在しない投稿id_PostNotFoundExceptionを投げる() {
        // 存在しない投稿idとして扱うため、PostRepositoryが空を返すように設定する。
        given(postRepository.findByIdForUpdate(999L)).willReturn(Optional.empty());

        // 存在しない投稿idの場合、404に対応するPostNotFoundExceptionが投げられることを検証する。
        assertThatThrownBy(() -> postLikeService.toggle(999L, "abcd1234"))
                .isInstanceOf(PostNotFoundException.class);
        // 存在しない投稿idの場合、いいね検索が呼ばれないことを検証する。
        verify(postLikeRepository, never()).findByPostIdAndClientHash(999L, "abcd1234");
        // 存在しない投稿idの場合、いいね保存が呼ばれないことを検証する。
        verify(postLikeRepository, never()).save(org.mockito.ArgumentMatchers.any(PostLike.class));
    }
}
