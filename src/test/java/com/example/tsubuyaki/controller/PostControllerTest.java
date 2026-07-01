package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostControllerTest {

    private TimeZone defaultTimeZone;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @BeforeEach
    void setUpTimeZone() {
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
    }

    @AfterEach
    void tearDownTimeZone() {
        TimeZone.setDefault(defaultTimeZone);
    }

    @Test
    @DisplayName("投稿一覧_投稿0件_空メッセージを表示する")
    void 投稿一覧_投稿0件_空メッセージを表示する() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", Collections.emptyList()))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsスラッシュへGETリクエストする")
    void 投稿一覧_更新ボタン_押すとpostsスラッシュへGetリクエストする() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<form class=\"refresh-form\" action=\"/posts/\" method=\"get\">")))
                .andExpect(content().string(containsString("<button type=\"submit\">更新</button>")));
    }

    @Test
    @DisplayName("投稿一覧_投稿あり_投稿者内容投稿日の順に表示する")
    void 投稿一覧_投稿あり_投稿者内容投稿日の順に表示する() throws Exception {
        Post post = new Post(
                "alice",
                "長い本文でも読みやすく折り返して表示する投稿です。",
                Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("(?s).*<article class=\"post\">\\s*"
                        + "<div class=\"post__author\">alice</div>\\s*"
                        + "<p class=\"post__body\">長い本文でも読みやすく折り返して表示する投稿です。</p>\\s*"
                        + "<time class=\"post__created-at\".*>2026-05-23 19:15</time>.*")));
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_空のフォームを表示する")
    void 投稿作成フォーム_GetPostsNew_空のフォームを表示する() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"));
    }
}
