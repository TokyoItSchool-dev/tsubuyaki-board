package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostControllerTest {

    private static final Instant BASE_TIME = Instant.parse("2026-06-30T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_GET_posts_新着50件をビューに渡す")
    void list_GET_posts_新着50件をビューに渡す() throws Exception {
        List<Post> posts = List.of(post("alice", "最新の共有です", BASE_TIME.plusSeconds(1)));
        given(postService.findLatest50()).willReturn(posts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", sameInstance(posts)));

        verify(postService).findLatest50();
    }

    @Test
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void list_0件の場合_まだ投稿はありませんを表示する() throws Exception {
        List<Post> posts = Collections.emptyList();
        given(postService.findLatest50()).willReturn(posts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", posts))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_新着50件を受け取った場合_50件を表示する")
    void list_新着50件を受け取った場合_50件を表示する() throws Exception {
        List<Post> posts = IntStream.rangeClosed(2, 51)
                .mapToObj(index -> post(
                        "author" + index,
                        "body" + index,
                        BASE_TIME.plusSeconds(index)))
                .toList();
        given(postService.findLatest50()).willReturn(posts);

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(countOccurrences(html, "<article class=\"post\">")).isEqualTo(50);
        assertThat(html).contains(">body51<");
        assertThat(html).doesNotContain(">body1<");
    }

    @Test
    @DisplayName("投稿一覧_更新ボタンがあり_GETでpostsスラッシュへリクエストする")
    void list_更新ボタンがあり_GETでpostsスラッシュへリクエストする() throws Exception {
        given(postService.findLatest50()).willReturn(List.of());

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).contains("class=\"post-list__refresh\" method=\"get\" action=\"/posts/\"");
        assertThat(html).contains("<button type=\"submit\">更新</button>");
    }

    @Test
    @DisplayName("投稿一覧_投稿がある場合_投稿者内容投稿日の順に表示する")
    void list_投稿がある場合_投稿者内容投稿日の順に表示する() throws Exception {
        Post post = post("alice", "本日の共有です", Instant.parse("2026-06-30T01:15:00Z"));
        given(postService.findLatest50()).willReturn(List.of(post));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).containsSubsequence("alice", "本日の共有です", "2026-06-30 10:15");
    }

    @Test
    @DisplayName("投稿登録_入力が妥当なとき_Serviceで保存し投稿一覧へリダイレクトする")
    void create_入力が妥当なとき_Serviceで保存し投稿一覧へリダイレクトする() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/posts")
                        .param("author", "alice")
                        .param("body", "本日の共有です"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "本日の共有です");
    }

    @Test
    @DisplayName("投稿登録フォーム_HTML入力制限を出力しない")
    void newForm_HTML入力制限を出力しない() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).doesNotContain("maxlength=");
        assertThat(html).doesNotContain(" required");
    }

    @Test
    @DisplayName("投稿登録_空白のみのとき_入力値を保持してフォームを再表示する")
    void create_空白のみのとき_入力値を保持してフォームを再表示する() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/posts")
                        .param("author", " ")
                        .param("body", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author", "body"))
                .andExpect(content().string(containsString("投稿者名を入力してください")))
                .andExpect(content().string(containsString("本文を入力してください")))
                .andExpect(content().string(containsString("value=\" \"")))
                .andExpect(content().string(containsString("> </textarea>")));

        verify(postService, never()).create(" ", " ");
    }

    @Test
    @DisplayName("投稿登録_投稿者名31文字のとき_エラーを表示して入力値を保持する")
    void create_投稿者名31文字のとき_エラーを表示して入力値を保持する() throws Exception {
        String author = "a".repeat(31);

        mockMvc.perform(MockMvcRequestBuilders.post("/posts")
                        .param("author", author)
                        .param("body", "本日の共有です"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名は 1 文字以上 30 文字以内で入力してください")))
                .andExpect(content().string(containsString("value=\"" + author + "\"")));

        verify(postService, never()).create(author, "本日の共有です");
    }

    private static Post post(String author, String body, Instant createdAt) {
        return new Post(author, body, createdAt);
    }

    private static int countOccurrences(String text, String target) {
        int count = 0;
        int fromIndex = 0;
        int foundIndex = text.indexOf(target, fromIndex);
        while (foundIndex >= 0) {
            count++;
            fromIndex = foundIndex + target.length();
            foundIndex = text.indexOf(target, fromIndex);
        }
        return count;
    }
}
