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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.HexFormat;

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

    @Test
    @DisplayName("Controller_投稿一覧_GET_posts_投稿一覧をmodelに渡す")
    void 投稿一覧_GET_posts_投稿一覧をmodelに渡す() throws Exception {
        List<Post> posts = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-06-26T10:00:00Z")));
        given(postService.latest()).willReturn(posts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", posts))
                .andExpect(content().string(containsString("新しい投稿")));
    }

    @Test
    @DisplayName("Controller_投稿一覧_0件のとき_まだ投稿はありませんを表示する")
    void 投稿一覧_0件のとき_まだ投稿はありませんを表示する() throws Exception {
        given(postService.latest()).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("Controller_新規投稿フォーム_GET_posts_new_PostFormをmodelに渡す")
    void 新規投稿フォーム_GET_posts_new_PostFormをmodelに渡す() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)))
                .andExpect(content().string(containsString("新規投稿")));
    }

    @Test
    @DisplayName("Controller_投稿作成_正常な入力_投稿を保存して一覧へリダイレクトする")
    void 投稿作成_正常な入力_投稿を保存して一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "M3 の投稿"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "M3 の投稿");
    }

    @Test
    @DisplayName("Controller_投稿作成_空白のみ_保存せずフォームを再表示する")
    void 投稿作成_空白のみ_保存せずフォームを再表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author", "body"))
                .andExpect(content().string(containsString("投稿者名を入力してください")))
                .andExpect(content().string(containsString("本文を入力してください")));

        verify(postService, never()).create("   ", "   ");
    }

    @Test
    @DisplayName("Controller_投稿作成_上限超過_保存せずフォームを再表示する")
    void 投稿作成_上限超過_保存せずフォームを再表示する() throws Exception {
        String tooLongAuthor = "a".repeat(31);
        String tooLongBody = "b".repeat(281);

        mockMvc.perform(post("/posts")
                        .param("author", tooLongAuthor)
                        .param("body", tooLongBody))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author", "body"))
                .andExpect(content().string(containsString("投稿者名は 30 文字以内で入力してください")))
                .andExpect(content().string(containsString("本文は 280 文字以内で入力してください")));

        verify(postService, never()).create(tooLongAuthor, tooLongBody);
    }

    @Test
    @DisplayName("Controller_投稿詳細_存在するid_投稿をmodelに渡す")
    void 投稿詳細_存在するid_投稿をmodelに渡す() throws Exception {
        Post post = new Post("alice", "M4 の詳細投稿", Instant.parse("2026-06-26T11:00:00Z"));
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(postService.countLikes(1L)).willReturn(2L);

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(model().attribute("likeCount", 2L))
                .andExpect(content().string(containsString("いいね 2")))
                .andExpect(content().string(containsString("Like")))
                .andExpect(content().string(containsString("M4 の詳細投稿")));
    }

    @Test
    @DisplayName("Controller_投稿詳細_存在しないid_404を返す")
    void 投稿詳細_存在しないid_404を返す() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Controller_いいねPOST_IPとUserAgentのclientHashでトグルする")
    void いいねPOST_IPとUserAgentのclientHashでトグルする() throws Exception {
        given(postService.toggleLike(1L, clientHash("127.0.0.1", "JUnit UA")))
                .willReturn(Optional.of(true));

        mockMvc.perform(post("/posts/1/likes")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        })
                        .header("User-Agent", "JUnit UA"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).toggleLike(1L, clientHash("127.0.0.1", "JUnit UA"));
    }

    @Test
    @DisplayName("Controller_いいねPOST_存在しないid_404を返す")
    void いいねPOST_存在しないid_404を返す() throws Exception {
        given(postService.toggleLike(999L, clientHash("127.0.0.1", "JUnit UA")))
                .willReturn(Optional.empty());

        mockMvc.perform(post("/posts/999/likes")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        })
                        .header("User-Agent", "JUnit UA"))
                .andExpect(status().isNotFound());
    }

    private static String clientHash(String ipAddress, String userAgent) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((ipAddress + userAgent).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
