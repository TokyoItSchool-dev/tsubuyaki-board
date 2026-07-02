package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.PostNotFoundException;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostDto;
import com.example.tsubuyaki.web.dto.PostDetailDto;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
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
    @DisplayName("投稿一覧_最新投稿があるとき_投稿を新着順でビューに渡す")
    void list_whenLatestPostsExist_passesPostsToViewInNewestOrder() throws Exception {
        PostDto newerPost = new PostDto(1L, "alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z"));
        PostDto olderPost = new PostDto(2L, "bob", "古い投稿", Instant.parse("2026-05-23T09:00:00Z"));
        given(postService.latest()).willReturn(List.of(newerPost, olderPost));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", contains(newerPost, olderPost)))
                .andExpect(content().string(containsString("新しい投稿")))
                .andExpect(content().string(containsString("古い投稿")));
    }

    @Test
    @DisplayName("投稿一覧_投稿がないとき_空メッセージを表示する")
    void list_whenNoPosts_displaysEmptyMessage() throws Exception {
        given(postService.latest()).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", empty()))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_投稿があるとき_投稿者内容投稿日の順に表示する")
    void list_whenPostsExist_displaysAuthorBodyCreatedAtInOrder() throws Exception {
        PostDto post = new PostDto(1L, "alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.latest()).willReturn(List.of(post));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("新しい投稿")))
                .andExpect(content().string(containsString("2026-05-23 19:00")))
                .andReturn();

        String html = result.getResponse().getContentAsString();

        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("新しい投稿"));
        assertThat(html.indexOf("新しい投稿")).isLessThan(html.indexOf("2026-05-23 19:00"));
    }

    @Test
    @DisplayName("投稿一覧_投稿があるとき_詳細画面へのリンクを表示する")
    void list_whenPostsExist_displaysDetailLink() throws Exception {
        PostDto post = new PostDto(1L, "alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<a href=\"/posts/1\">詳細</a>")));
    }

    @Test
    @DisplayName("投稿作成フォーム_表示時_空のフォームをビューに渡す")
    void newForm_whenDisplayed_passesEmptyFormToView() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)))
                .andExpect(content().string(containsString("<form action=\"/posts\" method=\"post\">")))
                .andExpect(content().string(containsString("<button type=\"submit\">投稿</button>")))
                .andExpect(content().string(containsString("新規投稿")));
    }

    @Test
    @DisplayName("投稿作成_author空文字_フォームを再表示しエラーを表示する")
    void create_whenAuthorBlank_returnsFormWithError() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "")
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_author空白のみ_フォームを再表示しエラーを表示する")
    void create_whenAuthorWhitespace_returnsFormWithError() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_author31文字_フォームを再表示しエラーを表示する")
    void create_whenAuthorTooLong_returnsFormWithError() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "a".repeat(31))
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名は 30 文字以内で入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_body空文字_フォームを再表示しエラーを表示する")
    void create_whenBodyBlank_returnsFormWithError() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_body空白のみ_フォームを再表示しエラーを表示する")
    void create_whenBodyWhitespace_returnsFormWithError() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_body281文字_フォームを再表示しエラーを表示する")
    void create_whenBodyTooLong_returnsFormWithError() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "b".repeat(281)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文は 280 文字以内で入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_入力正常_投稿を保存して一覧へリダイレクトする")
    void create_whenValid_savesPostAndRedirectsToList() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "登録した投稿"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().create(assertThatForm("alice", "登録した投稿"));
    }

    private PostForm assertThatForm(String author, String body) {
        return org.mockito.ArgumentMatchers.argThat(form ->
                author.equals(form.getAuthor()) && body.equals(form.getBody()));
    }

    @Test
    @DisplayName("投稿詳細_存在する投稿_詳細画面を表示しmodelに投稿を積む")
    void detail_whenPostExists_displaysDetailViewWithPost() throws Exception {
        PostDto post = new PostDto(1L, "alice", "詳細本文", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.getDetail(1L, "203.0.113.10", "JUnit UA"))
                .willReturn(new PostDetailDto(post, 3L, true));

        mockMvc.perform(get("/posts/1")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        })
                        .header("User-Agent", "JUnit UA"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(model().attribute("likeCount", 3L))
                .andExpect(model().attribute("liked", true))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("詳細本文")))
                .andExpect(content().string(containsString("2026-05-23 19:00")))
                .andExpect(content().string(containsString("いいね 3")))
                .andExpect(content().string(containsString("<form action=\"/posts/1/likes\" method=\"post\">")))
                .andExpect(content().string(containsString("like-toggle--on")))
                .andExpect(content().string(containsString("<span class=\"like-toggle__text\">Like</span>")));
    }

    @Test
    @DisplayName("投稿詳細_未いいねのclientHash_トグルをOFF表示する")
    void detail_whenClientHasNotLiked_displaysToggleOff() throws Exception {
        PostDto post = new PostDto(1L, "alice", "詳細本文", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.getDetail(1L, "203.0.113.10", "JUnit UA"))
                .willReturn(new PostDetailDto(post, 0L, false));

        mockMvc.perform(get("/posts/1")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        })
                        .header("User-Agent", "JUnit UA"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("liked", false))
                .andExpect(content().string(containsString("class=\"like-toggle\"")));
    }

    @Test
    @DisplayName("投稿詳細_存在する投稿_投稿者内容投稿日の順に表示する")
    void detail_whenPostExists_displaysAuthorBodyCreatedAtInOrder() throws Exception {
        PostDto post = new PostDto(1L, "alice", "詳細本文", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.getDetail(1L, "127.0.0.1", null)).willReturn(new PostDetailDto(post, 0L, false));

        MvcResult result = mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString();

        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("詳細本文"));
        assertThat(html.indexOf("詳細本文")).isLessThan(html.indexOf("2026-05-23 19:00"));
    }

    @Test
    @DisplayName("投稿詳細_存在しない投稿_404を返す")
    void detail_whenPostDoesNotExist_returns404() throws Exception {
        doThrow(new PostNotFoundException(999L)).when(postService).getDetail(999L, "127.0.0.1", null);

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿詳細_存在する投稿_一覧に戻るリンクを表示する")
    void detail_whenPostExists_displaysBackToListLink() throws Exception {
        PostDto post = new PostDto(1L, "alice", "詳細本文", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.getDetail(1L, "127.0.0.1", null)).willReturn(new PostDetailDto(post, 0L, false));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<a href=\"/posts\">一覧に戻る</a>")));
    }

    @Test
    @DisplayName("投稿詳細_本文にHTMLが含まれるとき_エスケープ表示する")
    void detail_whenBodyContainsHtml_escapesBody() throws Exception {
        PostDto post = new PostDto(1L, "alice", "<script>alert('xss')</script>",
                Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.getDetail(1L, "127.0.0.1", null)).willReturn(new PostDetailDto(post, 0L, false));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;")));
    }

    @Test
    @DisplayName("いいね_詳細画面で押したとき_clientHashでトグルして詳細へリダイレクトする")
    void like_whenPostExists_togglesLikeAndRedirectsToDetail() throws Exception {
        mockMvc.perform(post("/posts/1/likes")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        })
                        .header("User-Agent", "JUnit UA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        then(postService).should().toggleLike(1L, "203.0.113.10", "JUnit UA");
    }

    @Test
    @DisplayName("いいね_存在しない投稿_404を返す")
    void like_whenPostDoesNotExist_returns404() throws Exception {
        doThrow(new PostNotFoundException(999L))
                .when(postService).toggleLike(999L, "203.0.113.10", "JUnit UA");

        mockMvc.perform(post("/posts/999/likes")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        })
                        .header("User-Agent", "JUnit UA"))
                .andExpect(status().isNotFound());
    }
}
