package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    void list_whenEmpty_showsEmptyMessage() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", Collections.emptyList()))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsにGETリクエストする")
    void list_hasRefreshButtonRequestingPosts() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("method=\"get\"")))
                .andExpect(content().string(containsString("action=\"/posts/\"")))
                .andExpect(content().string(containsString("更新")));

        mockMvc.perform(get("/posts/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"));
    }

    @Test
    @DisplayName("投稿一覧_投稿表示_投稿者内容投稿日の順に表示する")
    void list_displaysAuthorBodyCreatedAtInOrder() throws Exception {
        given(postService.latest()).willReturn(List.of(
                new Post("alice", "本文です", Instant.parse("2026-05-23T01:00:00Z"))
        ));

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("alice", "本文です", "2026-05-23 10:00");
        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("本文です"));
        assertThat(html.indexOf("本文です")).isLessThan(html.indexOf("2026-05-23 10:00"));
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_postFormをビューに渡す")
    void newForm_setsPostForm() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"));
    }

    @Test
    @DisplayName("投稿作成フォーム_入力欄_投稿者名と内容を入力できる")
    void newForm_hasAuthorAndBodyFields() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"author\"")))
                .andExpect(content().string(containsString("type=\"text\"")))
                .andExpect(content().string(containsString("name=\"body\"")))
                .andExpect(content().string(containsString("<textarea")));
    }

    @Test
    @DisplayName("投稿作成フォーム_登録ボタン_POST_postsへ送信する")
    void newForm_submitButtonPostsToPosts() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(containsString("action=\"/posts\"")))
                .andExpect(content().string(containsString("登録")));
    }

    @Test
    @DisplayName("投稿作成フォーム_キャンセルボタン_postsへ戻る")
    void newForm_cancelButtonReturnsPosts() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/posts\"")))
                .andExpect(content().string(containsString("キャンセル")));
    }

    @Test
    @DisplayName("投稿作成_author空白のみ_posts_formを再表示し入力値を保持する")
    void create_whenAuthorBlank_redisplaysFormAndKeepsInput() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "本文です"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(model().attribute("postForm",
                        org.hamcrest.Matchers.hasProperty("author", org.hamcrest.Matchers.equalTo("   "))))
                .andExpect(content().string(containsString("投稿者名を入力してください")));

        verify(postService, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("投稿作成_body空白のみ_posts_formを再表示する")
    void create_whenBodyBlank_redisplaysForm() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")));

        verify(postService, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("投稿作成_author31文字_posts_formを再表示する")
    void create_whenAuthorTooLong_redisplaysForm() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "a".repeat(31))
                        .param("body", "本文です"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名は 30 文字以内で入力してください")));

        verify(postService, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("投稿作成_body281文字_posts_formを再表示する")
    void create_whenBodyTooLong_redisplaysForm() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "a".repeat(281)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文は 280 文字以内で入力してください")));

        verify(postService, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("投稿作成_入力正常_postsへリダイレクトし保存する")
    void create_whenValid_redirectsToPostsAndCreatesPost() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "本文です"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "本文です");
    }

    @Test
    @DisplayName("投稿詳細_存在するid_posts_detailを表示しpostをビューに渡す")
    void detail_whenPostExists_showsDetailAndSetsPost() throws Exception {
        Post post = new Post("alice", "詳細本文です", Instant.parse("2026-05-23T01:00:00Z"));
        given(postService.findById(42L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/42"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("詳細本文です")))
                .andExpect(content().string(containsString("2026-05-23 10:00")));
    }

    @Test
    @DisplayName("投稿詳細_存在しないid_404を返す")
    void detail_whenPostDoesNotExist_returns404() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿詳細_一覧に戻るボタン_postsへ遷移する")
    void detail_hasBackToListButton() throws Exception {
        Post post = new Post("alice", "詳細本文です", Instant.parse("2026-05-23T01:00:00Z"));
        given(postService.findById(42L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/42"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/posts\"")))
                .andExpect(content().string(containsString("一覧に戻る")));
    }
}
