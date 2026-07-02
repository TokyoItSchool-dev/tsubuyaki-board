package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyString;
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
    @DisplayName("投稿一覧_DB空のとき_空配列をビューに渡し空メッセージを表示する")
    void list_DB空のとき_空配列をビューに渡し空メッセージを表示する() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", Collections.emptyList()))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_投稿があるとき_詳細ページへのリンクを表示する")
    void list_投稿があるとき_詳細ページへのリンクを表示する() throws Exception {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        ReflectionTestUtils.setField(post, "id", 1L);
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/posts/1\"")))
                .andExpect(content().string(containsString("詳細")));
    }

    @Test
    @DisplayName("投稿一覧_投稿者名の色があるとき_先頭文字アバターではなく投稿者名へ色を付ける")
    void list_投稿者名の色があるとき_投稿者名へ色を付ける() throws Exception {
        Post post = new Post("watanabe", "hello", "green", Instant.parse("2026-05-23T10:00:00Z"));
        ReflectionTestUtils.setField(post, "id", 1L);
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("style=\"color: green\"")))
                .andExpect(content().string(containsString(">watanabe</span>")))
                .andExpect(content().string(not(containsString("post__avatar"))));
    }

    @Test
    @DisplayName("投稿一覧_q指定ありのとき_本文検索結果をビューに渡す")
    void list_q指定ありのとき_本文検索結果をビューに渡す() throws Exception {
        Post post = new Post("alice", "hello keyword", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.searchByBody("keyword")).willReturn(List.of(post));

        mockMvc.perform(get("/posts").param("q", "keyword"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of(post)))
                .andExpect(model().attribute("q", "keyword"))
                .andExpect(content().string(containsString("hello keyword")));
    }

    @Test
    @DisplayName("テーマカラー_不正値はなしに戻し遷移先にも現在テーマを反映する")
    void themeColor_不正値はなしに戻し遷移先にも現在テーマを反映する() throws Exception {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        ReflectionTestUtils.setField(post, "id", 1L);
        given(postService.latest()).willReturn(List.of(post));
        given(postService.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts").param("themeColor", "orange"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"page\"")))
                .andExpect(content().string(not(containsString("page--theme-orange"))))
                .andExpect(content().string(containsString("name=\"themeColor\"")))
                .andExpect(content().string(containsString("value=\"\" selected=\"selected\"")))
                .andExpect(content().string(containsString("value=\"blue\"")))
                .andExpect(content().string(containsString("value=\"green\"")))
                .andExpect(content().string(containsString("value=\"pink\"")))
                .andExpect(content().string(containsString("value=\"gray\"")));

        mockMvc.perform(get("/posts/new").cookie(new jakarta.servlet.http.Cookie("themeColor", "green")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"page page--theme-green\"")));

        mockMvc.perform(get("/posts/1").cookie(new jakarta.servlet.http.Cookie("themeColor", "green")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"page page--theme-green\"")));
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_/posts/new_posts/formを表示しpostFormをビューに渡す")
    void newForm_GET_postsNew_postsFormを表示しpostFormをビューに渡す() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)));
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_/posts/new_アバター色選択を表示する")
    void newForm_GET_postsNew_アバター色選択を表示する() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"avatarColor\"")))
                .andExpect(content().string(containsString("value=\"blue\"")))
                .andExpect(content().string(containsString("value=\"green\"")));
    }

    @Test
    @DisplayName("投稿登録_入力が妥当なとき_投稿を保存して一覧へリダイレクトする")
    void create_入力が妥当なとき_投稿を保存して一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "hello")
                        .param("avatarColor", "green"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "hello", "green");
    }

    @Test
    @DisplayName("投稿登録_空白のみのとき_保存せずフォームを再表示しエラー表示する")
    void create_空白のみのとき_保存せずフォームを再表示しエラー表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", " ")
                        .param("body", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author", "body"))
                .andExpect(content().string(containsString("投稿者名を入力してください")))
                .andExpect(content().string(containsString("本文を入力してください")));

        verify(postService, never()).create(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("投稿詳細_存在するidのとき_posts/detailを表示しpostをビューに渡す")
    void detail_存在するidのとき_postsDetailを表示しpostをビューに渡す() throws Exception {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(postService.countLikes(1L)).willReturn(3L);

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(model().attribute("likeCount", 3L))
                .andExpect(content().string(containsString("hello")))
                .andExpect(content().string(containsString("いいね 3")))
                .andExpect(content().string(containsString("Like")));
    }

    @Test
    @DisplayName("投稿詳細_存在しないidのとき_404を返す")
    void detail_存在しないidのとき_404を返す() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("いいね_POST_/posts/{id}/likes_clientHashでトグルして詳細へリダイレクトする")
    void like_POST_postsIdLikes_clientHashでトグルして詳細へリダイレクトする() throws Exception {
        given(postService.findById(1L)).willReturn(Optional.of(
                new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"))));

        mockMvc.perform(post("/posts/1/likes")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        })
                        .header("User-Agent", "JUnit"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).toggleLike(1L, "1fc7d39b");
    }
}
