package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void list_empty_rendersEmptyMessage() throws Exception {
        given(postService.latest()).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of()))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsスラッシュへGETリクエストする")
    void list_refreshButton_requestsPostsSlash() throws Exception {
        given(postService.latest()).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/posts/\"")))
                .andExpect(content().string(containsString("method=\"get\"")))
                .andExpect(content().string(containsString("<button type=\"submit\">更新</button>")));
    }

    @Test
    @DisplayName("投稿一覧_投稿は投稿者内容投稿日の順に表示する")
    void list_postFields_renderAuthorBodyCreatedAtInOrder() throws Exception {
        given(postService.latest()).willReturn(List.of(new Post(
                "tanaka",
                "表示順を確認する本文です",
                LocalDateTime.parse("2026-05-23T09:00:00"))));

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html.indexOf("tanaka")).isLessThan(html.indexOf("表示順を確認する本文です"));
        assertThat(html.indexOf("表示順を確認する本文です")).isLessThan(html.indexOf("2026-05-23 09:00"));
    }

    @Test
    @DisplayName("新規投稿フォーム_GET_posts_new_postFormを積んでformビューを返す")
    void newForm_addsPostFormAndRendersFormView() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)))
                .andExpect(model().attributeHasNoErrors("postForm"));
    }

    @Test
    @DisplayName("新規投稿フォーム_初期表示_空入力でもエラーなしで表示する")
    void newForm_initialDisplay_rendersEmptyFormWithoutValidationErrors() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"author\"")))
                .andExpect(content().string(containsString("name=\"body\"")))
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(containsString("action=\"/posts\"")))
                .andExpect(content().string(not(containsString("投稿者名を入力してください"))))
                .andExpect(content().string(not(containsString("本文を入力してください"))));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "1234567890123456789012345678901" })
    @DisplayName("投稿作成_authorが空文字または30文字超過_バリデーションエラーでフォームを再表示する")
    void create_invalidAuthor_rendersFormWithValidationError(String author) throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", author)
                        .param("body", "本文です"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者")));

        verify(postService, never()).create(author, "本文です");
    }

    @ParameterizedTest
    @MethodSource("invalidBodies")
    @DisplayName("投稿作成_bodyが空文字または280文字超過_バリデーションエラーでフォームを再表示する")
    void create_invalidBody_rendersFormWithValidationError(String body) throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "tanaka")
                        .param("body", body))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文")));

        verify(postService, never()).create("tanaka", body);
    }

    private static Stream<String> invalidBodies() {
        return Stream.of("", "a".repeat(281));
    }

    @Test
    @DisplayName("投稿作成_authorとbodyが空白のみ_バリデーションエラーでフォームを再表示する")
    void create_blankOnlyValues_rendersFormWithValidationErrors() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author", "body"))
                .andExpect(content().string(containsString("投稿者名を入力してください")))
                .andExpect(content().string(containsString("本文を入力してください")));

        verify(postService, never()).create("   ", "   ");
    }

    @Test
    @DisplayName("投稿作成_正常入力_投稿を登録してpostsへリダイレクトする")
    void create_validInput_createsPostAndRedirectsToPosts() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "tanaka")
                        .param("body", "投稿本文です"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("tanaka", "投稿本文です");
    }

    @Test
    @DisplayName("投稿詳細_存在するID_posts_detailビューを表示しmodelに投稿を積む")
    void detail_existingId_rendersDetailViewWithPostModel() throws Exception {
        Post post = new Post(
                "tanaka",
                "詳細画面に表示する本文です",
                LocalDateTime.parse("2026-05-23T09:00:00"));
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(postService.countLikes(1L)).willReturn(3L);

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(model().attribute("postId", 1L))
                .andExpect(model().attribute("likeCount", 3L))
                .andExpect(content().string(containsString("tanaka")))
                .andExpect(content().string(containsString("詳細画面に表示する本文です")))
                .andExpect(content().string(containsString("2026-05-23 09:00")))
                .andExpect(content().string(containsString("いいね 3")))
                .andExpect(content().string(containsString("action=\"/posts/1/likes\"")))
                .andExpect(content().string(containsString("method=\"post\"")));
    }

    @Test
    @DisplayName("投稿詳細_存在しないID_404を返す")
    void detail_missingId_returnsNotFound() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("いいね_POST_リクエスト情報からclientHashを作りトグルして詳細へ戻る")
    void like_validPost_togglesLikeAndRedirectsToDetail() throws Exception {
        given(postService.findById(1L)).willReturn(Optional.of(new Post(
                "tanaka",
                "本文",
                LocalDateTime.parse("2026-05-23T09:00:00"))));

        mockMvc.perform(post("/posts/1/likes")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.10");
                            return request;
                        })
                        .header("User-Agent", "MockBrowser/1.0"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).toggleLike(1L, "17a8e6e2");
    }

    @Test
    @DisplayName("いいね_POST_存在しない投稿ID_404を返す")
    void like_missingPost_returnsNotFound() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(post("/posts/999/likes"))
                .andExpect(status().isNotFound());

        verify(postService, never()).toggleLike(eq(999L), anyString());
    }
}
