package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.TsubuyakiApplication;
import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostLikeService;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
@ContextConfiguration(classes = TsubuyakiApplication.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private PostLikeService postLikeService;

    @Test
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void 投稿一覧_0件の場合_まだ投稿はありませんを表示する() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsにGETリクエストする")
    void 投稿一覧_更新ボタン_押すとpostsにGETリクエストする() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("action=\"/posts\"", "method=\"get\"", ">更新</button>");
    }

    @Test
    @DisplayName("投稿一覧_投稿あり_投稿者投稿日本文の順に表示する")
    void 投稿一覧_投稿あり_投稿者投稿日本文の順に表示する() throws Exception {
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.latest()).willReturn(List.of(post));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("2026-05-23"));
        assertThat(html.indexOf("2026-05-23")).isLessThan(html.indexOf("本文です"));
    }

    @Test
    @DisplayName("投稿一覧_投稿ブロック_詳細画面へのリンクを表示する")
    void 投稿一覧_投稿ブロック_詳細画面へのリンクを表示する() throws Exception {
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T10:00:00Z"));
        setField(post, "id", 1L);
        given(postService.latest()).willReturn(List.of(post));
        given(postLikeService.countByPostId(1L)).willReturn(3L);

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("href=\"/posts/1\"", "いいね 3");
    }

    @Test
    @DisplayName("投稿詳細_存在する投稿_投稿者投稿日時本文を表示する")
    void 投稿詳細_存在する投稿_投稿者投稿日時本文を表示する() throws Exception {
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T10:00:00Z"));
        setField(post, "id", 1L);
        given(postService.findById(1L)).willReturn(post);
        given(postLikeService.countByPostId(1L)).willReturn(3L);

        MvcResult result = mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("alice", "2026-05-23 19:00", "本文です", "いいね 3", "一覧に戻る");
        assertThat(html).contains("action=\"/posts/1/likes\"", "method=\"post\"", ">いいね</button>");
    }

    @Test
    @DisplayName("いいね_詳細画面で押下_IPとUAのハッシュでトグルして詳細へ戻る")
    void いいね_詳細画面で押下_IPとUAのハッシュでトグルして詳細へ戻る() throws Exception {
        mockMvc.perform(post("/posts/1/likes")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.10");
                            return request;
                        })
                        .header("User-Agent", "JUnit UA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        then(postLikeService).should().toggle(1L, "90a7ba91");
    }

    @Test
    @DisplayName("新規投稿_入力が正しい場合_投稿を登録して一覧へリダイレクトする")
    void 新規投稿_入力が正しい場合_投稿を登録して一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "本文です"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().create("alice", "本文です");
    }

    @Test
    @DisplayName("新規投稿_DBエラーが発生した場合_画面遷移せずエラーを表示する")
    void 新規投稿_DBエラーが発生した場合_画面遷移せずエラーを表示する() throws Exception {
        willThrow(new DataAccessResourceFailureException("DB connection failed"))
                .given(postService).create("alice", "本文です");

        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "本文です"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿の登録に失敗しました")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("alice")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文です")));
    }
}
