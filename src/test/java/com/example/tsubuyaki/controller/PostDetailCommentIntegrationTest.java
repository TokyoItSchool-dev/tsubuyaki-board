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

import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:post_detail_comment_integration;MODE=Oracle;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=true",
        "spring.jpa.open-in-view=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class PostDetailCommentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("投稿詳細_コメント投稿したとき_同じ詳細へ戻りコメント一覧に表示する")
    void detail_コメント投稿したとき_同じ詳細へ戻りコメント一覧に表示する() throws Exception {
        Post post = postRepository.saveAndFlush(new Post(
                "alice",
                "コメント対象の投稿です",
                Instant.parse("2026-06-30T00:00:00Z")));

        mockMvc.perform(get("/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("method=\"post\" action=\"/posts/" + post.getId() + "/comments\"")))
                .andExpect(content().string(containsString("name=\"body\"")));

        mockMvc.perform(post("/posts/{id}/comments", post.getId())
                        .param("body", "確認しました"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/" + post.getId()));

        mockMvc.perform(get("/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("確認しました")));
    }
}
