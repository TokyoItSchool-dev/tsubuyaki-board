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
    @DisplayName("投稿登録_正常な入力の場合_入力値を保存する")
    void 投稿登録_正常な入力の場合_入力値を保存する() {
        LocalDateTime before = LocalDateTime.now();

        postService.create("alice", "今日の共有です");

        LocalDateTime after = LocalDateTime.now();
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();
        assertThat(savedPost.getAuthor()).isEqualTo("alice");
        assertThat(savedPost.getBody()).isEqualTo("今日の共有です");
        assertThat(savedPost.getCreatedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("投稿詳細_存在するIDの場合_repositoryから取得した投稿を返す")
    void 投稿詳細_存在するIDの場合_repositoryから取得した投稿を返す() {
        Post post = new Post("alice", "詳細です", LocalDateTime.parse("2026-06-26T10:00:00"));
        given(postRepository.findById(10L)).willReturn(Optional.of(post));

        Optional<Post> actual = postService.findById(10L);

        assertThat(actual).containsSame(post);
    }
}
