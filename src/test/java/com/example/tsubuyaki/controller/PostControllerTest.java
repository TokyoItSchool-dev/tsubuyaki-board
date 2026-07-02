package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.ClientHashService;
import com.example.tsubuyaki.service.LikeToggleResult;
import com.example.tsubuyaki.service.PostDetail;
import com.example.tsubuyaki.service.PostRegistrationException;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.servlet.http.HttpServletRequest;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
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

    @MockitoBean
    private ClientHashService clientHashService;

    @Test
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void list_whenNoPosts_showsEmptyMessage() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", hasSize(0)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_表示するとき_modelに投稿Listを積む")
    void list_setsPostsListToModel() throws Exception {
        List<Post> posts = List.of(
                new Post("alice", "本文1", LocalDateTime.parse("2026-05-23T10:00:00")),
                new Post("bob", "本文2", LocalDateTime.parse("2026-05-23T09:00:00")));
        given(postService.latest()).willReturn(posts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", posts));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsへGETリクエストする")
    void list_hasRefreshButtonRequestingPosts() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("method=\"get\"", "action=\"/posts\"", "更新");
    }

    @Test
    @DisplayName("投稿一覧_投稿表示_投稿者内容投稿日の順に表示する")
    void list_displaysAuthorBodyCreatedAtInOrder() throws Exception {
        given(postService.latest()).willReturn(List.of(
                new Post("alice", "順序確認の本文", LocalDateTime.parse("2026-05-23T10:30:00"))));

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).containsSubsequence("alice", "順序確認の本文", "2026-05-23 10:30");
    }

    @Test
    @DisplayName("投稿一覧_投稿ブロック_詳細画面へのリンクを表示する")
    void list_displaysPostBlockLinkToDetail() throws Exception {
        Post post = new Post("alice", "リンク確認の本文", LocalDateTime.parse("2026-05-23T10:30:00"));
        ReflectionTestUtils.setField(post, "id", 42L);
        given(postService.latest()).willReturn(List.of(post));

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).containsSubsequence("href=\"/posts/42\"", "alice", "リンク確認の本文");
    }

    @Test
    @DisplayName("投稿詳細_存在するidの場合_posts_detailビューとpostを返す")
    void detail_whenPostExists_returnsDetailViewWithPost() throws Exception {
        Post post = new Post("alice", "詳細本文", LocalDateTime.parse("2026-05-23T10:30:00"));
        given(clientHashService.from(any(HttpServletRequest.class))).willReturn("a1b2c3d4");
        given(postService.findDetail(1L, "a1b2c3d4")).willReturn(Optional.of(new PostDetail(post, 2L, false)));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(model().attribute("likeCount", 2L))
                .andExpect(model().attribute("liked", false));

        then(postService).should().findDetail(1L, "a1b2c3d4");
    }

    @Test
    @DisplayName("投稿詳細_存在するidの場合_投稿者本文投稿日を表示する")
    void detail_whenPostExists_displaysAuthorBodyCreatedAt() throws Exception {
        given(clientHashService.from(any(HttpServletRequest.class))).willReturn("a1b2c3d4");
        given(postService.findDetail(1L, "a1b2c3d4")).willReturn(Optional.of(new PostDetail(
                new Post("alice", "詳細本文", LocalDateTime.parse("2026-05-23T10:30:00")), 2L, false)));

        String html = mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).containsSubsequence("alice", "詳細本文", "2026-05-23 10:30");
    }

    @Test
    @DisplayName("投稿詳細_存在しないidの場合_404エラー画面を表示する")
    void detail_whenPostDoesNotExist_returnsNotFoundErrorView() throws Exception {
        given(clientHashService.from(any(HttpServletRequest.class))).willReturn("a1b2c3d4");
        given(postService.findDetail(999L, "a1b2c3d4")).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿が見つかりません")));

        then(postService).should().findDetail(999L, "a1b2c3d4");
    }

    @Test
    @DisplayName("投稿詳細_いいね情報_いいね数とLikeボタンを表示する")
    void detail_displaysLikeCountAndLikeButton() throws Exception {
        given(clientHashService.from(any(HttpServletRequest.class))).willReturn("a1b2c3d4");
        given(postService.findDetail(1L, "a1b2c3d4")).willReturn(Optional.of(new PostDetail(
                new Post("alice", "詳細本文", LocalDateTime.parse("2026-05-23T10:30:00")), 2L, false)));

        String html = mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains(
                "いいね 2",
                "action=\"/posts/1/likes\"",
                "method=\"post\"",
                "いいね");
    }

    @Test
    @DisplayName("投稿詳細_いいね済みの場合_いいね解除ボタンを表示する")
    void detail_whenLiked_displaysUnlikeButton() throws Exception {
        given(clientHashService.from(any(HttpServletRequest.class))).willReturn("a1b2c3d4");
        given(postService.findDetail(1L, "a1b2c3d4")).willReturn(Optional.of(new PostDetail(
                new Post("alice", "詳細本文", LocalDateTime.parse("2026-05-23T10:30:00")), 2L, true)));

        String html = mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("いいね解除");
    }

    @Test
    @DisplayName("いいね_POST_posts_id_likes_サービスでトグルして詳細へリダイレクトする")
    void like_whenPostExists_togglesLikeAndRedirectsToDetail() throws Exception {
        given(clientHashService.from(any(HttpServletRequest.class))).willReturn("a1b2c3d4");
        given(postService.toggleLike(1L, "a1b2c3d4")).willReturn(Optional.of(new LikeToggleResult(true)));

        mockMvc.perform(post("/posts/1/likes")
                        .header("User-Agent", "Mozilla/5.0")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        then(clientHashService).should().from(any(HttpServletRequest.class));
        then(postService).should().toggleLike(1L, "a1b2c3d4");
    }

    @Test
    @DisplayName("いいね_POST_posts_id_likes_存在しない投稿の場合_404エラー画面を表示する")
    void like_whenPostDoesNotExist_returnsNotFoundErrorView() throws Exception {
        given(clientHashService.from(any(HttpServletRequest.class))).willReturn("a1b2c3d4");
        given(postService.toggleLike(999L, "a1b2c3d4")).willReturn(Optional.empty());

        mockMvc.perform(post("/posts/999/likes"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿が見つかりません")));

        then(postService).should().toggleLike(999L, "a1b2c3d4");
    }

    @Test
    @DisplayName("投稿作成画面_GET_posts_new_posts_formビューとpostFormを返す")
    void newForm_rendersFormViewWithPostForm() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)));
    }

    @Test
    @DisplayName("投稿作成画面_GET_posts_new_投稿フォーム項目を表示する")
    void newForm_displaysPostFormFields() throws Exception {
        String html = mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains(
                "action=\"/posts\"",
                "method=\"post\"",
                "name=\"author\"",
                "name=\"body\"");
    }

    @Test
    @DisplayName("投稿登録_authorが空の場合_posts_formを再表示しエラーを表示する")
    void create_whenAuthorIsEmpty_redisplaysFormWithError() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "")
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿者名を入力してください")));

        then(postService).should(never()).create(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("投稿登録_authorが31文字以上の場合_posts_formを再表示しエラーを表示する")
    void create_whenAuthorIsLongerThan30_redisplaysFormWithError() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "a".repeat(31))
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿者名は 30 文字以内で入力してください")));

        then(postService).should(never()).create(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("投稿登録_authorが空白文字のみの場合_posts_formを再表示しエラーを表示する")
    void create_whenAuthorIsBlank_redisplaysFormWithError() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿者名を入力してください")));

        then(postService).should(never()).create(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("投稿登録_bodyが空の場合_posts_formを再表示しエラーを表示する")
    void create_whenBodyIsEmpty_redisplaysFormWithError() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文を入力してください")));

        then(postService).should(never()).create(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("投稿登録_bodyが281文字以上の場合_posts_formを再表示しエラーを表示する")
    void create_whenBodyIsLongerThan280_redisplaysFormWithError() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "あ".repeat(281)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文は 280 文字以内で入力してください")));

        then(postService).should(never()).create(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("投稿登録_bodyが空白文字のみの場合_posts_formを再表示しエラーを表示する")
    void create_whenBodyIsBlank_redisplaysFormWithError() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文を入力してください")));

        then(postService).should(never()).create(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("投稿登録_投稿データの登録に失敗した場合_posts_formを再表示しエラーを表示する")
    void create_whenRegistrationFails_redisplaysFormWithError() throws Exception {
        willThrow(new PostRegistrationException("投稿の登録に失敗しました"))
                .given(postService).create("alice", "本文");

        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasErrors("postForm"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿の登録に失敗しました")));
    }

    @Test
    @DisplayName("投稿登録_投稿データの登録に成功した場合_postsへリダイレクトする")
    void create_whenRegistrationSucceeds_redirectsToPosts() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "本文"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().create("alice", "本文");
    }
}
