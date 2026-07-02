package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.BodyPart;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("タグ一覧_GET_tags_name_対象タグの投稿のみ表示する")
    void postsByTag_whenMatched_returnsListViewWithPosts() throws Exception {
        Post post = postWithId(1L, "alice", "本文 #java", Instant.parse("2026-07-02T10:00:00Z"));
        given(postService.postsByTag("java")).willReturn(List.of(post));
        given(postService.bodyParts("本文 #java"))
                .willReturn(List.of(BodyPart.text("本文 "), BodyPart.tag("#java", "java")));

        mockMvc.perform(get("/tags/java"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of(post)))
                .andExpect(model().attribute("searched", true))
                .andExpect(model().attribute("q", "#java"))
                .andExpect(model().attribute("likeCounts", Map.of(1L, 0L)))
                .andExpect(model().attribute("replyCounts", Map.of(1L, 0L)))
                .andExpect(content().string(containsString("本文 ")))
                .andExpect(content().string(containsString("href=\"/tags/java\"")))
                .andExpect(content().string(containsString("#java")));
    }

    @Test
    @DisplayName("タグ表示_一覧本文のタグリンク化でテンプレート由来の余白を本文に混ぜない")
    void postsByTag_whenBodyHasTag_rendersBodyPartsInline() throws Exception {
        Post post = postWithId(1L, "alice", "learn #Java today", Instant.parse("2026-07-02T10:00:00Z"));
        given(postService.postsByTag("Java")).willReturn(List.of(post));
        given(postService.bodyParts("learn #Java today"))
                .willReturn(List.of(
                        BodyPart.text("learn "),
                        BodyPart.tag("#Java", "Java"),
                        BodyPart.text(" today")
                ));

        String html = mockMvc.perform(get("/tags/Java"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("<p class=\"post__body\"><span>learn </span><a class=\"tag-link\"");
        assertThat(html).contains("</a><span> today</span></p>");
    }

    @Test
    @DisplayName("タグ一覧_GET_tags_name_タグが存在しない場合は該当なしを表示する")
    void postsByTag_whenNoPosts_showsSearchEmptyMessage() throws Exception {
        given(postService.postsByTag("unknown")).willReturn(Collections.emptyList());

        mockMvc.perform(get("/tags/unknown"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", Collections.emptyList()))
                .andExpect(model().attribute("searched", true))
                .andExpect(content().string(containsString("該当する投稿はありません")));
    }

    @Test
    @DisplayName("投稿詳細_本文中のタグをタグ一覧へのリンクとして表示する")
    void detail_whenBodyHasTag_rendersTagLink() throws Exception {
        Post post = new Post("alice", "本文 #java", Instant.parse("2026-07-02T10:00:00Z"));
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(postService.repliesForPost(1L)).willReturn(List.of());
        given(postService.bodyParts("本文 #java"))
                .willReturn(List.of(BodyPart.text("本文 "), BodyPart.tag("#java", "java")));

        String html = mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("本文 ", "href=\"/tags/java\"", "#java");
    }

    private static Post postWithId(Long id, String author, String body, Instant createdAt) {
        Post post = new Post(author, body, createdAt);
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }
}
