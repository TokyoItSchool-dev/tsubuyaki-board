package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(ApiPostController.class)
class ApiPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿一覧API_GET_api_posts_JSONで投稿一覧を返す")
    void 投稿一覧API_GET_api_posts_JSONで投稿一覧を返す() throws Exception {
        Post post = new Post("taro", "hello world", LocalDateTime.parse("2026-07-02T10:00:00"));
        ReflectionTestUtils.setField(post, "id", 1L);
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].author").value("taro"))
                .andExpect(jsonPath("$[0].body").value("hello world"))
                .andExpect(jsonPath("$[0].createdAt").value("2026-07-02T10:00:00"))
                .andExpect(jsonPath("$[0].deletedAt").doesNotExist())
                .andExpect(jsonPath("$[0].avatarColor").doesNotExist());

        verify(postService).latest();
    }
}
