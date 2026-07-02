package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ApiPostIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("投稿一覧API_投稿が51件以上と削除済みあり_未削除の新着50件だけをJSONで返す")
    void 投稿一覧API_投稿が51件以上と削除済みあり_未削除の新着50件だけをJSONで返す() throws Exception {
        postRepository.deleteAll();
        LocalDateTime base = LocalDateTime.parse("2026-07-02T10:00:00");
        for (int i = 1; i <= 51; i++) {
            postRepository.save(new Post("user-" + i, "body-" + i, base.plusSeconds(i)));
        }
        Post deleted = new Post("deleted-user", "deleted-body", base.plusSeconds(100));
        deleted.markDeleted(base.plusSeconds(101));
        postRepository.save(deleted);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.length()").value(50))
                .andExpect(jsonPath("$[0].author").value("user-51"))
                .andExpect(jsonPath("$[0].createdAt").value("2026-07-02T10:00:51"))
                .andExpect(jsonPath("$[49].author").value("user-2"))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].body").value("body-51"))
                .andExpect(content().string(not(containsString("\"author\":\"user-1\""))))
                .andExpect(content().string(not(containsString("deleted-user"))))
                .andExpect(content().string(not(containsString("deleted-body"))));
    }
}
