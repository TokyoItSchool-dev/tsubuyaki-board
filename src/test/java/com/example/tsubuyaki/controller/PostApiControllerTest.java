package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.TsubuyakiApplication;
import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostApiController.class)
@ContextConfiguration(classes = TsubuyakiApplication.class)
class PostApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿API_一覧取得_投稿をJSON配列で返す")
    void 投稿API_一覧取得_投稿をJSON配列で返す() throws Exception {
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T10:00:00Z"),
                "E0F2FE", "secret12");
        setField(post, "id", 1L);
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].author").value("alice"))
                .andExpect(jsonPath("$[0].body").value("本文です"))
                .andExpect(jsonPath("$[0].createdAt").value("2026-05-23T10:00:00Z"))
                .andExpect(jsonPath("$[0].color").value("E0F2FE"))
                .andExpect(content().string(not(org.hamcrest.Matchers.containsString("clientHash"))));

        then(postService).should().latest();
    }
}
