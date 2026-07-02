package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostNewFormFeatureTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_posts_formとPostFormをmodelに渡す")
    void 投稿作成フォーム_GET_posts_new_posts_formとPostFormをmodelに渡す() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)));
    }

    @Test
    @DisplayName("投稿作成フォーム_レイアウト_項目間隔とボタン型カラーパレットを表示する")
    void 投稿作成フォーム_レイアウト_項目間隔とボタン型カラーパレットを表示する() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(content().string(containsString("class=\"post-form\"")))
                .andExpect(content().string(containsString("class=\"post-form__field\"")))
                .andExpect(content().string(containsString("class=\"app-shell\"")))
                .andExpect(content().string(containsString("class=\"app-header\"")))
                .andExpect(content().string(containsString("社内つぶやきボード")))
                .andExpect(content().string(containsString("href=\"/posts\"")))
                .andExpect(content().string(containsString("href=\"/posts/new\"")))
                .andExpect(content().string(containsString("投稿者名は必須・30文字以内です。")))
                .andExpect(content().string(containsString("本文は必須・280文字以内です。")))
                .andExpect(content().string(containsString("aria-describedby=\"author-help\"")))
                .andExpect(content().string(containsString("aria-describedby=\"body-help\"")))
                .andExpect(content().string(containsString("class=\"button button--primary\"")))
                .andExpect(content().string(containsString("class=\"color-palette__input\"")))
                .andExpect(content().string(containsString("class=\"color-palette__swatch\"")));

        String css = new ClassPathResource("static/css/app.css").getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).contains(
                ".post-form {",
                "gap: 1rem;",
                ".post-form__field {",
                ".form-help {",
                ".form-actions {",
                ".color-palette__input {",
                "opacity: 0;",
                ".color-palette__input:checked + .color-palette__swatch",
                ".color-palette__swatch:hover",
                "box-shadow:");
    }
}
