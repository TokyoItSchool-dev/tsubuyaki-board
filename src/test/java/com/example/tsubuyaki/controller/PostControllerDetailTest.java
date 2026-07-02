package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostReply;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostControllerDetailTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿詳細_存在するID_detail画面を表示し対象投稿を渡す")
    void detail_whenPostExists_returnsDetailViewWithPost() throws Exception {
        Post post = new Post("alice", "朝の共有です", Instant.parse("2026-05-23T10:00:00Z"), "blue");
        List<PostReply> replies = List.of(
                new PostReply(post, "yamada", "ありがとうございます！", Instant.parse("2026-05-23T10:10:00Z"), "red"),
                new PostReply(post, "sato", "参考になりました！", Instant.parse("2026-05-23T10:20:00Z"), "green")
        );
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(postService.countLikes(1L)).willReturn(3L);
        given(postService.repliesForPost(1L)).willReturn(replies);

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", sameInstance(post)))
                .andExpect(model().attribute("likeCount", 3L))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("avatar--blue")))
                .andExpect(content().string(containsString("朝の共有です")))
                .andExpect(content().string(containsString("2026-05-23 19:00")))
                .andExpect(content().string(containsString("3 likes")))
                .andExpect(content().string(containsString("返信")))
                .andExpect(content().string(containsString("yamada")))
                .andExpect(content().string(containsString("ありがとうございます！")))
                .andExpect(content().string(containsString("avatar--red")))
                .andExpect(content().string(containsString("sato")))
                .andExpect(content().string(containsString("参考になりました！")))
                .andExpect(content().string(containsString("avatar--green")))
                .andExpect(content().string(containsString("Like")))
                .andExpect(content().string(containsString("action=\"/posts/1/likes\"")))
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(containsString("削除")))
                .andExpect(content().string(containsString("action=\"/posts/1/delete\"")))
                .andExpect(content().string(not(containsString("class=\"post__link\""))))
                .andExpect(content().string(not(containsString("href=\"/posts/1\""))));
    }

    @Test
    @DisplayName("投稿詳細_返信0件_返信はまだありませんを表示する")
    void detail_whenNoReplies_showsEmptyReplyMessage() throws Exception {
        Post post = new Post("alice", "朝の共有です", Instant.parse("2026-05-23T10:00:00Z"), "blue");
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(postService.countLikes(1L)).willReturn(0L);
        given(postService.repliesForPost(1L)).willReturn(List.of());

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("返信はまだありません")));
    }

    @Test
    @DisplayName("投稿詳細_共通カードにリンク用の装飾を残さない")
    void detail_cardStyle_doesNotUseClickablePostStyle() throws Exception {
        String css = Files.readString(Path.of("src/main/resources/static/css/app.css"));

        assertThat(styleBlock(css, ".post {")).doesNotContain("cursor");
        assertThat(css).doesNotContain(".post:hover");
        assertThat(css).contains(".post--clickable");
    }

    @Test
    @DisplayName("投稿詳細_存在しないID_404を返す")
    void detail_whenPostDoesNotExist_returnsNotFound() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("いいね_POST_RemoteAddrとUser-AgentからclientHashを作りトグルする")
    void like_whenPostExists_togglesLikeWithClientHash() throws Exception {
        given(postService.toggleLike(1L, clientHash("203.0.113.10", "JUnit Browser"))).willReturn(true);

        mockMvc.perform(post("/posts/1/likes")
                        .header("User-Agent", "JUnit Browser")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        }))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).toggleLike(1L, clientHash("203.0.113.10", "JUnit Browser"));
    }

    @Test
    @DisplayName("いいね_POST_存在しない投稿ID_404を返す")
    void like_whenPostDoesNotExist_returnsNotFound() throws Exception {
        given(postService.toggleLike(999L, clientHash("203.0.113.10", "JUnit Browser"))).willReturn(false);

        mockMvc.perform(post("/posts/999/likes")
                        .header("User-Agent", "JUnit Browser")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        }))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿削除_POST_削除後に一覧へリダイレクトする")
    void delete_whenPostExists_redirectsToList() throws Exception {
        given(postService.delete(1L)).willReturn(true);

        mockMvc.perform(post("/posts/1/delete"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).delete(1L);
    }

    @Test
    @DisplayName("投稿削除_POST_存在しない投稿ID_404を返す")
    void delete_whenPostDoesNotExist_returnsNotFound() throws Exception {
        given(postService.delete(999L)).willReturn(false);

        mockMvc.perform(post("/posts/999/delete"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("返信投稿_POST_投稿成功後に投稿詳細へリダイレクトする")
    void createReply_whenValid_redirectsToDetail() throws Exception {
        given(postService.createReply(1L, "yamada", "ありがとうございます！", "blue")).willReturn(true);

        mockMvc.perform(post("/posts/1/replies")
                        .param("author", "yamada")
                        .param("body", "ありがとうございます！")
                        .param("avatarColor", "blue"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).createReply(1L, "yamada", "ありがとうございます！", "blue");
    }

    @Test
    @DisplayName("返信投稿_POST_入力エラー時は詳細画面を再表示する")
    void createReply_whenInvalid_redisplaysDetailWithErrors() throws Exception {
        Post post = new Post("alice", "朝の共有です", Instant.parse("2026-05-23T10:00:00Z"), "blue");
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(postService.countLikes(1L)).willReturn(0L);
        given(postService.repliesForPost(1L)).willReturn(List.of());

        mockMvc.perform(post("/posts/1/replies")
                        .param("author", " ")
                        .param("body", " ")
                        .param("avatarColor", "purple"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attributeHasFieldErrors("replyForm", "author", "body", "avatarColor"));
    }

    @Test
    @DisplayName("返信削除_POST_削除後に投稿詳細へリダイレクトする")
    void deleteReply_whenReplyExists_redirectsToDetail() throws Exception {
        given(postService.deleteReply(1L, 2L)).willReturn(true);

        mockMvc.perform(post("/posts/1/replies/2/delete"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).deleteReply(1L, 2L);
    }

    private static String clientHash(String remoteAddr, String userAgent) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashed = digest.digest((remoteAddr + userAgent).getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hashed) {
            hex.append(String.format("%02x", b));
        }
        return hex.substring(0, 8);
    }

    private static String styleBlock(String css, String selector) {
        int start = css.indexOf(selector);
        int end = css.indexOf('}', start);
        return css.substring(start, end + 1);
    }
}
