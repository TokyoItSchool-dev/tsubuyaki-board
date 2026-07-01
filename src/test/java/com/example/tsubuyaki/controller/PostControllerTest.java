package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.LikeService;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private LikeService likeService;

    @Test
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void 投稿一覧_0件の場合_まだ投稿はありませんを表示する() throws Exception {
        given(postService.latest()).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", hasSize(0)))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsスラッシュにGETリクエストする")
    void 投稿一覧_更新ボタン_押すとpostsスラッシュにGETリクエストする() throws Exception {
        given(postService.latest()).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/posts/\" method=\"get\"")))
                .andExpect(content().string(containsString("<button type=\"submit\"")));
    }

    @Test
    @DisplayName("投稿一覧_投稿がある場合_投稿者内容投稿日の順に表示する")
    void 投稿一覧_投稿がある場合_投稿者内容投稿日の順に表示する() throws Exception {
        given(postService.latest()).willReturn(List.of(
                new Post("alice", "朝会メモを共有しました", Instant.parse("2026-06-26T09:00:00Z"))));

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", hasSize(1)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        int authorIndex = html.indexOf("alice");
        int bodyIndex = html.indexOf("朝会メモを共有しました");
        int createdAtIndex = html.indexOf("2026-06-26 18:00");

        assertThat(authorIndex).isNotNegative();
        assertThat(bodyIndex).isGreaterThan(authorIndex);
        assertThat(createdAtIndex).isGreaterThan(bodyIndex);
    }

    @Test
    @DisplayName("投稿登録_正常な入力_投稿を保存しpostsへリダイレクトする")
    void 投稿登録_正常な入力_投稿を保存しpostsへリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("avatarColor", "red")
                        .param("body", "こんにちは"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/posts"));

        verify(postService).create("alice", "こんにちは", "red");
    }

    @Test
    @DisplayName("投稿登録_author未入力_エラーと入力値を表示し保存しない")
    void 投稿登録_author未入力_エラーと入力値を表示し保存しない() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "")
                        .param("body", "本文の入力値"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名を入力してください")))
                .andExpect(content().string(containsString("本文の入力値")));

        verify(postService, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("投稿登録_author空白のみ_エラーを表示し保存しない")
    void 投稿登録_author空白のみ_エラーを表示し保存しない() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名を入力してください")));

        verify(postService, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("投稿登録_author31文字以上_エラーを表示し保存しない")
    void 投稿登録_author31文字以上_エラーを表示し保存しない() throws Exception {
        String author = "a".repeat(31);

        mockMvc.perform(post("/posts")
                        .param("author", author)
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名は 1 文字以上 30 文字以内で入力してください")))
                .andExpect(content().string(containsString("value=\"" + author + "\"")));

        verify(postService, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("投稿登録_body未入力_エラーと入力値を表示し保存しない")
    void 投稿登録_body未入力_エラーと入力値を表示し保存しない() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")))
                .andExpect(content().string(containsString("value=\"alice\"")));

        verify(postService, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("投稿登録_body空白のみ_エラーを表示し保存しない")
    void 投稿登録_body空白のみ_エラーを表示し保存しない() throws Exception {
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
    @DisplayName("投稿登録_body281文字以上_エラーと入力値を表示し保存しない")
    void 投稿登録_body281文字以上_エラーと入力値を表示し保存しない() throws Exception {
        String body = "b".repeat(281);

        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", body))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文は 1 文字以上 280 文字以内で入力してください")))
                .andExpect(content().string(containsString(body)));

        verify(postService, never()).create(anyString(), anyString());
    }
    @Test
    @DisplayName("投稿一覧_投稿時刻_日本時間の現在時刻で表示する")
    void 投稿一覧_投稿時刻_日本時間の現在時刻で表示する() throws Exception {
        given(postService.latest()).willReturn(List.of(
                new Post("alice", "こんにちは", Instant.parse("2026-07-01T05:00:00Z"))));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("2026-07-01 14:00")));
    }
    @Test
    @DisplayName("投稿詳細_存在するid_HTTP200でdetailビューに投稿を渡す")
    void 投稿詳細_存在するid_HTTP200でdetailビューに投稿を渡す() throws Exception {
        Post post = new Post("alice", "詳細本文", Instant.parse("2026-07-01T05:00:00Z"));
        given(postService.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("詳細本文")))
                .andExpect(content().string(containsString("2026-07-01 14:00")));
    }

    @Test
    @DisplayName("投稿詳細_存在しないid_HTTP404を返す")
    void 投稿詳細_存在しないid_HTTP404を返す() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("いいね_未いいねクライアントがPOST_いいねを登録して詳細へリダイレクトする")
    void いいね_未いいねクライアントがPOST_いいねを登録して詳細へリダイレクトする() throws Exception {
        given(postService.findById(1L)).willReturn(Optional.of(
                new Post("alice", "本文", Instant.parse("2026-07-01T05:00:00Z"))));

        mockMvc.perform(post("/posts/1/likes")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        })
                        .header("User-Agent", "JUnit"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/posts/1"));

        verify(likeService).toggle(1L, clientHash("203.0.113.10", "JUnit"));
    }

    @Test
    @DisplayName("いいね_存在しない投稿IDへPOST_HTTP404を返す")
    void いいね_存在しない投稿IDへPOST_HTTP404を返す() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(post("/posts/999/likes")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        })
                        .header("User-Agent", "JUnit"))
                .andExpect(status().isNotFound());

        verify(likeService, never()).toggle(org.mockito.ArgumentMatchers.anyLong(), anyString());
    }

    @Test
    @DisplayName("投稿詳細_いいね数とLikeボタンを表示する")
    void 投稿詳細_いいね数とLikeボタンを表示する() throws Exception {
        Post post = new Post("alice", "詳細本文", Instant.parse("2026-07-01T05:00:00Z"));
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(likeService.countByPostId(1L)).willReturn(3L);

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("post", post))
                .andExpect(model().attribute("likeCount", 3L))
                .andExpect(content().string(containsString("いいね数: 3")))
                .andExpect(content().string(containsString("Like")))
                .andExpect(content().string(containsString("action=\"/posts/1/likes\"")));
    }

    @Test
    @DisplayName("キーワード検索_q指定_本文にキーワードを含む投稿のみindexビューに表示する")
    void キーワード検索_q指定_本文にキーワードを含む投稿のみindexビューに表示する() throws Exception {
        given(postService.search("Spring")).willReturn(List.of(
                new Post("alice", "Spring Boot のメモ", Instant.parse("2026-07-01T05:00:00Z"))));

        mockMvc.perform(get("/posts").param("q", "Spring"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/index"))
                .andExpect(model().attribute("posts", hasSize(1)))
                .andExpect(model().attribute("q", "Spring"))
                .andExpect(content().string(containsString("Spring Boot のメモ")))
                .andExpect(content().string(not(containsString("Oracle DB のメモ"))))
                .andExpect(content().string(containsString("value=\"Spring\"")));

        verify(postService).search("Spring");
    }

    @Test
    @DisplayName("キーワード検索_q空文字_通常の投稿一覧にフォールバックする")
    void キーワード検索_q空文字_通常の投稿一覧にフォールバックする() throws Exception {
        given(postService.latest()).willReturn(List.of());

        mockMvc.perform(get("/posts").param("q", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", hasSize(0)));

        verify(postService).latest();
        verify(postService, never()).search(anyString());
    }

    @Test
    @DisplayName("投稿フォーム_avatarColor_5色の選択肢を表示する")
    void 投稿フォーム_avatarColor_5色の選択肢を表示する() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"avatarColor\"")))
                .andExpect(content().string(containsString("value=\"red\"")))
                .andExpect(content().string(containsString("value=\"blue\"")))
                .andExpect(content().string(containsString("value=\"green\"")))
                .andExpect(content().string(containsString("value=\"pink\"")))
                .andExpect(content().string(containsString("value=\"orange\"")));
    }

    @Test
    @DisplayName("投稿一覧_avatarColor指定_投稿者名とアバター色を表示する")
    void 投稿一覧_avatarColor指定_投稿者名とアバター色を表示する() throws Exception {
        given(postService.latest()).willReturn(List.of(
                new Post("alice", "色つき投稿", Instant.parse("2026-07-01T05:00:00Z"), "pink")));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("pink")))
                .andExpect(content().string(containsString("avatar-color--pink")))
                .andExpect(content().string(containsString("avatar-shape--person")))
                .andExpect(content().string(containsString("人型アバター")));
    }

    @Test
    @DisplayName("投稿一覧_avatarColor未指定_デフォルト色を表示する")
    void 投稿一覧_avatarColor未指定_デフォルト色を表示する() throws Exception {
        given(postService.latest()).willReturn(List.of(
                new Post("alice", "デフォルト色投稿", Instant.parse("2026-07-01T05:00:00Z"))));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("gray")))
                .andExpect(content().string(containsString("avatar-color--gray")));
    }

    @Test
    @DisplayName("投稿詳細_avatarColor指定_投稿者名とアバター色を表示する")
    void 投稿詳細_avatarColor指定_投稿者名とアバター色を表示する() throws Exception {
        Post post = new Post("alice", "詳細色つき投稿", Instant.parse("2026-07-01T05:00:00Z"), "green");
        given(postService.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("green")))
                .andExpect(content().string(containsString("avatar-color--green")))
                .andExpect(content().string(containsString("avatar-shape--person")))
                .andExpect(content().string(containsString("人型アバター")));
    }

    private static String clientHash(String remoteAddr, String userAgent) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest((remoteAddr + userAgent).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
