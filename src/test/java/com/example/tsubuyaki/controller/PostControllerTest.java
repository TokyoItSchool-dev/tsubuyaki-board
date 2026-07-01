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

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
    @DisplayName("投稿一覧_DB空のとき_まだ投稿はありませんを表示する")
    void 投稿一覧_DB空のとき_まだ投稿はありませんを表示する() throws Exception {
        given(postService.latest()).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of()))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_表示時_更新ボタンはpostsスラッシュへGETリクエストする")
    void 投稿一覧_表示時_更新ボタンはpostsスラッシュへGETリクエストする() throws Exception {
        given(postService.latest()).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("更新")))
                .andExpect(content().string(containsString("action=\"/posts/\"")))
                .andExpect(content().string(containsString("method=\"get\"")));
    }

    @Test
    @DisplayName("投稿一覧_投稿あり_投稿者内容投稿日の順に表示する")
    void 投稿一覧_投稿あり_投稿者内容投稿日の順に表示する() throws Exception {
        given(postService.latest()).willReturn(List.of(
                new Post("alice", "一覧の表示順を確認します", Instant.parse("2026-06-26T09:00:00Z"))));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        int authorIndex = html.indexOf("alice");
        int bodyIndex = html.indexOf("一覧の表示順を確認します");
        int createdAtIndex = html.indexOf("2026-06-26T09:00:00Z");

        assertThat(authorIndex).isGreaterThanOrEqualTo(0);
        assertThat(bodyIndex).isGreaterThan(authorIndex);
        assertThat(createdAtIndex).isGreaterThan(bodyIndex);
    }

    @Test
    @DisplayName("新規投稿フォーム_GET_posts_new_空の投稿フォームを表示する")
    void 新規投稿フォーム_GET_posts_new_空の投稿フォームを表示する() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)));
    }

    @Test
    @DisplayName("投稿作成_正常入力_投稿を保存して一覧へリダイレクトする")
    void 投稿作成_正常入力_投稿を保存して一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "今日の共有です"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "今日の共有です");
    }

    @Test
    @DisplayName("投稿作成_author未入力_フォームを再表示してエラー情報を含める")
    void 投稿作成_author未入力_フォームを再表示してエラー情報を含める() throws Exception {
        assertInvalidPost("", "本文があります", "author");
    }

    @Test
    @DisplayName("投稿作成_body未入力_フォームを再表示してエラー情報を含める")
    void 投稿作成_body未入力_フォームを再表示してエラー情報を含める() throws Exception {
        assertInvalidPost("alice", "", "body");
    }

    @Test
    @DisplayName("投稿作成_author31文字以上_フォームを再表示してエラー情報を含める")
    void 投稿作成_author31文字以上_フォームを再表示してエラー情報を含める() throws Exception {
        assertInvalidPost("a".repeat(31), "本文があります", "author");
    }

    @Test
    @DisplayName("投稿作成_body281文字以上_フォームを再表示してエラー情報を含める")
    void 投稿作成_body281文字以上_フォームを再表示してエラー情報を含める() throws Exception {
        assertInvalidPost("alice", "あ".repeat(281), "body");
    }

    @Test
    @DisplayName("投稿作成_空白のみ_フォームを再表示してエラー情報を含める")
    void 投稿作成_空白のみ_フォームを再表示してエラー情報を含める() throws Exception {
        assertInvalidPost("   ", "　　", "author", "body");
    }

    private void assertInvalidPost(String author, String body, String... errorFields) throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", author)
                        .param("body", body))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", errorFields));

        verify(postService, never()).create(anyString(), anyString());
    }
}
