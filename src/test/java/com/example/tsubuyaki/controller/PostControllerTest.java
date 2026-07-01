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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
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

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void 投稿一覧_0件の場合_まだ投稿はありませんを表示する() throws Exception {
        given(postService.latest()).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of()))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsスラッシュへGETする")
    void 投稿一覧_更新ボタン_押すとpostsスラッシュへGetする() throws Exception {
        given(postService.latest()).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/posts/\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("method=\"get\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("更新")));
    }

    @Test
    @DisplayName("投稿一覧_新規投稿リンク_押すとフォームURLへ遷移できる")
    void 投稿一覧_新規投稿リンク_押すとフォームUrlへ遷移できる() throws Exception {
        given(postService.latest()).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/posts/new\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("新規投稿")));
    }

    @Test
    @DisplayName("投稿一覧_投稿がある場合_投稿者内容投稿日の順に表示する")
    void 投稿一覧_投稿がある場合_投稿者内容投稿日の順に表示する() throws Exception {
        Post post = new Post(
                "alice",
                "本文は長くても画面上で折り返して表示する",
                Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.latest()).willReturn(List.of(post));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).contains("alice", "本文は長くても画面上で折り返して表示する", "2026-05-23");
        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("本文は長くても画面上で折り返して表示する"));
        assertThat(html.indexOf("本文は長くても画面上で折り返して表示する")).isLessThan(html.indexOf("2026-05-23"));
    }

    @Test
    @DisplayName("新規投稿フォーム_GET_posts_new_空のフォームをビューに渡す")
    void 新規投稿フォーム_GET_postsNew_空のフォームをビューに渡す() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.instanceOf(PostForm.class)))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("author",
                        org.hamcrest.Matchers.nullValue())))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("body",
                        org.hamcrest.Matchers.nullValue())));
    }

    @Test
    @DisplayName("投稿作成_投稿者が空文字の場合_エラーを表示し入力内容を保持する")
    void 投稿作成_投稿者が空文字の場合_エラーを表示し入力内容を保持する() throws Exception {
        mockMvc.perform(post("/posts").param("author", "").param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("author",
                        org.hamcrest.Matchers.equalTo(""))))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("body",
                        org.hamcrest.Matchers.equalTo("本文"))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿者名を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_投稿者が空白のみの場合_エラーを表示し入力内容を保持する")
    void 投稿作成_投稿者が空白のみの場合_エラーを表示し入力内容を保持する() throws Exception {
        mockMvc.perform(post("/posts").param("author", "   ").param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("author",
                        org.hamcrest.Matchers.equalTo("   "))))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("body",
                        org.hamcrest.Matchers.equalTo("本文"))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿者名を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_投稿者が31文字以上の場合_エラーを表示し入力内容を保持する")
    void 投稿作成_投稿者が31文字以上の場合_エラーを表示し入力内容を保持する() throws Exception {
        String author = "a".repeat(31);

        mockMvc.perform(post("/posts").param("author", author).param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("author",
                        org.hamcrest.Matchers.equalTo(author))))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("body",
                        org.hamcrest.Matchers.equalTo("本文"))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿者名は 30 文字以内で入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_本文が空文字の場合_エラーを表示し入力内容を保持する")
    void 投稿作成_本文が空文字の場合_エラーを表示し入力内容を保持する() throws Exception {
        mockMvc.perform(post("/posts").param("author", "alice").param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("author",
                        org.hamcrest.Matchers.equalTo("alice"))))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("body",
                        org.hamcrest.Matchers.equalTo(""))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_本文が空白のみの場合_エラーを表示し入力内容を保持する")
    void 投稿作成_本文が空白のみの場合_エラーを表示し入力内容を保持する() throws Exception {
        mockMvc.perform(post("/posts").param("author", "alice").param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("author",
                        org.hamcrest.Matchers.equalTo("alice"))))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("body",
                        org.hamcrest.Matchers.equalTo("   "))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_本文が281文字以上の場合_エラーを表示し入力内容を保持する")
    void 投稿作成_本文が281文字以上の場合_エラーを表示し入力内容を保持する() throws Exception {
        String body = "a".repeat(281);

        mockMvc.perform(post("/posts").param("author", "alice").param("body", body))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("author",
                        org.hamcrest.Matchers.equalTo("alice"))))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("body",
                        org.hamcrest.Matchers.equalTo(body))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文は 280 文字以内で入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_入力が妥当な場合_投稿後にpostsへリダイレクトする")
    void 投稿作成_入力が妥当な場合_投稿後にpostsへリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts").param("author", "alice").param("body", "本文"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "本文");
    }
}
