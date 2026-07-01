package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_GET_posts_model_postsにListを積みposts_listビューを返す")
    void 投稿一覧_GETPosts_modelPostsにListを積みPostsListビューを返す() throws Exception {
        List<Post> latestPosts = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-06-26T10:00:00Z")),
                new Post("bob", "古い投稿", Instant.parse("2026-06-26T09:00:00Z")));
        given(postService.latest()).willReturn(latestPosts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", sameInstance(latestPosts)));
    }

    @Test
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void 投稿一覧_0件の場合_まだ投稿はありませんを表示する() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsスラッシュへGETリクエストする")
    void 投稿一覧_更新ボタン_押すとPostsスラッシュへGetリクエストする() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).contains("method=\"get\"", "action=\"/posts/\"", ">更新</button>");

        mockMvc.perform(get("/posts/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"));
    }

    @Test
    @DisplayName("投稿一覧_投稿カード_投稿者_内容_投稿日の順に表示する")
    void 投稿一覧_投稿カード_投稿者_内容_投稿日の順に表示する() throws Exception {
        given(postService.latest()).willReturn(List.of(
                new Post("alice", "順序を確認する本文", Instant.parse("2026-06-26T10:00:00Z"))));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("順序を確認する本文"));
        assertThat(html.indexOf("順序を確認する本文")).isLessThan(html.indexOf("2026-06-26"));
    }
}
