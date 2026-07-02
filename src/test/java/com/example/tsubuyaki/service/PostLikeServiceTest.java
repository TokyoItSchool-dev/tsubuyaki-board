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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    private PostService postService;

    @Test
    @DisplayName("Service_いいね_未いいねなら保存してtrueを返す")
    void いいね_未いいねなら保存してtrueを返す() {
        Post post = new Post("alice", "S1 の投稿", Instant.parse("2026-06-26T12:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.existsByPostIdAndClientHash(1L, "a1b2c3d4")).willReturn(false);

        Optional<Boolean> result = postService.toggleLike(1L, "a1b2c3d4");

        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        assertThat(result).contains(true);
        verify(postLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getPost()).isSameAs(post);
        assertThat(captor.getValue().getClientHash()).isEqualTo("a1b2c3d4");
        verify(postLikeRepository, never()).deleteByPostIdAndClientHash(1L, "a1b2c3d4");
    }

    @Test
    @DisplayName("Service_いいね_同じclientHashなら削除してfalseを返す")
    void いいね_同じclientHashなら削除してfalseを返す() {
        Post post = new Post("alice", "S1 の投稿", Instant.parse("2026-06-26T12:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.existsByPostIdAndClientHash(1L, "a1b2c3d4")).willReturn(true);

        Optional<Boolean> result = postService.toggleLike(1L, "a1b2c3d4");

        assertThat(result).contains(false);
        verify(postLikeRepository).deleteByPostIdAndClientHash(1L, "a1b2c3d4");
        verify(postLikeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Service_いいね数_Repositoryの件数を返す")
    void いいね数_Repositoryの件数を返す() {
        given(postLikeRepository.countByPostId(1L)).willReturn(3L);

        long likeCount = postService.countLikes(1L);

        assertThat(likeCount).isEqualTo(3L);
        verify(postLikeRepository).countByPostId(1L);
    }
}
