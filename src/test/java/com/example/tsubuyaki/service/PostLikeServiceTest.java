package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    @Mock
    private PostLikeRepository postLikeRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T04:30:00Z"), ZoneOffset.UTC);

    @Test
    @DisplayName("いいね_未登録clientHashの場合_いいねを登録する")
    void いいね_未登録clientHashの場合_いいねを登録する() {
        PostLikeService postLikeService = new PostLikeService(postLikeRepository, clock);
        given(postLikeRepository.existsByPostIdAndClientHash(1L, "abc12345")).willReturn(false);

        postLikeService.toggle(1L, "abc12345");

        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        then(postLikeRepository).should().save(captor.capture());
        PostLike saved = captor.getValue();
        assertThat(saved.getPostId()).isEqualTo(1L);
        assertThat(saved.getClientHash()).isEqualTo("abc12345");
        assertThat(saved.getCreatedAt()).isEqualTo(Instant.parse("2026-07-01T04:30:00Z"));
    }

    @Test
    @DisplayName("いいね_登録済みclientHashの場合_いいねを削除する")
    void いいね_登録済みclientHashの場合_いいねを削除する() {
        PostLikeService postLikeService = new PostLikeService(postLikeRepository, clock);
        given(postLikeRepository.existsByPostIdAndClientHash(1L, "abc12345")).willReturn(true);

        postLikeService.toggle(1L, "abc12345");

        then(postLikeRepository).should().deleteByPostIdAndClientHash(1L, "abc12345");
    }
}
