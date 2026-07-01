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
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
                .andExpect(model().attributeExists("posts"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_GET_postsスラッシュへリクエストできる")
    void list_whenRefreshButtonShown_requestsPostsSlash() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("更新")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("method=\"get\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/posts/\"")));

        mockMvc.perform(get("/posts/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"));
    }

    @Test
    @DisplayName("投稿一覧_投稿者内容投稿日の順に表示する")
    void list_whenPostsExist_rendersAuthorBodyCreatedAtInOrder() throws Exception {
        given(postService.latest()).willReturn(List.of(
                new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"))));

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("alice", "hello", "2026-05-23 19:00");
        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("hello"));
        assertThat(html.indexOf("hello")).isLessThan(html.indexOf("2026-05-23 19:00"));
    }

    @Test
    @DisplayName("新規投稿フォーム_GET_posts_new_posts_formを返しpostFormを渡す")
    void newForm_whenRequested_returnsFormViewWithPostForm() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)));
    }

    @Test
    @DisplayName("投稿作成_投稿者と本文が空白のみ_posts_formを再表示しエラーを表示する")
    void create_whenAuthorAndBodyAreBlank_redisplaysFormWithErrors() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author", "body"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿者名を入力してください")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文を入力してください")));
    }

    @Test
    @DisplayName("投稿作成_入力が妥当なとき_postsへリダイレクトする")
    void create_whenValid_redirectsToPosts() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "hello"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/posts"));

        verify(postService).create(eq("alice"), eq("hello"));
    }

    @Test
    @DisplayName("投稿作成_投稿者と本文が最大文字数のとき_postsへリダイレクトし登録できる")
    void create_whenAuthorAndBodyAreMaxLength_redirectsToPosts() throws Exception {
        String author = "a".repeat(30);
        String body = "b".repeat(280);

        mockMvc.perform(post("/posts")
                        .param("author", author)
                        .param("body", body))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/posts"));

        verify(postService).create(eq(author), eq(body));
    }

    @Test
    @DisplayName("投稿一覧_投稿ボタン_GET_posts_newへリクエストできる")
    void list_whenPostButtonShown_requestsPostsNew() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("method=\"get\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/posts/new\"")));

        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"));
    }
}
