package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_取得するとき_Repositoryの新着50件取得を使う")
    void findLatest50_取得するとき_Repositoryの新着50件取得を使う() {
        List<Post> expected = List.of(new Post(
                "alice",
                "hello",
                Instant.parse("2026-06-30T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expected);

        List<Post> actual = postService.findLatest50();

        assertThat(actual).isSameAs(expected);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }
}
