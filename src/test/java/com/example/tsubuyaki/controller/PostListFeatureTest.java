package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostListFeatureTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("投稿一覧_GET_posts_空表示と新着50件と更新ボタンと表示順を満たす")
    void 投稿一覧_GET_posts_空表示と新着50件と更新ボタンと表示順を満たす() throws Exception {
        postRepository.deleteAll();

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(content().string(containsString("まだ投稿はありません")));

        LocalDateTime base = LocalDateTime.of(2026, 5, 23, 18, 0);
        for (int i = 0; i < 51; i++) {
            postRepository.save(new Post("user" + i, "body" + i, base.plusMinutes(i)));
        }
        postRepository.flush();

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(content().string(containsString("action=\"/posts\"")))
                .andExpect(content().string(containsString("method=\"get\"")))
                .andExpect(content().string(containsString("更新")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long latestPostId = postRepository.findTop50ByOrderByCreatedAtDesc().getFirst().getId();

        assertThat(html).contains("user50", "body50", "2026-05-23 18:50");
        assertThat(html).doesNotContain("user0", "body0");
        assertThat(html).contains("href=\"/posts/" + latestPostId + "\"");
        assertThat(html).containsSubsequence("user50", "body50", "2026-05-23 18:50");
    }
}
