package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
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
    @DisplayName("API_投稿一覧_GET_api_posts_未削除投稿とタグをJSONで返す")
    void 投稿一覧_GET_api_posts_未削除投稿とタグをJSONで返す() throws Exception {
        Tag javaTag = tagWithId(10L, "java");
        Tag springTag = tagWithId(11L, "spring");
        Post post = postWithId(1L, "alice", "#java #spring の投稿", "green",
                Instant.parse("2026-06-26T10:00:00Z"));
        post.addTag(springTag);
        post.addTag(javaTag);
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].author").value("alice"))
                .andExpect(jsonPath("$[0].body").value("#java #spring の投稿"))
                .andExpect(jsonPath("$[0].avatarColor").value("green"))
                .andExpect(jsonPath("$[0].createdAt").value("2026-06-26T10:00:00Z"))
                .andExpect(jsonPath("$[0].tags", hasSize(2)))
                .andExpect(jsonPath("$[0].tags[*].id", contains(10, 11)))
                .andExpect(jsonPath("$[0].tags[*].name", contains("java", "spring")));

        verify(postService).latest();
    }

    @Test
    @DisplayName("API_投稿一覧_0件のとき_空配列を返す")
    void 投稿一覧_0件のとき_空配列を返す() throws Exception {
        given(postService.latest()).willReturn(List.of());

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(postService).latest();
    }

    private static Post postWithId(
            Long id,
            String author,
            String body,
            String avatarColor,
            Instant createdAt) {
        Post post = new Post(author, body, avatarColor, createdAt);
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    private static Tag tagWithId(Long id, String name) {
        Tag tag = new Tag(name);
        ReflectionTestUtils.setField(tag, "id", id);
        return tag;
    }
}
