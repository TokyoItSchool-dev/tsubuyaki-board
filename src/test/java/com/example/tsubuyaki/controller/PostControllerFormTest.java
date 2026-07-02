package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostControllerFormTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿作成フォーム_GET_フォーム画面を表示し空のpostFormを渡す")
    void newForm_whenGet_returnsFormViewWithPostForm() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)))
                .andExpect(content().string(containsString("<h1>新規投稿</h1>")))
                .andExpect(content().string(containsString("name=\"author\"")))
                .andExpect(content().string(containsString("name=\"body\"")))
                .andExpect(content().string(containsString("name=\"avatarColor\"")))
                .andExpect(content().string(containsString("value=\"red\"")))
                .andExpect(content().string(containsString("value=\"blue\"")))
                .andExpect(content().string(containsString("value=\"green\"")))
                .andExpect(content().string(containsString("value=\"yellow\"")))
                .andExpect(content().string(containsString("value=\"gray\"")))
                .andExpect(content().string(containsString("<button type=\"submit\"")))
                .andExpect(content().string(containsString("action=\"/posts\"")))
                .andExpect(content().string(containsString("method=\"post\"")));
    }

    @Test
    @DisplayName("投稿作成_正常な入力_保存して投稿一覧へリダイレクトする")
    void create_whenValidInput_savesAndRedirectsToPosts() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "朝の共有です")
                        .param("avatarColor", "blue"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "朝の共有です", "blue");
    }

    @Test
    @DisplayName("投稿作成_アバター色未選択_デフォルト色で保存して投稿一覧へリダイレクトする")
    void create_whenAvatarColorNotSelected_savesWithDefaultColor() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "朝の共有です")
                        .param("avatarColor", ""))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "朝の共有です", "gray");
    }

    @Test
    @DisplayName("投稿作成_不正なアバター色_フォームを再表示しエラーを表示する")
    void create_whenAvatarColorInvalid_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "朝の共有です")
                        .param("avatarColor", "purple"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "avatarColor"))
                .andExpect(content().string(containsString("アバター色は選択肢から選んでください")));

        verify(postService, never()).create(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("投稿作成_author未入力_フォームを再表示し入力値とエラーを表示する")
    void create_whenAuthorEmpty_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "")
                        .param("body", "朝の共有です"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("朝の共有です")))
                .andExpect(content().string(containsString("投稿者名を入力してください")));

        verify(postService, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("投稿作成_author空白のみ_フォームを再表示しエラーを表示する")
    void create_whenAuthorBlank_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "朝の共有です"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名を入力してください")));
    }

    @Test
    @DisplayName("投稿作成_author31文字_フォームを再表示しエラーを表示する")
    void create_whenAuthorTooLong_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "a".repeat(31))
                        .param("body", "朝の共有です"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名は 30 文字以内で入力してください")));
    }

    @Test
    @DisplayName("投稿作成_body未入力_フォームを再表示し入力値とエラーを表示する")
    void create_whenBodyEmpty_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("本文を入力してください")));
    }

    @Test
    @DisplayName("投稿作成_body空白のみ_フォームを再表示しエラーを表示する")
    void create_whenBodyBlank_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")));
    }

    @Test
    @DisplayName("投稿作成_body281文字_フォームを再表示しエラーを表示する")
    void create_whenBodyTooLong_returnsFormWithErrors() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "b".repeat(281)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文は 280 文字以内で入力してください")));
    }
}
