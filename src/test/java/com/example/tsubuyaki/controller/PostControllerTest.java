package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostControllerTest {

    private static final String POSTS_PATH = "/posts";
    private static final String POSTS_VIEW = "posts/list";
    private static final String POSTS_ATTRIBUTE = "posts";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_最新投稿があるとき_最新50件を新着順のままビューに渡す")
    void 投稿一覧_最新投稿があるとき_最新50件を新着順のままビューに渡す() throws Exception {
        List<Post> posts = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z")),
                new Post("bob", "古い投稿", Instant.parse("2026-05-23T09:00:00Z"))
        );
        given(postService.findLatest50()).willReturn(posts);

        mockMvc.perform(get(POSTS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(POSTS_VIEW))
                .andExpect(model().attribute(POSTS_ATTRIBUTE, posts));

        verify(postService).findLatest50();
    }

    @Test
    @DisplayName("投稿一覧_DB空のとき_空配列をビューに渡し空メッセージを表示する")
    void 投稿一覧_DB空のとき_空配列をビューに渡し空メッセージを表示する() throws Exception {
        List<Post> posts = List.of();
        given(postService.findLatest50()).willReturn(posts);

        mockMvc.perform(get(POSTS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(POSTS_VIEW))
                .andExpect(model().attribute(POSTS_ATTRIBUTE, posts))
                .andExpect(content().string(containsString(">まだ投稿はありません<")));

        verify(postService).findLatest50();
    }
}
