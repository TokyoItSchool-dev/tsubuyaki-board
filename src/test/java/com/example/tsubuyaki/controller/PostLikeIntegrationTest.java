package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class PostLikeIntegrationTest {

    private static final String REMOTE_ADDR = "203.0.113.10";

    private static final String USER_AGENT = "JUnit Like Client";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("いいね_新規いいねのとき_詳細画面へリダイレクトする")
    void いいね_新規いいねのとき_詳細画面へリダイレクトする() throws Exception {
        Post post = savePost();

        mockMvc.perform(post("/posts/{id}/likes", post.getId())
                        .with(request -> {
                            request.setRemoteAddr(REMOTE_ADDR);
                            return request;
                        })
                        .header("User-Agent", USER_AGENT))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/" + post.getId()));
    }

    @Test
    @DisplayName("いいね_同一クライアントで2回押したとき_いいね数が元に戻る")
    void いいね_同一クライアントで2回押したとき_いいね数が元に戻る() throws Exception {
        Post post = savePost();

        toggleLike(post);
        toggleLike(post);

        mockMvc.perform(get("/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("likeCount", 0L));
    }

    @Test
    @DisplayName("投稿詳細_表示するとき_いいね数とLikeボタンを表示する")
    void 投稿詳細_表示するとき_いいね数とLikeボタンを表示する() throws Exception {
        Post post = savePost();

        toggleLike(post);

        mockMvc.perform(get("/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("likeCount", 1L))
                .andExpect(content().string(containsString("いいね数")))
                .andExpect(content().string(containsString("Like")))
                .andExpect(content().string(containsString("action=\"/posts/" + post.getId() + "/likes\"")));
    }

    private Post savePost() {
        return postRepository.saveAndFlush(
                new Post("alice", "いいね対象の本文", Instant.parse("2026-05-23T10:00:00Z")));
    }

    private void toggleLike(Post post) throws Exception {
        mockMvc.perform(post("/posts/{id}/likes", post.getId())
                        .with(request -> {
                            request.setRemoteAddr(REMOTE_ADDR);
                            return request;
                        })
                        .header("User-Agent", USER_AGENT))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/" + post.getId()));
    }
}
