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
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
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
    @DisplayName("投稿一覧_DB空のとき_空メッセージを画面に表示する")
    void 投稿一覧_DB空のとき_空メッセージを画面に表示する() throws Exception {
        List<Post> posts = Collections.emptyList();
        given(postService.findLatestPosts()).willReturn(posts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", posts))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタンがあり_GET_postsスラッシュへリクエストできる")
    void 投稿一覧_更新ボタンがあり_GET_postsスラッシュへリクエストできる() throws Exception {
        given(postService.findLatestPosts()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/posts/\"")))
                .andExpect(content().string(containsString("method=\"get\"")))
                .andExpect(content().string(containsString("<button type=\"submit\"")))
                .andExpect(content().string(containsString("更新")));

        mockMvc.perform(get("/posts/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"));
    }

    @Test
    @DisplayName("投稿一覧_投稿者_内容_投稿日の順に表示する")
    void 投稿一覧_投稿者_内容_投稿日の順に表示する() throws Exception {
        List<Post> latestPosts = List.of(
                new Post("alice", "朝会のメモを共有します", Instant.parse("2026-05-23T10:00:00Z")));
        given(postService.findLatestPosts()).willReturn(latestPosts);

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", latestPosts))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        int authorIndex = html.indexOf("alice");
        int bodyIndex = html.indexOf("朝会のメモを共有します");
        int createdAtIndex = html.indexOf("2026-05-23T10:00:00Z");

        assertThat(authorIndex).isNotNegative();
        assertThat(bodyIndex).isGreaterThan(authorIndex);
        assertThat(createdAtIndex).isGreaterThan(bodyIndex);
    }

    @Test
    @DisplayName("投稿フォーム_GET_posts_new_入力フォームをビューに渡す")
    void 投稿フォーム_GET_posts_new_入力フォームをビューに渡す() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.instanceOf(PostForm.class)));
    }
}
