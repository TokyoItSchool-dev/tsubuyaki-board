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
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostControllerTest {

    private static final String POSTS_PATH = "/posts";
    private static final String POSTS_VIEW = "posts/list.html";
    private static final String POSTS_ATTRIBUTE = "posts";
    private static final String POST_FORM_PATH = "/posts/new";
    private static final String POST_FORM_VIEW = "posts/form.html";
    private static final String POST_FORM_ATTRIBUTE = "postForm";
    private static final String POST_DETAIL_VIEW = "posts/detail.html";
    private static final String POST_ATTRIBUTE = "post";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_最新投稿があるとき_最新50件を新着順のままビューに渡す")
    void 投稿一覧_最新投稿があるとき_最新50件を新着順のままビューに渡す() throws Exception {
        List<Post> posts = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z")),
                new Post("bob", "古い投稿", Instant.parse("2026-05-23T09:00:00Z"))
        );
        given(postService.findLatest50()).willReturn(posts);

        mockMvc.perform(get(POSTS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(POSTS_VIEW))
                .andExpect(model().attribute(POSTS_ATTRIBUTE, posts));

        verify(postService).findLatest50();
    }

    @Test
    @DisplayName("投稿一覧_DB空のとき_空配列をビューに渡し空メッセージを表示する")
    void 投稿一覧_DB空のとき_空配列をビューに渡し空メッセージを表示する() throws Exception {
        List<Post> posts = List.of();
        given(postService.findLatest50()).willReturn(posts);

        mockMvc.perform(get(POSTS_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(POSTS_VIEW))
                .andExpect(model().attribute(POSTS_ATTRIBUTE, posts))
                .andExpect(content().string(containsString(">まだ投稿はありません<")));

        verify(postService).findLatest50();
    }

    @Test
    @DisplayName("投稿一覧_表示したとき_新規投稿フォームへのリンクを表示する")
    void 投稿一覧_表示したとき_新規投稿フォームへのリンクを表示する() throws Exception {
        given(postService.findLatest50()).willReturn(List.of());

        mockMvc.perform(get(POSTS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/posts/new\"")))
                .andExpect(content().string(containsString(">新規投稿<")));

        verify(postService).findLatest50();
    }

    @Test
    @DisplayName("新規投稿フォーム_GETリクエスト_空のPostFormをビューに渡す")
    void 新規投稿フォーム_GETリクエスト_空のPostFormをビューに渡す() throws Exception {
        mockMvc.perform(get(POST_FORM_PATH))
                .andExpect(status().isOk())
                .andExpect(view().name(POST_FORM_VIEW))
                .andExpect(model().attribute(POST_FORM_ATTRIBUTE, instanceOf(PostForm.class)));
    }

    @Test
    @DisplayName("投稿作成_入力値が妥当なとき_保存して投稿一覧へリダイレクトする")
    void 投稿作成_入力値が妥当なとき_保存して投稿一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post(POSTS_PATH)
                        .param("author", "alice")
                        .param("body", "今日の共有です"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(POSTS_PATH));

        verify(postService).create("alice", "今日の共有です");
        verifyNoMoreInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_投稿者と本文が空白のみのとき_フォームを再表示しエラーを表示する")
    void 投稿作成_投稿者と本文が空白のみのとき_フォームを再表示しエラーを表示する() throws Exception {
        mockMvc.perform(post(POSTS_PATH)
                        .param("author", "   ")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name(POST_FORM_VIEW))
                .andExpect(model().attributeHasFieldErrors(POST_FORM_ATTRIBUTE, "author", "body"))
                .andExpect(content().string(containsString("投稿者名を入力してください")))
                .andExpect(content().string(containsString("本文を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿詳細_存在するIDのとき_投稿をビューに渡す")
    void 投稿詳細_存在するIdのとき_投稿をビューに渡す() throws Exception {
        Post post = new Post("alice", "詳細で表示する投稿", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get(POSTS_PATH + "/1"))
                .andExpect(status().isOk())
                .andExpect(view().name(POST_DETAIL_VIEW))
                .andExpect(model().attribute(POST_ATTRIBUTE, post))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("詳細で表示する投稿")));

        verify(postService).findById(1L);
    }

    @Test
    @DisplayName("投稿詳細_存在しないIDのとき_404を返す")
    void 投稿詳細_存在しないIdのとき_404を返す() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get(POSTS_PATH + "/999"))
                .andExpect(status().isNotFound());

        verify(postService).findById(999L);
    }
}
