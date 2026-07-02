package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class PostListIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Test
    @DisplayName("投稿一覧_51件以上の投稿がある場合_画面には新着50件だけを表示する")
    void 投稿一覧_51件以上の投稿がある場合_画面には新着50件だけを表示する() throws Exception {
        Instant base = Instant.parse("2026-05-23T10:00:00Z");
        List<Post> posts = new ArrayList<>();
        posts.add(new Post("oldest", "除外される投稿", base));
        for (int i = 1; i <= 50; i++) {
            posts.add(new Post("user" + i, "表示される投稿" + i, base.plusSeconds(i)));
        }
        postRepository.saveAll(posts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", hasSize(50)))
                .andExpect(content().string(containsString("表示される投稿50")))
                .andExpect(content().string(containsString("表示される投稿1")))
                .andExpect(content().string(not(containsString("除外される投稿"))));
    }

    @Test
    @DisplayName("いいね_POST_posts_id_likes_同一クライアントは追加後に再押下で解除する")
    void いいね_POST_posts_id_likes_同一クライアントは追加後に再押下で解除する() throws Exception {
        Post post = postRepository.save(new Post("alice", "いいね対象です", Instant.parse("2026-05-23T10:00:00Z")));

        mockMvc.perform(get("/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("0 いいね")))
                .andExpect(content().string(containsString("Like")));

        mockMvc.perform(post("/posts/{id}/likes", post.getId())
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        })
                        .header("User-Agent", "MockBrowser/1.0"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/" + post.getId()));

        mockMvc.perform(get("/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1 いいね")));
        assertThat(postLikeRepository.findAll())
                .singleElement()
                .extracting(like -> like.getClientHash())
                .isEqualTo(clientHash("203.0.113.10", "MockBrowser/1.0"));

        mockMvc.perform(post("/posts/{id}/likes", post.getId())
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        })
                        .header("User-Agent", "MockBrowser/1.0"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/" + post.getId()));

        mockMvc.perform(get("/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("0 いいね")));
        assertThat(postLikeRepository.findAll()).isEmpty();
    }

    private String clientHash(String ipAddress, String userAgent) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest((ipAddress + userAgent).getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest).substring(0, 8);
    }
}
