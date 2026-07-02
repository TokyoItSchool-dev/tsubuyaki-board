package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.service.dto.PostApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostApiController.class)
class PostApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿一覧API_GET_api_posts_JSONで投稿一覧を返す")
    void list_returnsPostsAsJson() throws Exception {
        PostApiResponse post = new PostApiResponse(
                42L,
                "alice",
                "#java 本文です",
                "purple",
                Instant.parse("2026-05-23T01:00:00Z"),
                Instant.parse("2026-05-23T01:00:00Z"),
                List.of("java", "spring"),
                3L);
        given(postService.latestForApi()).willReturn(List.of(post));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(42))
                .andExpect(jsonPath("$[0].author").value("alice"))
                .andExpect(jsonPath("$[0].body").value("#java 本文です"))
                .andExpect(jsonPath("$[0].avatarColor").value("purple"))
                .andExpect(jsonPath("$[0].createdAt").value("2026-05-23T01:00:00Z"))
                .andExpect(jsonPath("$[0].updatedAt").value("2026-05-23T01:00:00Z"))
                .andExpect(jsonPath("$[0].tags[0]").value("java"))
                .andExpect(jsonPath("$[0].tags[1]").value("spring"))
                .andExpect(jsonPath("$[0].likesCount").value(3));

        verify(postService).latestForApi();
    }
}
