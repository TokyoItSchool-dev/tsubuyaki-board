package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.domain.Post;
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
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyString;
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
    @DisplayName("投稿一覧_GET_posts_0件なら空メッセージとposts属性を表示する")
    void getPosts_whenEmpty_rendersEmptyMessageAndPostsModel() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", empty()))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("キーワード検索_GET_posts_qあり_検索結果とq属性を一覧ビューに渡す")
    void getPosts_whenQueryExists_rendersSearchResultsAndQueryModel() throws Exception {
        Post post = new Post("alice", "MTGメモ", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.search("MTG")).willReturn(List.of(post));

        mockMvc.perform(get("/posts").param("q", "MTG"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of(post)))
                .andExpect(model().attribute("q", "MTG"))
                .andExpect(content().string(containsString("MTGメモ")))
                .andExpect(content().string(containsString("value=\"MTG\"")));

        verify(postService).search("MTG");
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_formビューとpostForm属性を表示する")
    void getPostsNew_rendersFormViewAndPostFormModel() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)));
    }

    @Test
    @DisplayName("投稿登録_入力正常_保存して投稿一覧へリダイレクトする")
    void createPost_whenValid_savesAndRedirectsToPosts() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "hello"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "hello");
    }

    @Test
    @DisplayName("投稿登録_author空白のみ_フォームを再表示しエラーを表示する")
    void createPost_whenAuthorBlank_rerendersFormWithError() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名を入力してください")));

        verify(postService, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("投稿登録_body空白のみ_フォームを再表示しエラーを表示する")
    void createPost_whenBodyBlank_rerendersFormWithError() throws Exception {
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
    @DisplayName("投稿登録_author31文字_フォームを再表示しエラーを表示する")
    void createPost_whenAuthorTooLong_rerendersFormWithError() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "a".repeat(31))
                        .param("body", "hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名は 30 文字以内で入力してください")));

        verify(postService, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("投稿登録_body281文字_フォームを再表示しエラーを表示する")
    void createPost_whenBodyTooLong_rerendersFormWithError() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "b".repeat(281)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文は 280 文字以内で入力してください")));

        verify(postService, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("投稿詳細_存在するid_detailビューとpost属性を表示する")
    void getPostDetail_whenFound_rendersDetailViewAndPostModel() throws Exception {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(postService.countLikes(1L)).willReturn(2L);

        mockMvc.perform(get("/posts/1")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        })
                        .header("User-Agent", "JUnit"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(model().attribute("likeCount", 2L))
                .andExpect(model().attribute("likedByCurrentClient", false))
                .andExpect(content().string(containsString("hello")))
                .andExpect(content().string(containsString("いいね 2")))
                .andExpect(content().string(containsString("いいね")));
    }

    @Test
    @DisplayName("投稿詳細_存在しないid_404を返す")
    void getPostDetail_whenNotFound_returns404() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("いいね_POST_posts_id_likes_clientHashでトグルして詳細へリダイレクトする")
    void toggleLike_hashesClientAndRedirectsToDetail() throws Exception {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(post("/posts/1/likes")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        })
                        .header("User-Agent", "JUnit"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).toggleLike(1L, "1ad93342");
    }

    @Test
    @DisplayName("いいね_存在しないid_404を返す")
    void toggleLike_whenPostNotFound_returns404() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(post("/posts/999/likes"))
                .andExpect(status().isNotFound());

        verify(postService, never()).toggleLike(999L, "");
    }
}
