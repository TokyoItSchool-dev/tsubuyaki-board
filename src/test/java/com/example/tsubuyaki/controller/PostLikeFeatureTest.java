package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostLikeFeatureTest {

    private static final String CLIENT_IP = "203.0.113.10";
    private static final String USER_AGENT = "TsubuyakiTestBrowser/1.0";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("いいね_POST_posts_id_likes_押下元画面を維持して同一clientHashで登録解除する")
    void いいね_POST_posts_id_likes_押下元画面を維持して同一clientHashで登録解除する() throws Exception {
        postRepository.deleteAll();
        Post savedPost = postRepository.save(new Post(
                "like-user",
                "like-body",
                LocalDateTime.of(2026, 5, 23, 18, 0)));
        postRepository.flush();

        String likeAction = "/posts/" + savedPost.getId() + "/likes";

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(content().string(containsString("action=\"" + likeAction + "\"")))
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(containsString("name=\"returnTo\" value=\"list\"")))
                .andExpect(content().string(containsString("Like")));

        mockMvc.perform(get("/posts/{id}", savedPost.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(content().string(containsString("いいね 0")))
                .andExpect(content().string(containsString("action=\"" + likeAction + "\"")))
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(containsString("name=\"returnTo\" value=\"detail\"")))
                .andExpect(content().string(containsString("Like")));

        mockMvc.perform(post("/posts/{id}/likes", savedPost.getId())
                        .param("returnTo", "list")
                        .with(testClient()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        assertThat(countLikes(savedPost.getId(), clientHash())).isEqualTo(1);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(content().string(containsString("いいね 1")));

        mockMvc.perform(post("/posts/{id}/likes", savedPost.getId())
                        .param("returnTo", "detail")
                        .with(testClient()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/" + savedPost.getId()));

        assertThat(countLikes(savedPost.getId(), clientHash())).isZero();

        mockMvc.perform(get("/posts/{id}", savedPost.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(content().string(containsString("いいね 0")));

        mockMvc.perform(post("/posts/{id}/likes", savedPost.getId() + 1).with(testClient()))
                .andExpect(status().isNotFound());
    }

    private RequestPostProcessor testClient() {
        return request -> {
            request.setRemoteAddr(CLIENT_IP);
            request.addHeader("User-Agent", USER_AGENT);
            return request;
        };
    }

    private Integer countLikes(Long postId, String clientHash) {
        return jdbcTemplate.queryForObject(
                "select count(*) from post_likes where post_id = ? and client_hash = ?",
                Integer.class,
                postId,
                clientHash);
    }

    private String clientHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((CLIENT_IP + USER_AGENT).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 4);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
