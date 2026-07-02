package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.config.SecurityConfig;
import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostDetail;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiPostController.class)
@Import(SecurityConfig.class)
class ApiPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Test
    @DisplayName("投稿API_GET_api_posts_JSONで投稿一覧を返す")
    void 投稿API_GET_api_posts_JSONで投稿一覧を返す() throws Exception {
        given(postService.latestDetails()).willReturn(List.of(
                new PostDetail(
                        new Post(1L, "alice", "BLUE", "API の共有です", Instant.parse("2026-06-26T09:00:00Z")),
                        3L),
                new PostDetail(
                        new Post(2L, "bob", "GREEN", "2件目です", Instant.parse("2026-06-26T08:00:00Z")),
                        0L)));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].author").value("alice"))
                .andExpect(jsonPath("$[0].avatarColor").value("BLUE"))
                .andExpect(jsonPath("$[0].body").value("API の共有です"))
                .andExpect(jsonPath("$[0].createdAt").value("2026-06-26T09:00:00Z"))
                .andExpect(jsonPath("$[0].likeCount").value(3L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].author").value("bob"))
                .andExpect(jsonPath("$[1].avatarColor").value("GREEN"))
                .andExpect(jsonPath("$[1].body").value("2件目です"))
                .andExpect(jsonPath("$[1].createdAt").value("2026-06-26T08:00:00Z"))
                .andExpect(jsonPath("$[1].likeCount").value(0L));
    }
}
