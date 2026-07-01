package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
}
