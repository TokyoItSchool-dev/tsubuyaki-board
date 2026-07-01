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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void 投稿一覧_0件の場合_まだ投稿はありませんを表示する() throws Exception {
        given(postService.findLatestPosts()).willReturn(Collections.emptyList());

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", Collections.emptyList()))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("まだ投稿はありません");
    }

    @Test
    @DisplayName("投稿一覧_最新投稿があるとき_model_postsにListを積む")
    void 投稿一覧_最新投稿があるとき_model_postsにListを積む() throws Exception {
        List<Post> latestPosts = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z")),
                new Post("bob", "古い投稿", Instant.parse("2026-05-23T09:00:00Z"))
        );
        given(postService.findLatestPosts()).willReturn(latestPosts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", sameInstance(latestPosts)));

        then(postService).should().findLatestPosts();
    }

    @Test
    @DisplayName("投稿一覧_更新ボタンがあるとき_postsスラッシュへリクエストできる")
    void 投稿一覧_更新ボタンがあるとき_postsスラッシュへリクエストできる() throws Exception {
        given(postService.findLatestPosts()).willReturn(Collections.emptyList());

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("更新")
                .contains("action=\"/posts/\"")
                .contains("method=\"get\"");

        mockMvc.perform(get("/posts/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"));
    }

    @Test
    @DisplayName("投稿一覧_投稿があるとき_投稿者_内容_投稿日の順に表示する")
    void 投稿一覧_投稿があるとき_投稿者_内容_投稿日の順に表示する() throws Exception {
        given(postService.findLatestPosts()).willReturn(List.of(
                new Post("alice", "本文です", Instant.parse("2026-05-23T10:00:00Z"))
        ));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).containsSubsequence("alice", "本文です", "2026-05-23");
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_200を返しフォームビューを表示する")
    void 投稿作成フォーム_GET_posts_new_200を返しフォームビューを表示する() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"));
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_modelに空のpostFormを積む")
    void 投稿作成フォーム_GET_posts_new_modelに空のpostFormを積む() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postForm"))
                .andReturn();

        Object postForm = result.getModelAndView().getModel().get("postForm");
        assertThat(postForm).isInstanceOf(PostForm.class);
        PostForm form = (PostForm) postForm;
        assertThat(form.getAuthor()).isNull();
        assertThat(form.getBody()).isNull();
    }

    @Test
    @DisplayName("投稿詳細_存在するidの場合_詳細ビューを表示しmodelにpostを積む")
    void 投稿詳細_存在するidの場合_詳細ビューを表示しmodelにpostを積む() throws Exception {
        Post post = new Post("alice", "詳細本文", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.findPost(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", sameInstance(post)));
    }

    @Test
    @DisplayName("投稿詳細_存在するidの場合_投稿者_内容_投稿日_戻るリンクを表示する")
    void 投稿詳細_存在するidの場合_投稿者_内容_投稿日_戻るリンクを表示する() throws Exception {
        given(postService.findPost(1L)).willReturn(Optional.of(
                new Post("alice", "詳細本文", Instant.parse("2026-05-23T10:00:00Z"))
        ));

        MvcResult result = mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .containsSubsequence("alice", "詳細本文", "2026-05-23")
                .contains("href=\"/posts\"");
    }

    @Test
    @DisplayName("投稿詳細_存在しないidの場合_404を返す")
    void 投稿詳細_存在しないidの場合_404を返す() throws Exception {
        given(postService.findPost(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿登録_正常な入力の場合_投稿を登録しpostsへリダイレクトする")
    void 投稿登録_正常な入力の場合_投稿を登録しpostsへリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "本文です"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().createPost("alice", "本文です");
    }

    @Test
    @DisplayName("投稿登録_author未入力の場合_登録せずフォームを再表示する")
    void 投稿登録_author未入力の場合_登録せずフォームを再表示する() throws Exception {
        assertInvalidPost("", "本文です", "投稿者名を入力してください");
    }

    @Test
    @DisplayName("投稿登録_authorが31文字以上の場合_登録せずフォームを再表示する")
    void 投稿登録_authorが31文字以上の場合_登録せずフォームを再表示する() throws Exception {
        assertInvalidPost("a".repeat(31), "本文です", "投稿者名は 30 文字以内で入力してください");
    }

    @Test
    @DisplayName("投稿登録_body未入力の場合_登録せずフォームを再表示する")
    void 投稿登録_body未入力の場合_登録せずフォームを再表示する() throws Exception {
        assertInvalidPost("alice", "", "本文を入力してください");
    }

    @Test
    @DisplayName("投稿登録_bodyが281文字以上の場合_登録せずフォームを再表示する")
    void 投稿登録_bodyが281文字以上の場合_登録せずフォームを再表示する() throws Exception {
        assertInvalidPost("alice", "あ".repeat(281), "本文は 280 文字以内で入力してください");
    }

    @Test
    @DisplayName("投稿登録_authorが空白文字のみの場合_登録せずフォームを再表示する")
    void 投稿登録_authorが空白文字のみの場合_登録せずフォームを再表示する() throws Exception {
        assertInvalidPost("   ", "本文です", "投稿者名を入力してください");
    }

    @Test
    @DisplayName("投稿登録_bodyが空白文字のみの場合_登録せずフォームを再表示する")
    void 投稿登録_bodyが空白文字のみの場合_登録せずフォームを再表示する() throws Exception {
        assertInvalidPost("alice", "   ", "本文を入力してください");
    }

    @Test
    @DisplayName("投稿登録_バリデーション失敗時_エラーメッセージを表示し入力内容を保持する")
    void 投稿登録_バリデーション失敗時_エラーメッセージを表示し入力内容を保持する() throws Exception {
        String invalidAuthor = "a".repeat(31);
        MvcResult result = mockMvc.perform(post("/posts")
                        .param("author", invalidAuthor)
                        .param("body", "保持する本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("投稿者名は 30 文字以内で入力してください")
                .contains("value=\"" + invalidAuthor + "\"")
                .contains(">保持する本文</textarea>");
        then(postService).should(never()).createPost(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    private void assertInvalidPost(String author, String body, String expectedMessage) throws Exception {
        MvcResult result = mockMvc.perform(post("/posts")
                        .param("author", author)
                        .param("body", body))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().hasErrors())
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains(expectedMessage)
                .contains("name=\"author\"")
                .contains("name=\"body\"");
        then(postService).should(never()).createPost(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }
}
