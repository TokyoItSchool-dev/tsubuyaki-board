package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.ClientHashGenerator;
import com.example.tsubuyaki.service.PostLikeService;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostFormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private PostLikeService postLikeService;

    @MockitoBean
    private ClientHashGenerator clientHashGenerator;

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_フォーム画面と空Formを返す")
    void 投稿作成フォーム_GET_posts_new_フォーム画面と空Formを返す() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", allOf(
                        instanceOf(PostForm.class),
                        hasProperty("author", nullValue()),
                        hasProperty("body", nullValue())
                )));
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_投稿者と内容の入力項目と一覧リンクを表示する")
    void 投稿作成フォーム_GET_posts_new_投稿者と内容の入力項目と一覧リンクを表示する() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<form")))
                .andExpect(content().string(containsString("投稿者")))
                .andExpect(content().string(containsString("name=\"author\"")))
                .andExpect(content().string(containsString("本文")))
                .andExpect(content().string(containsString("name=\"body\"")))
                .andExpect(content().string(containsString("href=\"/posts\"")));
    }
}
