package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostKeywordTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿登録_本文が空文字のとき_フォームを再表示しエラーを表示する")
    void 投稿登録_本文が空文字のとき_フォームを再表示しエラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "suzuki")
                        .param("title", "検索タイトル")
                        .param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")));

        verify(postService, never()).create("suzuki", "検索タイトル", "");
    }

    @Test
    @DisplayName("投稿一覧_検索ワードあり_本文LIKE検索結果を一覧画面へ渡す")
    void 投稿一覧_検索ワードあり_本文Like検索結果を一覧画面へ渡す() throws Exception {
        Post matched = new Post("suzuki", "検索タイトル", "AI研修のメモ", LocalDateTime.of(2026, 7, 2, 9, 0));
        ReflectionTestUtils.setField(matched, "id", 10L);
        given(postService.searchByBody("AI研修")).willReturn(List.of(matched));

        mockMvc.perform(get("/posts").param("q", "AI研修"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of(matched)))
                .andExpect(model().attribute("q", "AI研修"))
                .andExpect(content().string(containsString("AI研修のメモ")))
                .andExpect(content().string(containsString("value=\"AI研修\"")));

        verify(postService).searchByBody("AI研修");
        verify(postService, never()).latest();
    }

    @Test
    @DisplayName("投稿一覧_検索ワードにHTMLが含まれるとき_検索ボックスでエスケープして表示する")
    void 投稿一覧_検索ワードにHtmlが含まれるとき_検索ボックスでエスケープして表示する() throws Exception {
        given(postService.searchByBody("<script>alert(1)</script>")).willReturn(List.of());

        mockMvc.perform(get("/posts").param("q", "<script>alert(1)</script>"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "value=\"&lt;script&gt;alert(1)&lt;/script&gt;\"")));

        verify(postService).searchByBody("<script>alert(1)</script>");
    }
}
