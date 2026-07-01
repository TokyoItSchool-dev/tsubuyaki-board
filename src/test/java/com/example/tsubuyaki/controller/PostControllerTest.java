package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_投稿があるとき_最新50件をビューに渡す")
    void 投稿一覧_投稿があるとき_最新50件をビューに渡す() throws Exception {
        List<Post> posts = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z")),
                new Post("bob", "古い投稿", Instant.parse("2026-05-23T09:00:00Z")));
        given(postService.findLatest50()).willReturn(posts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", posts));

        verify(postService).findLatest50();
    }

    @Test
    @DisplayName("投稿作成画面_GET_posts_new_フォーム用モデルを渡す")
    void 投稿作成画面_GET_posts_new_フォーム用モデルを渡す() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿詳細_存在するidのとき_投稿をビューに渡す")
    void 投稿詳細_存在するidのとき_投稿をビューに渡す() throws Exception {
        Post post = new Post("alice", "詳細表示する投稿", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(postService.countLikes(1L)).willReturn(2L);

        mockMvc.perform(get("/posts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(model().attribute("likeCount", 2L));

        verify(postService).findById(1L);
        verify(postService).countLikes(1L);
    }

    @Test
    @DisplayName("投稿詳細_存在しないidのとき_404を返す")
    void 投稿詳細_存在しないidのとき_404を返す() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(postService).findById(999L);
        verify(postService, never()).countLikes(999L);
    }

    @Test
    @DisplayName("いいね切替_存在する投稿のとき_clientHashを渡して詳細へリダイレクトする")
    void いいね切替_存在する投稿のとき_clientHashを渡して詳細へリダイレクトする() throws Exception {
        Post post = new Post("alice", "いいね対象", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(post("/posts/{id}/likes", 1L)
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.10");
                            return request;
                        })
                        .header("User-Agent", "JUnit-Agent"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).findById(1L);
        verify(postService).toggleLike(1L, "c68f6c0d");
    }

    @Test
    @DisplayName("いいね切替_存在しない投稿のとき_404を返し更新しない")
    void いいね切替_存在しない投稿のとき_404を返し更新しない() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(post("/posts/{id}/likes", 999L)
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.10");
                            return request;
                        })
                        .header("User-Agent", "JUnit-Agent"))
                .andExpect(status().isNotFound());

        verify(postService).findById(999L);
        verify(postService, never()).toggleLike(999L, "c68f6c0d");
    }

    @Test
    @DisplayName("投稿登録_入力が正しいとき_投稿を保存して一覧へリダイレクトする")
    void 投稿登録_入力が正しいとき_投稿を保存して一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "こんにちは"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "こんにちは");
    }

    @Test
    @DisplayName("投稿登録_本文が空白のみのとき_フォームを再表示して保存しない")
    void 投稿登録_本文が空白のみのとき_フォームを再表示して保存しない() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"));

        verifyNoInteractions(postService);
    }
}
