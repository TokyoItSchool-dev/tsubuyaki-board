package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_latest_Repositoryから新着50件を取得する")
    void 投稿一覧_latest_Repositoryから新着50件を取得する() {
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(Collections.emptyList());

        postService.latest();

        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿検索_search_本文キーワードでRepositoryを検索する")
    void 投稿検索_search_本文キーワードでRepositoryを検索する() {
        given(postRepository.findByBodyContainingOrderByCreatedAtDesc("検索")).willReturn(Collections.emptyList());

        postService.search("検索");

        verify(postRepository).findByBodyContainingOrderByCreatedAtDesc("検索");
    }

    @Test
    @DisplayName("投稿作成_create_avatarColorをPostに保存する")
    void 投稿作成_create_avatarColorをPostに保存する() {
        postService.create("alice", "本文です", "blue");

        verify(postRepository).save(argThat((Post post) ->
                "alice".equals(post.getAuthor())
                        && "本文です".equals(post.getBody())
                        && "blue".equals(post.getAvatarColor())
                        && post.getCreatedAt() != null));
    }
}
