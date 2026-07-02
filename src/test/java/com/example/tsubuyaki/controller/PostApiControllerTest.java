package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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
    @DisplayName("投稿API_GET_api_posts_投稿一覧をJSONで返す")
    void list_GET_api_posts_投稿一覧をJSONで返す() throws Exception {
        Post post = postWithId(
                10L,
                "alice",
                "APIで共有します",
                LocalDateTime.parse("2026-06-30T01:15:00"),
                "green");
        given(postService.findLatest50()).willReturn(List.of(post));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].author").value("alice"))
                .andExpect(jsonPath("$[0].body").value("APIで共有します"))
                .andExpect(jsonPath("$[0].avatarColor").value("green"))
                .andExpect(jsonPath("$[0].createdAt").value("2026-06-30T01:15:00"));

        verify(postService).findLatest50();
    }

    private static Post postWithId(
            Long id,
            String author,
            String body,
            LocalDateTime createdAt,
            String avatarColor) {
        Post post = new Post(author, body, createdAt, avatarColor);
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }
}
