package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
@Import(PostService.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostRepository postRepository;

    @Test
    @DisplayName("投稿一覧_新着投稿があるとき_最新50件を新着順で表示する")
    void 投稿一覧_新着投稿があるとき_最新50件を新着順で表示する() throws Exception {
        Post newer = new Post("alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z"));
        Post older = new Post("bob", "古い投稿", Instant.parse("2026-05-23T09:00:00Z"));
        List<Post> posts = List.of(newer, older);
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(posts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", posts))
                .andExpect(content().string(matchesPattern("(?s).*新しい投稿.*古い投稿.*")));
    }

    @Test
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void 投稿一覧_0件の場合_まだ投稿はありませんを表示する() throws Exception {
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", Collections.emptyList()))
                .andExpect(content().string(matchesPattern("(?s).*まだ投稿はありません.*")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsスラッシュへGETリクエストする")
    void 投稿一覧_更新ボタン_押すとpostsスラッシュへGetリクエストする() throws Exception {
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("(?s).*<form[^>]*action=\"/posts/\"[^>]*>.*")))
                .andExpect(content().string(matchesPattern("(?s).*<form[^>]*method=\"get\"[^>]*>.*")))
                .andExpect(content().string(matchesPattern("(?s).*<button[^>]*>\\s*更新\\s*</button>.*")));

        mockMvc.perform(get("/posts/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"));
    }

    @Test
    @DisplayName("投稿一覧_投稿がある場合_投稿者内容投稿日の順に表示する")
    void 投稿一覧_投稿がある場合_投稿者内容投稿日の順に表示する() throws Exception {
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("(?s).*alice.*本文です.*2026-05-23 19:00.*")));
    }

    @Test
    @DisplayName("新規投稿フォーム_GET_posts_new_postFormをビューに渡す")
    void 新規投稿フォーム_GET_posts_new_postFormをビューに渡す() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)));
    }
}
