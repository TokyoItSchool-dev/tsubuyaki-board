package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
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
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
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
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(postService.countLikes(1L)).willReturn(3L);

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
                .andExpect(content().string(containsString("Like")))
                .andExpect(content().string(containsString("action=\"/posts/1/likes\"")))
                .andExpect(content().string(containsString("method=\"post\"")));
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

    private static String clientHash(String remoteAddr, String userAgent) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashed = digest.digest((remoteAddr + userAgent).getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hashed) {
            hex.append(String.format("%02x", b));
        }
        return hex.substring(0, 8);
    }
}
