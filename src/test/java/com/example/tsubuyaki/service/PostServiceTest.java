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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_最新投稿取得_Repositoryの最新50件を返す")
    void 投稿一覧_最新投稿取得_Repositoryの最新50件を返す() {
        List<Post> latestPosts = List.of(
                new Post("alice", "新しい投稿", LocalDateTime.parse("2026-05-23T10:00:00")),
                new Post("bob", "古い投稿", LocalDateTime.parse("2026-05-23T09:00:00")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(latestPosts);

        List<Post> actual = postService.findLatestPosts();

        assertThat(actual).isEqualTo(latestPosts);
        then(postRepository).should().findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿登録_正常入力_投稿をRepositoryへ保存する")
    void 投稿登録_正常入力_投稿をRepositoryへ保存する() {
        postService.createPost("alice", "今日の学びを共有します");

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();
        assertThat(savedPost.getAuthor()).isEqualTo("alice");
        assertThat(savedPost.getBody()).isEqualTo("今日の学びを共有します");
        assertThat(savedPost.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("投稿詳細_id指定_RepositoryのfindByIdを返す")
    void 投稿詳細_id指定_RepositoryのfindByIdを返す() {
        Post post = new Post("alice", "詳細で表示する本文", LocalDateTime.parse("2026-05-23T10:00:00"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Optional<Post> actual = postService.findPostById(1L);

        assertThat(actual).contains(post);
        then(postRepository).should().findById(1L);
    }
}
