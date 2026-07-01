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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
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
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void list_whenNoPosts_showsEmptyMessage() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", hasSize(0)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_表示するとき_modelに投稿Listを積む")
    void list_setsPostsListToModel() throws Exception {
        List<Post> posts = List.of(
                new Post("alice", "本文1", Instant.parse("2026-05-23T10:00:00Z")),
                new Post("bob", "本文2", Instant.parse("2026-05-23T09:00:00Z")));
        given(postService.latest()).willReturn(posts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", posts));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsへGETリクエストする")
    void list_hasRefreshButtonRequestingPosts() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("method=\"get\"", "action=\"/posts\"", "更新");
    }

    @Test
    @DisplayName("投稿一覧_投稿表示_投稿者内容投稿日の順に表示する")
    void list_displaysAuthorBodyCreatedAtInOrder() throws Exception {
        given(postService.latest()).willReturn(List.of(
                new Post("alice", "順序確認の本文", Instant.parse("2026-05-23T10:30:00Z"))));

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).containsSubsequence("alice", "順序確認の本文", "2026-05-23 19:30");
    }

    @Test
    @DisplayName("投稿作成画面_GET_posts_new_posts_formビューとpostFormを返す")
    void newForm_rendersFormViewWithPostForm() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)));
    }

    @Test
    @DisplayName("投稿作成画面_GET_posts_new_投稿フォーム項目を表示する")
    void newForm_displaysPostFormFields() throws Exception {
        String html = mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains(
                "action=\"/posts\"",
                "method=\"post\"",
                "name=\"author\"",
                "name=\"body\"");
    }
}
