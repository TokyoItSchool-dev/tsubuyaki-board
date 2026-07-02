package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.config.SecurityConfig;
import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(TagController.class)
@Import(SecurityConfig.class)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("タグ別一覧_GET_tags_name_関連投稿を一覧画面に表示する")
    void タグ別一覧_GET_tags_name_関連投稿を一覧画面に表示する() throws Exception {
        given(postService.findPostsByTag("SpringBoot")).willReturn(List.of(
                new Post(1L, "alice", "GREEN", "Spring Bootを勉強しています。 #SpringBoot",
                        Instant.parse("2026-06-26T09:00:00Z"), List.of("SpringBoot"))));

        MvcResult result = mockMvc.perform(get("/tags/{name}", "SpringBoot"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("tagName", "SpringBoot"))
                .andExpect(model().attribute("query", ""))
                .andExpect(content().string(containsString("#SpringBoot の投稿")))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("href=\"/tags/SpringBoot\"")))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).contains(">Spring Bootを勉強しています。</a>");
        assertThat(html).doesNotContain(">Spring Bootを勉強しています。 #SpringBoot</a>");

        assertThat(result.getModelAndView().getModel().get("posts"))
                .asList()
                .allSatisfy(post -> assertThat(post).isInstanceOf(PostResponse.class));
        verify(postService).findPostsByTag("SpringBoot");
    }
}
