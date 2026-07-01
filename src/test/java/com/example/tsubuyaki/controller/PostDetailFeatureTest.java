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

import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
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
                Instant.parse("2026-05-23T09:00:00Z")));
        postRepository.flush();

        mockMvc.perform(get("/posts/{id}", savedPost.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attributeExists("post"))
                .andExpect(content().string(containsString("detail-user")))
                .andExpect(content().string(containsString("detail-body")));

        mockMvc.perform(get("/posts/{id}", savedPost.getId() + 1))
                .andExpect(status().isNotFound());
    }
}
