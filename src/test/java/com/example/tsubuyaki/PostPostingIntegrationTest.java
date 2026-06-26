package com.example.tsubuyaki;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class PostPostingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("投稿登録_テストデータを入力して投稿_投稿一覧の一番上に表示される")
    void postFormSubmit_showsCreatedPostAtTopOfList() throws Exception {
        postRepository.save(new Post("bob", "古い投稿", Instant.parse("2026-05-23T09:00:00Z")));

        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "投稿テスト本文"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("alice", "投稿テスト本文", "bob", "古い投稿");
        assertThat(html.indexOf("投稿テスト本文")).isLessThan(html.indexOf("古い投稿"));
    }
}
