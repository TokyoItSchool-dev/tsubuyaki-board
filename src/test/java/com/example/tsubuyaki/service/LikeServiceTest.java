package com.example.tsubuyaki.service;

import com.example.tsubuyaki.repository.LikeEntity;
import com.example.tsubuyaki.repository.LikeRepository;
import com.example.tsubuyaki.repository.PostEntity;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Test
    @DisplayName("いいね_toggleLike_未登録ならLikeを保存する")
    void いいね_toggleLike_未登録ならLikeを保存する() {
        LikeService likeService = new LikeService(likeRepository, postRepository);
        given(likeRepository.findByPostIdAndClientHash(1L, "abc12345")).willReturn(Optional.empty());
        given(postRepository.findById(1L)).willReturn(Optional.of(
                new PostEntity(1L, "alice", "BLUE", "本文", Instant.parse("2026-06-26T09:00:00Z"))));

        likeService.toggleLike(1L, "abc12345");

        verify(likeRepository).saveAndFlush(any(LikeEntity.class));
        verify(likeRepository, never()).delete(any(LikeEntity.class));
    }

    @Test
    @DisplayName("いいね_toggleLike_登録済みならLikeを削除する")
    void いいね_toggleLike_登録済みならLikeを削除する() {
        LikeService likeService = new LikeService(likeRepository, postRepository);
        LikeEntity like = new LikeEntity(1L, "abc12345");
        given(likeRepository.findByPostIdAndClientHash(1L, "abc12345")).willReturn(Optional.of(like));

        likeService.toggleLike(1L, "abc12345");

        verify(likeRepository).delete(like);
        verify(likeRepository, never()).saveAndFlush(any(LikeEntity.class));
        verify(postRepository, never()).findById(1L);
    }

    @Test
    @DisplayName("いいね_toggleLike_投稿が存在しない場合は専用例外を投げる")
    void いいね_toggleLike_投稿が存在しない場合は専用例外を投げる() {
        LikeService likeService = new LikeService(likeRepository, postRepository);
        given(likeRepository.findByPostIdAndClientHash(999L, "abc12345")).willReturn(Optional.empty());
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.toggleLike(999L, "abc12345"))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("999");

        verify(likeRepository, never()).saveAndFlush(any(LikeEntity.class));
    }

    @Test
    @DisplayName("いいね_toggleLike_同時登録で一意制約に衝突したら既存Likeを削除する")
    void いいね_toggleLike_同時登録で一意制約に衝突したら既存Likeを削除する() {
        LikeService likeService = new LikeService(likeRepository, postRepository);
        LikeEntity concurrentLike = new LikeEntity(1L, "abc12345");
        given(likeRepository.findByPostIdAndClientHash(1L, "abc12345"))
                .willReturn(Optional.empty(), Optional.of(concurrentLike));
        given(postRepository.findById(1L)).willReturn(Optional.of(
                new PostEntity(1L, "alice", "BLUE", "本文", Instant.parse("2026-06-26T09:00:00Z"))));
        given(likeRepository.saveAndFlush(any(LikeEntity.class)))
                .willThrow(new DataIntegrityViolationException("unique constraint"));

        likeService.toggleLike(1L, "abc12345");

        verify(likeRepository).saveAndFlush(any(LikeEntity.class));
        verify(likeRepository).delete(concurrentLike);
    }

    @Test
    @DisplayName("いいね件数_countByPostId_投稿が存在するとき件数を返す")
    void いいね件数_countByPostId_投稿が存在するとき件数を返す() {
        LikeService likeService = new LikeService(likeRepository, postRepository);
        given(postRepository.findById(1L)).willReturn(Optional.of(
                new PostEntity(1L, "alice", "BLUE", "本文", Instant.parse("2026-06-26T09:00:00Z"))));
        given(likeRepository.countByPostId(1L)).willReturn(2L);

        long actual = likeService.countByPostId(1L);

        assertThat(actual).isEqualTo(2L);
        verify(postRepository).findById(1L);
        verify(likeRepository).countByPostId(1L);
    }

    @Test
    @DisplayName("いいね件数_countByPostId_投稿が存在しない場合は専用例外を投げる")
    void いいね件数_countByPostId_投稿が存在しない場合は専用例外を投げる() {
        LikeService likeService = new LikeService(likeRepository, postRepository);
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.countByPostId(999L))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("999");

        verify(likeRepository, never()).countByPostId(999L);
    }
}
