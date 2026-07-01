package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_0件のとき_空メッセージと更新ボタンを表示する")
    void 投稿一覧_0件のとき_空メッセージと更新ボタンを表示する() throws Exception {
        given(postService.search(null)).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", Collections.emptyList()))
                .andExpect(content().string(containsString("まだ投稿はありません")))
                .andExpect(content().string(containsString("name=\"q\"")))
                .andExpect(content().string(containsString("action=\"/posts/\"")))
                .andExpect(content().string(containsString("type=\"submit\"")))
                .andExpect(content().string(containsString("更新")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン押下時_postsスラッシュで一覧を再表示する")
    void 投稿一覧_更新ボタン押下時_postsスラッシュで一覧を再表示する() throws Exception {
        given(postService.search(null)).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", Collections.emptyList()))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_キーワード指定ありのとき_本文に含む投稿だけを表示する")
    void 投稿一覧_キーワード指定ありのとき_本文に含む投稿だけを表示する() throws Exception {
        Post matchPost = new Post("alice", "Springの話題", Instant.parse("2026-05-23T10:00:00Z"));
        List<Post> searchResults = List.of(matchPost);
        given(postService.search("Spring")).willReturn(searchResults);

        mockMvc.perform(get("/posts").param("q", "Spring"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", searchResults))
                .andExpect(content().string(containsString("Springの話題")))
                .andExpect(result -> {
                    Object modelPosts = result.getModelAndView().getModel().get("posts");
                    assertThat(modelPosts).isInstanceOf(List.class);
                    assertThat((List<?>) modelPosts)
                            .extracting(post -> ((Post) post).getBody())
                            .containsExactly("Springの話題");
                });
    }

    @Test
    @DisplayName("投稿一覧_キーワード空文字のとき_全件を表示する")
    void 投稿一覧_キーワード空文字のとき_全件を表示する() throws Exception {
        List<Post> posts = List.of(
                new Post("alice", "Springの話題", Instant.parse("2026-05-23T10:00:00Z")),
                new Post("bob", "Javaの話題", Instant.parse("2026-05-23T09:00:00Z")));
        given(postService.search("")).willReturn(posts);

        mockMvc.perform(get("/posts").param("q", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", posts));
    }

    @Test
    @DisplayName("投稿作成フォーム_表示するとき_フォーム用モデルを積んで表示する")
    void 投稿作成フォーム_表示するとき_フォーム用モデルを積んで表示する() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form.html"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)));
    }

    @Test
    @DisplayName("投稿登録_有効な値のとき_保存して一覧へリダイレクトする")
    void 投稿登録_有効な値のとき_保存して一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "こんにちは"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "こんにちは");
    }

    @Test
    @DisplayName("投稿登録_author空のとき_フォームを再表示してエラーを積む")
    void 投稿登録_author空のとき_フォームを再表示してエラーを積む() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "")
                        .param("body", "こんにちは"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"));

        verify(postService, never()).create("", "こんにちは");
    }

    @Test
    @DisplayName("投稿登録_author空白のみのとき_フォームを再表示してエラーを積む")
    void 投稿登録_author空白のみのとき_フォームを再表示してエラーを積む() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "こんにちは"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"));

        verify(postService, never()).create("   ", "こんにちは");
    }

    @Test
    @DisplayName("投稿登録_author31文字のとき_フォームを再表示してエラーを積む")
    void 投稿登録_author31文字のとき_フォームを再表示してエラーを積む() throws Exception {
        String author = "a".repeat(31);

        mockMvc.perform(post("/posts")
                        .param("author", author)
                        .param("body", "こんにちは"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"));

        verify(postService, never()).create(author, "こんにちは");
    }

    @Test
    @DisplayName("投稿登録_body空のとき_フォームを再表示してエラーを積む")
    void 投稿登録_body空のとき_フォームを再表示してエラーを積む() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"));

        verify(postService, never()).create("alice", "");
    }

    @Test
    @DisplayName("投稿登録_body空白のみのとき_フォームを再表示してエラーを積む")
    void 投稿登録_body空白のみのとき_フォームを再表示してエラーを積む() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"));

        verify(postService, never()).create("alice", "   ");
    }

    @Test
    @DisplayName("投稿登録_body281文字のとき_フォームを再表示してエラーを積む")
    void 投稿登録_body281文字のとき_フォームを再表示してエラーを積む() throws Exception {
        String body = "a".repeat(281);

        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", body))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"));

        verify(postService, never()).create("alice", body);
    }

    @Test
    @DisplayName("投稿詳細_存在するIDのとき_投稿をビューに渡して表示する")
    void 投稿詳細_存在するIDのとき_投稿をビューに渡して表示する() throws Exception {
        Post post = new Post("alice", "詳細本文", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail.html"))
                .andExpect(model().attribute("post", post));
    }

    @Test
    @DisplayName("投稿詳細_存在しないIDのとき_404を返す")
    void 投稿詳細_存在しないIDのとき_404を返す() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿一覧_投稿があるとき_投稿者_内容_投稿日の順に表示する")
    void 投稿一覧_投稿があるとき_投稿者_内容_投稿日の順に表示する() throws Exception {
        List<Post> posts = List.of(
                new Post("alice", "長い本文が折り返される", Instant.parse("2026-05-23T10:00:00Z")));
        given(postService.search(null)).willReturn(posts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", posts))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("長い本文が折り返される")))
                .andExpect(content().string(containsString("2026-05-23 19:00")))
                .andExpect(content().string(containsString("post__body")))
                .andExpect(result -> {
                    String html = result.getResponse().getContentAsString();
                    assertThat(html.indexOf("alice")).isLessThan(html.indexOf("長い本文が折り返される"));
                    assertThat(html.indexOf("長い本文が折り返される"))
                            .isLessThan(html.indexOf("2026-05-23 19:00"));
                });
    }
}
