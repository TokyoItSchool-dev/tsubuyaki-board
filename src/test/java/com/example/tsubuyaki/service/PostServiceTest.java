package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
    @DisplayName("Service_投稿一覧_Repositoryの新着50件を返す")
    void latest_returnsLatestPostsFromRepository() {
        List<Post> expected = List.of(new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expected);

        List<Post> actual = postService.latest();

        assertThat(actual).isSameAs(expected);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Service_投稿詳細_Repositoryからidで取得する")
    void findById_returnsPostFromRepository() {
        Post expected = new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(expected));

        Optional<Post> actual = postService.findById(1L);

        assertThat(actual).containsSame(expected);
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("Service_投稿登録_authorとbodyを持つ投稿を保存する")
    void create_savesPostWithAuthorAndBody() {
        postService.create("alice", "hello");

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        Post saved = postCaptor.getValue();
        assertThat(saved.getAuthor()).isEqualTo("alice");
        assertThat(saved.getBody()).isEqualTo("hello");
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
