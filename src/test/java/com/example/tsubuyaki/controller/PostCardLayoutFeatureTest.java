package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.service.ClientHashGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
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

    private static final String OWNER_IP = "203.0.113.10";
    private static final String OWNER_USER_AGENT = "TsubuyakiOwner/1.0";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("投稿カード_一覧と詳細_表示順を維持して読みやすい操作バーにする")
    void 投稿カード_一覧と詳細_表示順を維持して読みやすい操作バーにする() throws Exception {
        postRepository.deleteAll();
        Post savedPost = postRepository.save(new Post(
                "layout-user",
                "layout-body\nlong-long-long-long-long-long-long-long-long-long",
                LocalDateTime.of(2026, 5, 25, 10, 30),
                "#fef9c3",
                ClientHashGenerator.hash(OWNER_IP, OWNER_USER_AGENT)));
        savedPost.updateBodyAndBackgroundColor(
                savedPost.getBody(),
                savedPost.getBackgroundColor(),
                LocalDateTime.of(2026, 5, 25, 11, 45));
        postRepository.flush();

        String listHtml = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String detailHtml = mockMvc.perform(get("/posts/{id}", savedPost.getId()).with(client()))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listHtml).containsSubsequence(
                "post__author",
                "post__body",
                "post__created-at",
                "post__updated-at",
                "post__actions",
                "post__like-count",
                "post__like-button",
                "post__detail-link");
        assertThat(detailHtml).containsSubsequence(
                "post post--detail",
                "post__author",
                "post__body",
                "post__created-at",
                "post__updated-at",
                "post__actions",
                "post__like-count",
                "post__like-button",
                "post__edit-link",
                "post__delete-link");
        assertThat(listHtml).contains("いいね！");
        assertThat(detailHtml).contains("いいね！");

        String css = new ClassPathResource("static/css/app.css").getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).contains(
                ".post {",
                "padding: 1.25rem;",
                ".post__body {",
                "font-size: 1.08rem;",
                "white-space: pre-wrap;",
                "overflow-wrap: anywhere;",
                "word-break: break-word;",
                ".post--detail .post__body {",
                "line-height: 1.75;",
                ".post__author {",
                "font-size: 0.95rem;",
                ".post__actions {",
                "display: flex;",
                "align-items: center;",
                "border-top:",
                "padding-top:",
                ".post__like-count {",
                "border-radius:",
                "background:",
                ".post__like-button {",
                "border-radius: 999px;",
                "@media (max-width: 640px)",
                ".post__actions {",
                "flex-direction: column;");
    }

    private RequestPostProcessor client() {
        return request -> {
            request.setRemoteAddr(OWNER_IP);
            request.addHeader("User-Agent", OWNER_USER_AGENT);
            return request;
        };
    }
}
