package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class LikeServiceTest {

    private final PostLikeRepository postLikeRepository = mock(PostLikeRepository.class);

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T05:00:00Z"), ZoneOffset.UTC);

    private final LikeService likeService = new LikeService(postLikeRepository, clock);

    @Test
    @DisplayName("いいね_未いいねの場合_いいねを登録する")
    void いいね_未いいねの場合_いいねを登録する() {
        given(postLikeRepository.findByPostIdAndClientHash(1L, "abc12345"))
                .willReturn(Optional.empty());

        likeService.toggle(1L, "abc12345");

        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        verify(postLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getPostId()).isEqualTo(1L);
        assertThat(captor.getValue().getClientHash()).isEqualTo("abc12345");
        assertThat(captor.getValue().getCreatedAt()).isEqualTo(Instant.parse("2026-07-01T05:00:00Z"));
    }

    @Test
    @DisplayName("いいね_同じclientHashが再度POST_いいねを解除する")
    void いいね_同じclientHashが再度POST_いいねを解除する() {
        PostLike existing = new PostLike(1L, "abc12345", Instant.parse("2026-07-01T05:00:00Z"));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "abc12345"))
                .willReturn(Optional.of(existing));

        likeService.toggle(1L, "abc12345");

        verify(postLikeRepository).delete(existing);
        verify(postLikeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("いいね数_countByPostId_Repositoryの件数を返す")
    void いいね数_countByPostId_Repositoryの件数を返す() {
        given(postLikeRepository.countByPostId(1L)).willReturn(2L);

        long count = likeService.countByPostId(1L);

        assertThat(count).isEqualTo(2L);
        verify(postLikeRepository).countByPostId(1L);
    }
}
