package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.argThat;
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
    @DisplayName("いいねトグル_初回リクエスト_いいねを追加する")
    void いいねトグル_初回リクエスト_いいねを追加する() {
        given(postRepository.existsById(1L)).willReturn(true);
        given(postLikeRepository.findByPostIdAndClientHash(1L, "abc12345"))
                .willReturn(Optional.empty());

        postLikeService.toggleLike(1L, "abc12345");

        verify(postLikeRepository).save(argThat((PostLike like) ->
                like.getPostId().equals(1L) && like.getClientHash().equals("abc12345")));
        verify(postLikeRepository, never()).delete(org.mockito.ArgumentMatchers.any(PostLike.class));
    }

    @Test
    @DisplayName("いいねトグル_同じclientHashの2回目_いいねを解除する")
    void いいねトグル_同じclientHashの2回目_いいねを解除する() {
        PostLike existingLike = new PostLike(1L, "abc12345");
        given(postRepository.existsById(1L)).willReturn(true);
        given(postLikeRepository.findByPostIdAndClientHash(1L, "abc12345"))
                .willReturn(Optional.of(existingLike));

        postLikeService.toggleLike(1L, "abc12345");

        verify(postLikeRepository).delete(existingLike);
        verify(postLikeRepository, never()).save(org.mockito.ArgumentMatchers.any(PostLike.class));
    }

    @Test
    @DisplayName("いいねトグル_同じclientHashの1回目で追加し2回目で解除する")
    void いいねトグル_同じclientHashの1回目で追加し2回目で解除する() {
        PostLike existingLike = new PostLike(1L, "abc12345");
        Optional<PostLike> noLike = Optional.empty();
        Optional<PostLike> foundLike = Optional.of(existingLike);
        given(postRepository.existsById(1L)).willReturn(true);
        given(postLikeRepository.findByPostIdAndClientHash(1L, "abc12345"))
                .willReturn(noLike)
                .willReturn(foundLike);

        postLikeService.toggleLike(1L, "abc12345");
        postLikeService.toggleLike(1L, "abc12345");

        verify(postLikeRepository).save(argThat((PostLike like) ->
                like.getPostId().equals(1L) && like.getClientHash().equals("abc12345")));
        verify(postLikeRepository).delete(existingLike);
    }

    @Test
    @DisplayName("いいね数_countLikes_Repositoryから投稿別いいね数を取得する")
    void いいね数_countLikes_Repositoryから投稿別いいね数を取得する() {
        given(postLikeRepository.countByPostId(1L)).willReturn(2L);

        long count = postLikeService.countLikes(1L);

        org.assertj.core.api.Assertions.assertThat(count).isEqualTo(2L);
        verify(postLikeRepository).countByPostId(1L);
    }
}
