package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿登録_妥当な入力_投稿を作成して一覧へリダイレクトする")
    void 投稿登録_妥当な入力_投稿を作成して一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "hello"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().create("alice", "hello");
    }

    @Test
    @DisplayName("投稿登録_postsスラッシュ_投稿を作成して一覧へリダイレクトする")
    void 投稿登録_postsスラッシュ_投稿を作成して一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts/")
                        .param("author", "alice")
                        .param("body", "hello"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().create("alice", "hello");
    }

    @Test
    @DisplayName("投稿登録_author空白のみ_フォームを再表示し投稿者名必須エラーを表示する")
    void 投稿登録_author空白のみ_フォームを再表示し投稿者名必須エラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿登録_author空文字_フォームを再表示し投稿者名必須エラーを表示する")
    void 投稿登録_author空文字_フォームを再表示し投稿者名必須エラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "")
                        .param("body", "hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿登録_author31文字_フォームを再表示し投稿者名文字数エラーを表示する")
    void 投稿登録_author31文字_フォームを再表示し投稿者名文字数エラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "a".repeat(31))
                        .param("body", "hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名は 30 文字以内で入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿登録_body空白のみ_フォームを再表示し本文必須エラーを表示する")
    void 投稿登録_body空白のみ_フォームを再表示し本文必須エラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿登録_body空文字_フォームを再表示し本文必須エラーを表示する")
    void 投稿登録_body空文字_フォームを再表示し本文必須エラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿登録_body281文字_フォームを再表示し本文文字数エラーを表示する")
    void 投稿登録_body281文字_フォームを再表示し本文文字数エラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "b".repeat(281)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文は 280 文字以内で入力してください")));

        verifyNoInteractions(postService);
    }
}
