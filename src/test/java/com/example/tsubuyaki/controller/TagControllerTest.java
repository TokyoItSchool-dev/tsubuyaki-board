package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(TagController.class)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("タグ別投稿一覧_関連投稿があるとき_投稿一覧ビューに渡す")
    void タグ別投稿一覧_関連投稿があるとき_投稿一覧ビューに渡す() throws Exception {
        Post post = new Post("alice", "#spring 投稿",
                Instant.parse("2026-05-23T10:00:00Z"));
        post.addTag(new Tag("spring"));
        List<Post> posts = List.of(post);
        given(postService.findByTagName("spring")).willReturn(posts);

        mockMvc.perform(get("/tags/{name}", "spring"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", posts))
                .andExpect(model().attribute("tagName", "spring"))
                .andExpect(model().attribute("q", ""))
                .andExpect(content().string(containsString("#spring")))
                .andExpect(content().string(containsString("href=\"/tags/spring\"")));

        verify(postService).findByTagName("spring");
    }

    @Test
    @DisplayName("タグ別投稿一覧_関連投稿が0件のとき_まだ投稿はありませんを表示する")
    void タグ別投稿一覧_関連投稿が0件のとき_まだ投稿はありませんを表示する() throws Exception {
        given(postService.findByTagName("missing")).willReturn(List.of());

        mockMvc.perform(get("/tags/{name}", "missing"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of()))
                .andExpect(model().attribute("tagName", "missing"))
                .andExpect(content().string(containsString("まだ投稿はありません")));

        verify(postService).findByTagName("missing");
    }
}
