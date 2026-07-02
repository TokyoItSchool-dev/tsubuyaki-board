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
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostDetailFeatureTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("投稿詳細_GET_posts_id_存在する投稿は詳細を表示し存在しない投稿は404を返す")
    void 投稿詳細_GET_posts_id_存在する投稿は詳細を表示し存在しない投稿は404を返す() throws Exception {
        postRepository.deleteAll();
        Post savedPost = postRepository.save(new Post(
                "detail-user",
                "detail-body",
                LocalDateTime.of(2026, 5, 23, 18, 0)));
        postRepository.flush();

        mockMvc.perform(get("/posts/{id}", savedPost.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attributeExists("post"))
                .andExpect(content().string(containsString("class=\"app-shell\"")))
                .andExpect(content().string(containsString("class=\"app-header\"")))
                .andExpect(content().string(containsString("社内つぶやきボード")))
                .andExpect(content().string(containsString("href=\"/posts\"")))
                .andExpect(content().string(containsString("href=\"/posts/new\"")))
                .andExpect(content().string(containsString("detail-user")))
                .andExpect(content().string(containsString("detail-body")));

        mockMvc.perform(get("/posts/{id}", savedPost.getId() + 1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿詳細_編集済み表示_updatedAtがある投稿だけ更新日時を表示する")
    void 投稿詳細_編集済み表示_updatedAtがある投稿だけ更新日時を表示する() throws Exception {
        postRepository.deleteAll();
        Post originalPost = postRepository.save(new Post(
                "detail-original-user",
                "detail-original-body",
                LocalDateTime.of(2026, 5, 27, 10, 0)));
        Post editedPost = postRepository.save(new Post(
                "detail-edited-user",
                "detail-edited-body",
                LocalDateTime.of(2026, 5, 27, 11, 0)));
        editedPost.updateBodyAndBackgroundColor(
                "detail-edited-body",
                editedPost.getBackgroundColor(),
                LocalDateTime.of(2026, 5, 27, 12, 30));
        postRepository.flush();

        String editedHtml = mockMvc.perform(get("/posts/{id}", editedPost.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(editedHtml).contains("編集済み", "2026-05-27 12:30");

        mockMvc.perform(get("/posts/{id}", originalPost.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(content().string(not(containsString("編集済み"))));
    }
}
