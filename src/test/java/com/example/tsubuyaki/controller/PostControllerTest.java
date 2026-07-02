package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.ClientHashService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
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

    @MockitoBean
    private ClientHashService clientHashService;

    @Test
    @DisplayName("投稿一覧_投稿0件_空メッセージを表示する")
    void 投稿一覧_投稿0件_空メッセージを表示する() throws Exception {
        given(postService.search(null)).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of()))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_ROOT_一覧ビューを返す")
    void 投稿一覧_ROOT_一覧ビューを返す() throws Exception {
        given(postService.search(null)).willReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of()));

        verify(postService).search(null);
    }

    @Test
    @DisplayName("投稿一覧_投稿あり_投稿者投稿日本文を表示する")
    void 投稿一覧_投稿あり_投稿者投稿日本文を表示する() throws Exception {
        Post post = new Post("alice", "今日の共有です", "blue", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.search(null)).willReturn(List.of(post));

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", List.of(post)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("alice", "今日の共有です", "2026-05-23 19:15", "avatar--blue");
    }

    @Test
    @DisplayName("投稿検索_qあり_検索結果と検索語を一覧ビューに渡す")
    void 投稿検索_qあり_検索結果と検索語を一覧ビューに渡す() throws Exception {
        Post post = new Post("alice", "今日の共有です", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.search("共有")).willReturn(List.of(post));

        String html = mockMvc.perform(get("/posts")
                        .param("q", "共有"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of(post)))
                .andExpect(model().attribute("q", "共有"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("name=\"q\"", "value=\"共有\"", "今日の共有です");
        verify(postService).search("共有");
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_PostFormをビューに渡す")
    void 投稿作成フォーム_GET_posts_new_PostFormをビューに渡す() throws Exception {
        String html = mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)))
                .andExpect(model().attribute("avatarColors", List.of("gray", "blue", "green", "orange", "pink")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("name=\"avatarColor\"", "value=\"gray\"", "value=\"blue\"");
    }

    @Test
    @DisplayName("投稿登録_入力が妥当なとき_投稿一覧へリダイレクトする")
    void 投稿登録_入力が妥当なとき_投稿一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "今日の共有です")
                        .param("avatarColor", "blue"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "今日の共有です", "blue");
    }

    @Test
    @DisplayName("投稿登録_アバター色未指定_投稿一覧へリダイレクトする")
    void 投稿登録_アバター色未指定_投稿一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "今日の共有です"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "今日の共有です", "gray");
    }

    @Test
    @DisplayName("投稿登録_不正なアバター色_フォームを再表示してエラーを表示する")
    void 投稿登録_不正なアバター色_フォームを再表示してエラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "今日の共有です")
                        .param("avatarColor", "red"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("avatarColors", List.of("gray", "blue", "green", "orange", "pink")))
                .andExpect(model().attributeHasFieldErrors("postForm", "avatarColor"))
                .andExpect(content().string(containsString("アバター色を選択してください")));

        verify(postService, never()).create("alice", "今日の共有です", "red");
    }

    @Test
    @DisplayName("投稿登録_空白のみのとき_フォームを再表示してエラーを表示する")
    void 投稿登録_空白のみのとき_フォームを再表示してエラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author", "body"))
                .andExpect(content().string(containsString("投稿者名を入力してください")))
                .andExpect(content().string(containsString("本文を入力してください")));

        verify(postService, never()).create("   ", "   ", null);
    }

    @Test
    @DisplayName("投稿詳細_存在するid_詳細ビューに投稿を渡す")
    void 投稿詳細_存在するid_詳細ビューに投稿を渡す() throws Exception {
        Post post = new Post("alice", "詳細で読む投稿です", "green", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.findById(10L)).willReturn(Optional.of(post));
        given(clientHashService.generate("127.0.0.1", "JUnit")).willReturn("abcdef12");
        given(postService.countLikes(10L)).willReturn(2L);
        given(postService.likedBy(10L, "abcdef12")).willReturn(true);

        String html = mockMvc.perform(get("/posts/10")
                        .header("User-Agent", "JUnit"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(model().attribute("likeCount", 2L))
                .andExpect(model().attribute("likedByClient", true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("alice", "詳細で読む投稿です", "2026-05-23 19:15", "2", "いいね済み",
                "avatar--green");
    }

    @Test
    @DisplayName("投稿詳細_存在しないid_404を返す")
    void 投稿詳細_存在しないid_404を返す() throws Exception {
        given(postService.findById(404L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("いいね_POST_サービスを呼び詳細へリダイレクトする")
    void いいね_POST_サービスを呼び詳細へリダイレクトする() throws Exception {
        given(postService.findById(10L)).willReturn(Optional.of(
                new Post("alice", "hello", Instant.parse("2026-05-23T10:15:00Z"))));
        given(clientHashService.generate("127.0.0.1", "JUnit")).willReturn("abcdef12");

        mockMvc.perform(post("/posts/10/likes")
                        .header("User-Agent", "JUnit"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/10"));

        verify(postService).toggleLike(10L, "abcdef12");
    }

    @Test
    @DisplayName("いいね_POST_存在しないid_404を返す")
    void いいね_POST_存在しないid_404を返す() throws Exception {
        given(postService.findById(404L)).willReturn(Optional.empty());

        mockMvc.perform(post("/posts/404/likes"))
                .andExpect(status().isNotFound());
    }
}
