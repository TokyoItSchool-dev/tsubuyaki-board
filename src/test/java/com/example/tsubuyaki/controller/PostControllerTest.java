package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
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
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_DB空のとき_空配列をビューに渡し空状態メッセージを表示する")
    void 投稿一覧_DB空のとき_空配列をビューに渡し空状態メッセージを表示する() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", empty()))
                .andExpect(content().string(containsString(">まだ投稿はありません<")));
    }

    @Test
    @DisplayName("投稿一覧_表示時_更新ボタンはpostsスラッシュへGETリクエストする")
    void 投稿一覧_表示時_更新ボタンはpostsスラッシュへGetリクエストする() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<form class=\"post-list__refresh\" method=\"get\" action=\"/posts/\">")))
                .andExpect(content().string(containsString("<button type=\"submit\">更新</button>")));
    }

    @Test
    @DisplayName("投稿一覧_投稿があるとき_投稿者内容投稿日の順に表示する")
    void 投稿一覧_投稿があるとき_投稿者内容投稿日の順に表示する() throws Exception {
        Post post = new Post("suzuki", "表示順タイトル", "表示順を確認します", LocalDateTime.of(2026, 6, 26, 10, 0));
        ReflectionTestUtils.setField(post, "id", 123L);
        given(postService.latest()).willReturn(List.of(post));

        String html = mockMvc.perform(get("/posts/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("suzuki", "表示順タイトル", "表示順を確認します", "2026-06-26 10:00");
        assertThat(html.indexOf("suzuki")).isLessThan(html.indexOf("表示順タイトル"));
        assertThat(html.indexOf("表示順タイトル")).isLessThan(html.indexOf("表示順を確認します"));
        assertThat(html.indexOf("表示順を確認します")).isLessThan(html.indexOf("2026-06-26 10:00"));
    }

    @Test
    @DisplayName("投稿一覧_投稿タイトルリンク_投稿詳細へ遷移できる")
    void 投稿一覧_投稿タイトルリンク_投稿詳細へ遷移できる() throws Exception {
        Post post = new Post("suzuki", "詳細タイトル", "本文はリンクにしません", LocalDateTime.of(2026, 6, 26, 10, 0));
        ReflectionTestUtils.setField(post, "id", 123L);
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<a class=\"post__title\" href=\"/posts/123\">詳細タイトル</a>")))
                .andExpect(content().string(containsString("<span>本文はリンクにしません</span>")));
    }

    @Test
    @DisplayName("投稿詳細_存在するid_投稿をビューに渡し詳細画面を表示する")
    void 投稿詳細_存在するid_投稿をビューに渡し詳細画面を表示する() throws Exception {
        Post post = new Post("suzuki", "詳細タイトル", "詳細を確認します", LocalDateTime.of(2026, 6, 26, 11, 30));
        given(postService.find(123L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/123"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(content().string(containsString("suzuki")))
                .andExpect(content().string(containsString("詳細タイトル")))
                .andExpect(content().string(containsString("詳細を確認します")))
                .andExpect(content().string(containsString("2026-06-26 11:30")))
                .andExpect(content().string(containsString("<a href=\"/posts\">一覧に戻る</a>")));
    }

    @Test
    @DisplayName("投稿詳細_存在しないid_404を返す")
    void 投稿詳細_存在しないid_404を返す() throws Exception {
        given(postService.find(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_空のPostFormをビューに渡しフォームを表示する")
    void 投稿作成フォーム_GET_posts_new_空のPostFormをビューに渡しフォームを表示する() throws Exception {
        PostForm postForm = (PostForm) mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"))
                .andExpect(content().string(containsString("<form action=\"/posts\" method=\"post\" novalidate>")))
                .andReturn()
                .getModelAndView()
                .getModel()
                .get("postForm");

        assertThat(postForm.getAuthor()).isEmpty();
        assertThat(postForm.getTitle()).isEmpty();
        assertThat(postForm.getBody()).isEmpty();
    }

    @Test
    @DisplayName("投稿作成フォーム_表示時_ブラウザ標準バリデーションを無効にする")
    void 投稿作成フォーム_表示時_ブラウザ標準バリデーションを無効にする() throws Exception {
        String html = mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("<form action=\"/posts\" method=\"post\" novalidate>");
        assertThat(html).doesNotContain(" required", "maxlength=\"");
    }

    @Test
    @DisplayName("投稿登録_入力が最小文字数のとき_投稿を作成し一覧へリダイレクトする")
    void 投稿登録_入力が最小文字数のとき_投稿を作成し一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", " a ")
                        .param("title", " t ")
                        .param("body", " b "))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("a", "t", "b");
    }

    @Test
    @DisplayName("投稿登録_入力が最大文字数のとき_投稿を作成し一覧へリダイレクトする")
    void 投稿登録_入力が最大文字数のとき_投稿を作成し一覧へリダイレクトする() throws Exception {
        String author = "a".repeat(30);
        String title = "t".repeat(100);
        String body = "b".repeat(280);

        mockMvc.perform(post("/posts")
                        .param("author", author)
                        .param("title", title)
                        .param("body", body))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create(author, title, body);
    }

    @Test
    @DisplayName("投稿登録_投稿者が空白のみのとき_フォームを再表示しエラーを表示する")
    void 投稿登録_投稿者が空白のみのとき_フォームを再表示しエラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("title", "タイトル")
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名を入力してください")));

        verify(postService, never()).create(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("投稿登録_投稿者が空文字のとき_フォームを再表示しエラーを表示する")
    void 投稿登録_投稿者が空文字のとき_フォームを再表示しエラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "")
                        .param("title", "タイトル")
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名を入力してください")));

        verify(postService, never()).create(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("投稿登録_投稿者が31文字のとき_フォームを再表示しエラーを表示する")
    void 投稿登録_投稿者が31文字のとき_フォームを再表示しエラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "a".repeat(31))
                        .param("title", "タイトル")
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名は 30 文字以内で入力してください")));

        verify(postService, never()).create(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("投稿登録_本文が空白のみのとき_フォームを再表示しエラーを表示する")
    void 投稿登録_本文が空白のみのとき_フォームを再表示しエラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "suzuki")
                        .param("title", "タイトル")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")));

        verify(postService, never()).create(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("投稿登録_本文が空文字のとき_フォームを再表示しエラーを表示する")
    void 投稿登録_本文が空文字のとき_フォームを再表示しエラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "suzuki")
                        .param("title", "タイトル")
                        .param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")));

        verify(postService, never()).create(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("投稿登録_本文が281文字のとき_フォームを再表示しエラーを表示する")
    void 投稿登録_本文が281文字のとき_フォームを再表示しエラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "suzuki")
                        .param("title", "タイトル")
                        .param("body", "b".repeat(281)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文は 280 文字以内で入力してください")));

        verify(postService, never()).create(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("投稿登録_タイトルが空白のみのとき_フォームを再表示しエラーを表示する")
    void 投稿登録_タイトルが空白のみのとき_フォームを再表示しエラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "suzuki")
                        .param("title", "   ")
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "title"))
                .andExpect(content().string(containsString("タイトルを入力してください")));

        verify(postService, never()).create(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("投稿登録_タイトルが101文字のとき_フォームを再表示しエラーを表示する")
    void 投稿登録_タイトルが101文字のとき_フォームを再表示しエラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "suzuki")
                        .param("title", "t".repeat(101))
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "title"))
                .andExpect(content().string(containsString("タイトルは 100 文字以内で入力してください")));

        verify(postService, never()).create(anyString(), anyString(), anyString());
    }
}
