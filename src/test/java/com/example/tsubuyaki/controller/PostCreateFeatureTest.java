package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.PostBackgroundColor;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostCreateFeatureTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿登録_POST_posts_正常入力を保存して一覧へリダイレクトし不正入力はフォームを再表示する")
    void 投稿登録_POST_posts_正常入力を保存して一覧へリダイレクトし不正入力はフォームを再表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "a")
                        .param("body", "b"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        String maxAuthor = "a".repeat(30);
        String maxBody = "b".repeat(280);
        mockMvc.perform(post("/posts")
                        .param("author", maxAuthor)
                        .param("body", maxBody))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        mockMvc.perform(post("/posts")
                        .param("author", " ")
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名を入力してください")));

        mockMvc.perform(post("/posts")
                        .param("author", "投稿者")
                        .param("body", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")));

        mockMvc.perform(post("/posts")
                        .param("author", "a".repeat(31))
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名は 30 文字以内で入力してください")));

        mockMvc.perform(post("/posts")
                        .param("author", "投稿者")
                        .param("body", "b".repeat(281)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文は 280 文字以内で入力してください")));

        verify(postService).create("a", "b", PostBackgroundColor.DEFAULT);
        verify(postService).create(maxAuthor, maxBody, PostBackgroundColor.DEFAULT);
        verifyNoMoreInteractions(postService);
    }
}
