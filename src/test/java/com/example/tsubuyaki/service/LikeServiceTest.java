package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("いいね_同じclientHashが未登録のとき_いいねを追加する")
    void toggleLike_同じclientHashが未登録のとき_いいねを追加する() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "abcd1234")).willReturn(Optional.empty());

        postService.toggleLike(1L, "abcd1234");

        verify(postLikeRepository).save(new PostLike(post, "abcd1234"));
    }

    @Test
    @DisplayName("いいね_同じclientHashが登録済みのとき_いいねを解除する")
    void toggleLike_同じclientHashが登録済みのとき_いいねを解除する() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        PostLike existing = new PostLike(post, "abcd1234", Instant.parse("2026-05-23T10:01:00Z"));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "abcd1234")).willReturn(Optional.of(existing));

        postService.toggleLike(1L, "abcd1234");

        verify(postLikeRepository).delete(existing);
        verifyNoInteractions(postRepository);
    }
}
