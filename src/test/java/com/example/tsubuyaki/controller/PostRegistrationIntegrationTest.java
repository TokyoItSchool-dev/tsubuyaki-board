package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class PostRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("投稿作成_登録成功後_H2のpostsテーブルに1件増える")
    void create_validValues_increasesPostCount() throws Exception {
        long before = postRepository.count();
        String body = "integration-test-body";

        try {
            mockMvc.perform(post("/posts")
                            .param("author", "alice")
                            .param("body", body)
                            .param("avatarColor", "#2563eb"))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/posts"));

            assertThat(postRepository.count()).isEqualTo(before + 1);
        } finally {
            postRepository.findAll().stream()
                    .filter(post -> body.equals(post.getBody()))
                    .forEach(postRepository::delete);
        }
    }
}
