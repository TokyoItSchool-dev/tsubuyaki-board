package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:post_list_integration;MODE=Oracle;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=true",
        "spring.jpa.open-in-view=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class PostListIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("投稿一覧_DBのタグ付き投稿を表示するとき_タグリンクを描画できる")
    void list_DBのタグ付き投稿を表示するとき_タグリンクを描画できる() throws Exception {
        Post post = new Post(
                "alice",
                "タグ付き投稿です #java",
                Instant.parse("2026-06-30T00:00:00Z"));
        post.addTag(new Tag("java", Instant.parse("2026-06-30T00:00:00Z")));
        postRepository.saveAndFlush(post);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/tags/java\"")))
                .andExpect(content().string(containsString(">#java<")));
    }
}
