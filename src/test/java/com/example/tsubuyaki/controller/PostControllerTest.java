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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.BDDMockito.given;
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
    @DisplayName("投稿一覧_投稿があるとき_Serviceの最新投稿をビューに渡す")
    void 投稿一覧_投稿があるとき_Serviceの最新投稿をビューに渡す() throws Exception {
        List<Post> latestPosts = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z")),
                new Post("bob", "古い投稿", Instant.parse("2026-05-23T09:00:00Z")));
        given(postService.findLatest50Posts()).willReturn(latestPosts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", latestPosts));
    }

    @Test
    @DisplayName("投稿一覧_投稿が0件のとき_空メッセージを表示する")
    void 投稿一覧_投稿が0件のとき_空メッセージを表示する() throws Exception {
        given(postService.findLatest50Posts()).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of()))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_表示したとき_postsへ再取得する更新ボタンを表示する")
    void 投稿一覧_表示したとき_postsへ再取得する更新ボタンを表示する() throws Exception {
        given(postService.findLatest50Posts()).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/posts/\"")))
                .andExpect(content().string(containsString("method=\"get\"")))
                .andExpect(content().string(containsString("<button type=\"submit\">更新</button>")));
    }

    @Test
    @DisplayName("投稿一覧_投稿があるとき_投稿者_内容_投稿日の順に表示する")
    void 投稿一覧_投稿があるとき_投稿者_内容_投稿日の順に表示する() throws Exception {
        List<Post> latestPosts = List.of(
                new Post("alice", "本文です", Instant.parse("2026-05-23T10:00:00Z")));
        given(postService.findLatest50Posts()).willReturn(latestPosts);

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("本文です"));
        assertThat(html.indexOf("本文です")).isLessThan(html.indexOf("2026-05-23 19:00"));
    }

    @Test
    @DisplayName("投稿詳細_投稿が削除されていないとき_detailを表示し投稿をビューに渡す")
    void 投稿詳細_投稿が削除されていないとき_detailを表示し投稿をビューに渡す() throws Exception {
        Post post = new Post("alice", "詳細を表示する投稿", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.findDetailPost(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("詳細を表示する投稿")));
    }

    @Test
    @DisplayName("投稿詳細_投稿が存在しないとき_404を返す")
    void 投稿詳細_投稿が存在しないとき_404を返す() throws Exception {
        given(postService.findDetailPost(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿詳細_表示したとき_投稿一覧へ戻るリンクを表示する")
    void 投稿詳細_表示したとき_投稿一覧へ戻るリンクを表示する() throws Exception {
        Post post = new Post("alice", "詳細を表示する投稿", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.findDetailPost(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/posts\"")))
                .andExpect(content().string(containsString("一覧に戻る")));
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_posts_formを表示しpostFormをビューに渡す")
    void 投稿作成フォーム_GET_posts_new_posts_formを表示しpostFormをビューに渡す() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)));
    }

    @Test
    @DisplayName("投稿作成_投稿者名と本文が空白のとき_入力チェックエラーを表示する")
    void 投稿作成_投稿者名と本文が空白のとき_入力チェックエラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author", "body"))
                .andExpect(content().string(containsString("投稿者名を入力してください")))
                .andExpect(content().string(containsString("本文を入力してください")));
    }

    @Test
    @DisplayName("投稿作成_投稿者名と本文が最大文字数を超えるとき_形式エラーを表示する")
    void 投稿作成_投稿者名と本文が最大文字数を超えるとき_形式エラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "a".repeat(31))
                        .param("body", "b".repeat(281)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author", "body"))
                .andExpect(content().string(containsString("投稿者名は 30 文字以内で入力してください")))
                .andExpect(content().string(containsString("本文は 280 文字以内で入力してください")));
    }

    @Test
    @DisplayName("投稿作成_入力が正しいとき_Serviceで登録し投稿一覧へリダイレクトする")
    void 投稿作成_入力が正しいとき_Serviceで登録し投稿一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "はじめての投稿"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).createPost("alice", "はじめての投稿");
    }
}
