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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostBackgroundColorFeatureTest {

    private static final String SELECTED_COLOR = "#dbeafe";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("投稿背景色_作成フォームで6色選択でき保存色を一覧と詳細に反映する")
    void 投稿背景色_作成フォームで6色選択でき保存色を一覧と詳細に反映する() throws Exception {
        postRepository.deleteAll();

        mockMvc.perform(post("/posts")
                        .param("author", " ")
                        .param("body", "本文")
                        .param("backgroundColor", SELECTED_COLOR))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名を入力してください")));

        String formHtml = mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(content().string(containsString("class=\"required-label\"")))
                .andExpect(content().string(containsString("class=\"color-palette\"")))
                .andExpect(content().string(containsString("name=\"backgroundColor\"")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(formHtml).contains(
                "value=\"#ffffff\"",
                "value=\"#fee2e2\"",
                "value=\"#f3e8ff\"",
                "value=\"#dbeafe\"",
                "value=\"#dcfce7\"",
                "value=\"#fef9c3\"");
        assertThat(formHtml).containsSubsequence(
                "color-palette",
                "#ffffff",
                "#fee2e2",
                "#f3e8ff",
                "#dbeafe",
                "#dcfce7",
                "#fef9c3");

        mockMvc.perform(post("/posts")
                        .param("author", "color-user")
                        .param("body", "color-body")
                        .param("backgroundColor", SELECTED_COLOR))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        Long savedPostId = postRepository.findTop50ByOrderByCreatedAtDesc().getFirst().getId();
        String savedColor = jdbcTemplate.queryForObject(
                "select background_color from posts where id = ?",
                String.class,
                savedPostId);
        assertThat(savedColor).isEqualTo(SELECTED_COLOR);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(content().string(containsString("post--bg-blue")))
                .andExpect(content().string(not(containsString("style=\"background-color:"))))
                .andExpect(content().string(containsString("color-body")));

        Post defaultColorPost = postRepository.save(new Post(
                "default-color-user",
                "default-color-body",
                LocalDateTime.of(2026, 5, 24, 12, 0)));
        postRepository.flush();

        mockMvc.perform(get("/posts/{id}", defaultColorPost.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(content().string(containsString("post--bg-default")))
                .andExpect(content().string(not(containsString("style=\"background-color:"))));
    }
}
