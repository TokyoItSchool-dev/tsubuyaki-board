package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.web.dto.PostDto;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_投稿がないとき_空リストを返す")
    void latest_whenNoPosts_returnsEmpty() {
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(List.of());

        List<PostDto> actual = postService.latest();

        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("投稿一覧_最新投稿があるとき_Repositoryから最新50件を新着順で取得する")
    void latest_whenPostsExist_returnsLatest50PostsInNewestOrder() {
        Post newerPost = new Post("alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z"));
        Post olderPost = new Post("bob", "古い投稿", Instant.parse("2026-05-23T09:00:00Z"));
        List<Post> latestPosts = List.of(newerPost, olderPost);
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(latestPosts);

        List<PostDto> actual = postService.latest();

        assertThat(actual)
                .extracting(PostDto::author, PostDto::body, PostDto::createdAt)
                .containsExactly(
                        tuple(newerPost.getAuthor(), newerPost.getBody(), newerPost.getCreatedAt()),
                        tuple(olderPost.getAuthor(), olderPost.getBody(), olderPost.getCreatedAt()));
    }

    @Test
    @DisplayName("投稿作成_入力正常のとき_投稿者本文投稿日を保存する")
    void create_whenValid_savesPostWithCreatedAt() {
        PostForm form = new PostForm();
        form.setAuthor("alice");
        form.setBody("登録した投稿");
        Instant before = Instant.now();

        postService.create(form);

        Instant after = Instant.now();
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        then(postRepository).should().save(captor.capture());
        Post saved = captor.getValue();
        assertThat(saved.getAuthor()).isEqualTo("alice");
        assertThat(saved.getBody()).isEqualTo("登録した投稿");
        assertThat(saved.getCreatedAt()).isBetween(before, after);
    }
}
