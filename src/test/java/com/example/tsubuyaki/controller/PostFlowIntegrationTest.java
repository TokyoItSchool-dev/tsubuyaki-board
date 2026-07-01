package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PostFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("投稿作成_登録成功後_投稿一覧にデータが反映される")
    void 投稿作成_登録成功後_投稿一覧にデータが反映される() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "integration-user")
                        .param("body", "統合テストから登録した本文です"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("integration-user")))
                .andExpect(content().string(containsString("統合テストから登録した本文です")));
    }
}
