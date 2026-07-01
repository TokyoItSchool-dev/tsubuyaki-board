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
import static org.hamcrest.Matchers.empty;
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
    @DisplayName("投稿一覧_DB空のとき_空配列をビューに渡し空状態メッセージを表示する")
    void 投稿一覧_DB空のとき_空配列をビューに渡し空状態メッセージを表示する() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", empty()))
                .andExpect(content().string(containsString(">まだ投稿はありません<")));
    }

    @Test
    @DisplayName("投稿一覧_表示時_更新ボタンはpostsスラッシュへGETリクエストする")
    void 投稿一覧_表示時_更新ボタンはpostsスラッシュへGetリクエストする() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<form class=\"post-list__refresh\" method=\"get\" action=\"/posts/\">")))
                .andExpect(content().string(containsString("<button type=\"submit\">更新</button>")));
    }

    @Test
    @DisplayName("投稿一覧_投稿があるとき_投稿者内容投稿日の順に表示する")
    void 投稿一覧_投稿があるとき_投稿者内容投稿日の順に表示する() throws Exception {
        given(postService.latest()).willReturn(List.of(
                new Post("suzuki", "表示順を確認します", Instant.parse("2026-06-26T01:00:00Z"))));

        String html = mockMvc.perform(get("/posts/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("suzuki", "表示順を確認します", "2026-06-26 10:00");
        assertThat(html.indexOf("suzuki")).isLessThan(html.indexOf("表示順を確認します"));
        assertThat(html.indexOf("表示順を確認します")).isLessThan(html.indexOf("2026-06-26 10:00"));
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_空のPostFormをビューに渡しフォームを表示する")
    void 投稿作成フォーム_GET_posts_new_空のPostFormをビューに渡しフォームを表示する() throws Exception {
        PostForm postForm = (PostForm) mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"))
                .andExpect(content().string(containsString("<form action=\"/posts\" method=\"post\">")))
                .andReturn()
                .getModelAndView()
                .getModel()
                .get("postForm");

        assertThat(postForm.getAuthor()).isEmpty();
        assertThat(postForm.getBody()).isEmpty();
    }
}
