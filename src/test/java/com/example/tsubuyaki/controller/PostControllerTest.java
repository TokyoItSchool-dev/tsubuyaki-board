package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import com.example.tsubuyaki.web.dto.PostView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void list_whenNoPosts_showsEmptyMessage() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_検索ボックスと検索ボタンを表示する")
    void list_showsSearchBoxAndButton() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/posts\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("method=\"get\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"q\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("検索")));
    }

    @Test
    @DisplayName("投稿検索_GET_posts_qあり_本文部分一致検索結果を一覧表示する")
    void list_whenQueryProvided_showsSearchResults() throws Exception {
        given(postService.searchByBody("hello")).willReturn(List.of(
                new PostView(new Post(1L, "alice", "hello world", Instant.parse("2026-05-23T10:00:00Z")), 2)));

        mockMvc.perform(get("/posts").param("q", " hello "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("q", "hello"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"hello\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("alice")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("hello world")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("いいね 2")));

        verify(postService).searchByBody("hello");
        verify(postService, never()).latest();
    }

    @Test
    @DisplayName("投稿検索_GET_posts_q空白のみ_通常一覧を表示する")
    void list_whenQueryIsBlank_showsLatestPosts() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts").param("q", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("q", ""))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("まだ投稿はありません")));

        verify(postService).latest();
        verify(postService, never()).searchByBody(anyString());
    }

    @Test
    @DisplayName("投稿検索_0件の場合_まだ投稿はありませんを表示する")
    void list_whenSearchResultIsEmpty_showsEmptyMessage() throws Exception {
        given(postService.searchByBody("missing")).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts").param("q", "missing"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_GET_postsスラッシュへリクエストできる")
    void list_whenRefreshButtonShown_requestsPostsSlash() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("更新")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("method=\"get\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/posts/\"")));

        mockMvc.perform(get("/posts/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"));
    }

    @Test
    @DisplayName("投稿一覧_投稿者内容投稿日の順に表示する")
    void list_whenPostsExist_rendersAuthorBodyCreatedAtInOrder() throws Exception {
        given(postService.latest()).willReturn(List.of(
                new PostView(new Post(1L, "alice", "hello", Instant.parse("2026-05-23T10:00:00Z")), 0)));

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("alice", "hello", "2026-05-23 19:00");
        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("hello"));
        assertThat(html.indexOf("hello")).isLessThan(html.indexOf("2026-05-23 19:00"));
    }

    @Test
    @DisplayName("投稿一覧_投稿があるとき_いいね数を表示する")
    void list_whenPostsExist_showsLikeCount() throws Exception {
        given(postService.latest()).willReturn(List.of(
                new PostView(new Post(1L, "alice", "hello", Instant.parse("2026-05-23T10:00:00Z")), 3)));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("いいね 3")));
    }

    @Test
    @DisplayName("新規投稿フォーム_GET_posts_new_posts_formを返しpostFormを渡す")
    void newForm_whenRequested_returnsFormViewWithPostForm() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)));
    }

    @Test
    @DisplayName("投稿作成_投稿者と本文が空白のみ_posts_formを再表示しエラーを表示する")
    void create_whenAuthorAndBodyAreBlank_redisplaysFormWithErrors() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author", "body"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿者名を入力してください")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文を入力してください")));
    }

    @Test
    @DisplayName("投稿作成_入力が妥当なとき_postsへリダイレクトする")
    void create_whenValid_redirectsToPosts() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "hello"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/posts"));

        verify(postService).create(eq("alice"), eq("hello"));
    }

    @Test
    @DisplayName("投稿作成_投稿者と本文が最大文字数のとき_postsへリダイレクトし登録できる")
    void create_whenAuthorAndBodyAreMaxLength_redirectsToPosts() throws Exception {
        String author = "a".repeat(30);
        String body = "b".repeat(280);

        mockMvc.perform(post("/posts")
                        .param("author", author)
                        .param("body", body))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/posts"));

        verify(postService).create(eq(author), eq(body));
    }

    @Test
    @DisplayName("投稿一覧_投稿ボタン_GET_posts_newへリクエストできる")
    void list_whenPostButtonShown_requestsPostsNew() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("method=\"get\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/posts/new\"")));

        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"));
    }

    @Test
    @DisplayName("投稿詳細_存在するID_posts_detailを表示する")
    void detail_whenPostExists_returnsDetailView() throws Exception {
        PostView post = new PostView(new Post(1L, "alice", "hello", Instant.parse("2026-05-23T10:00:00Z")), 0);
        given(postService.findById(eq(1L), anyString())).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post));
    }

    @Test
    @DisplayName("投稿詳細_存在しないID_404を返す")
    void detail_whenPostDoesNotExist_returnsNotFound() throws Exception {
        given(postService.findById(eq(999L), anyString())).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿一覧_article押下時_投稿詳細へ遷移できる")
    void list_whenArticleClicked_requestsPostDetail() throws Exception {
        given(postService.latest()).willReturn(List.of(
                new PostView(new Post(1L, "alice", "hello", Instant.parse("2026-05-23T10:00:00Z")), 0)));
        given(postService.findById(eq(1L), anyString())).willReturn(Optional.of(
                new PostView(new Post(1L, "alice", "hello", Instant.parse("2026-05-23T10:00:00Z")), 0)));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "<a class=\"post__link\" href=\"/posts/1\">")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "<article class=\"post\">")));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"));
    }

    @Test
    @DisplayName("投稿詳細_対象IDの投稿_投稿内容を表示する")
    void detail_whenPostExists_showsTargetPostContent() throws Exception {
        given(postService.findById(eq(2L), anyString())).willReturn(Optional.of(
                new PostView(new Post(2L, "bob", "対象IDの本文", Instant.parse("2026-05-24T10:00:00Z")), 0)));

        mockMvc.perform(get("/posts/2"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("bob")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("対象IDの本文")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2026-05-24 19:00")));
    }

    @Test
    @DisplayName("投稿詳細_投稿があるとき_Likeボタンといいね数を表示する")
    void detail_whenPostExists_showsLikeButtonAndLikeCount() throws Exception {
        given(postService.findById(eq(1L), anyString())).willReturn(Optional.of(
                new PostView(new Post(1L, "alice", "hello", Instant.parse("2026-05-23T10:00:00Z")), 2)));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("いいね 2")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/posts/1/likes\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("method=\"post\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Like")));
    }

    @Test
    @DisplayName("投稿詳細_いいね済みの場合_Liked解除ボタンを強調表示する")
    void detail_whenAlreadyLiked_showsLikedCancelButton() throws Exception {
        given(postService.findById(eq(1L), anyString())).willReturn(Optional.of(
                new PostView(new Post(1L, "alice", "hello", Instant.parse("2026-05-23T10:00:00Z")), 2, true)));

        mockMvc.perform(get("/posts/1")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.1");
                            return request;
                        })
                        .header("User-Agent", "JUnit UA"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Liked（解除）")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("like-button--liked")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("aria-pressed=\"true\"")));

        verify(postService).findById(eq(1L), eq(sha256Prefix8("192.0.2.1|JUnit UA")));
    }

    @Test
    @DisplayName("いいね_POST_存在しないID_404を返す")
    void like_whenPostDoesNotExist_returnsNotFound() throws Exception {
        given(postService.toggleLike(eq(999L), anyString())).willReturn(Optional.empty());

        mockMvc.perform(post("/posts/999/likes")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.1");
                            return request;
                        })
                        .header("User-Agent", "JUnit UA"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("いいね_POST_存在するID_詳細画面へリダイレクトする")
    void like_whenPostExists_redirectsToDetailView() throws Exception {
        PostView post = new PostView(new Post(1L, "alice", "hello", Instant.parse("2026-05-23T10:00:00Z")), 1);
        given(postService.toggleLike(eq(1L), anyString())).willReturn(Optional.of(post));

        mockMvc.perform(post("/posts/1/likes")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.1");
                            return request;
                        })
                        .header("User-Agent", "JUnit UA"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/posts/1"));

        verify(postService).toggleLike(eq(1L), eq(sha256Prefix8("192.0.2.1|JUnit UA")));
    }

    private static String sha256Prefix8(String value) throws NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (byte oneByte : digest) {
            builder.append(String.format("%02x", oneByte));
        }
        return builder.substring(0, 8);
    }
}
