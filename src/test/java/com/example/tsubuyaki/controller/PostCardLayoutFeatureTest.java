package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostCardLayoutFeatureTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("投稿カード_一覧と詳細_本文を大きく表示しメタ情報といいね操作を見やすく配置する")
    void 投稿カード_一覧と詳細_本文を大きく表示しメタ情報といいね操作を見やすく配置する() throws Exception {
        postRepository.deleteAll();
        Post savedPost = postRepository.save(new Post(
                "layout-user",
                "layout-body",
                LocalDateTime.of(2026, 5, 25, 10, 30)));
        postRepository.flush();

        String listHtml = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String detailHtml = mockMvc.perform(get("/posts/{id}", savedPost.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listHtml).containsSubsequence(
                "post__author",
                "post__body",
                "post__created-at",
                "post__actions",
                "post__like-count",
                "post__like-button");
        assertThat(detailHtml).containsSubsequence(
                "post__author",
                "post__body",
                "post__created-at",
                "post__actions",
                "post__like-count",
                "post__like-button");
        assertThat(listHtml).contains("いいね！");
        assertThat(detailHtml).contains("いいね！");

        String css = new ClassPathResource("static/css/app.css").getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).contains(
                ".post {",
                "padding: 1.25rem;",
                ".post__body {",
                "font-size: 1.08rem;",
                ".post__author {",
                "font-size: 0.95rem;",
                ".post__actions {",
                "display: flex;",
                "align-items: center;",
                ".post__like-count {",
                ".post__like-button {",
                "border-radius: 999px;");
    }
}
