package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.config.SecurityConfig;
import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.ClientHashService;
import com.example.tsubuyaki.service.LikeService;
import com.example.tsubuyaki.service.PostNotFoundException;
import com.example.tsubuyaki.service.PostDetail;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import com.example.tsubuyaki.web.dto.PostFormOptions;
import com.example.tsubuyaki.web.dto.PostResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
@Import(SecurityConfig.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private LikeService likeService;

    @MockitoBean
    private ClientHashService clientHashService;

    @MockitoBean
    private PostFormOptions postFormOptions;

    @BeforeEach
    void setUp() {
        given(postFormOptions.avatarColors()).willReturn(List.of("RED", "BLUE", "GREEN", "YELLOW", "PURPLE", "ORANGE"));
    }

    @Test
    @DisplayName("投稿一覧_DB空のとき_まだ投稿はありませんを表示する")
    void 投稿一覧_DB空のとき_まだ投稿はありませんを表示する() throws Exception {
        given(postService.findPosts(null)).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of()))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_表示時_更新ボタンはpostsスラッシュへGETリクエストする")
    void 投稿一覧_表示時_更新ボタンはpostsスラッシュへGETリクエストする() throws Exception {
        given(postService.findPosts(null)).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("更新")))
                .andExpect(content().string(containsString("action=\"/posts/\"")))
                .andExpect(content().string(containsString("method=\"get\"")));
    }

    @Test
    @DisplayName("投稿一覧_投稿あり_投稿者内容投稿日の順に表示する")
    void 投稿一覧_投稿あり_投稿者内容投稿日の順に表示する() throws Exception {
        given(postService.findPosts(null)).willReturn(List.of(
                new Post(1L, "alice", "BLUE", "一覧の表示順を確認します #Java",
                        Instant.parse("2026-06-26T09:00:00Z"), List.of("Java"))));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        int authorIndex = html.indexOf("alice");
        int avatarColorIndex = html.indexOf("post__avatar-color--blue");
        int bodyIndex = html.indexOf("一覧の表示順を確認します");
        int createdAtIndex = html.indexOf("2026-06-26T09:00:00Z");

        assertThat(authorIndex).isGreaterThanOrEqualTo(0);
        assertThat(avatarColorIndex).isGreaterThanOrEqualTo(0);
        assertThat(html).doesNotContain(">BLUE<");
        assertThat(html).contains("post-link");
        assertThat(html).contains("href=\"/posts/1\"");
        assertThat(html).contains(">一覧の表示順を確認します</a>");
        assertThat(html).doesNotContain(">一覧の表示順を確認します #Java</a>");
        assertThat(html).contains("href=\"/tags/Java\"");
        assertThat(bodyIndex).isGreaterThan(authorIndex);
        assertThat(createdAtIndex).isGreaterThan(bodyIndex);
        assertThat(result.getModelAndView().getModel().get("posts"))
                .asList()
                .allSatisfy(post -> assertThat(post).isInstanceOf(PostResponse.class));
    }

    @Test
    @DisplayName("投稿検索_q指定_本文部分一致の検索結果を一覧画面に表示し検索文字列を保持する")
    void 投稿検索_q指定_本文部分一致の検索結果を一覧画面に表示し検索文字列を保持する() throws Exception {
        given(postService.findPosts("Spring")).willReturn(List.of(
                new Post("alice", "GREEN", "Spring Boot の共有です", Instant.parse("2026-06-26T09:00:00Z"))));

        MvcResult result = mockMvc.perform(get("/posts").param("q", "Spring"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("query", "Spring"))
                .andExpect(content().string(containsString("Spring Boot の共有です")))
                .andExpect(content().string(containsString("value=\"Spring\"")))
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .contains("post__avatar-color--green")
                .doesNotContain(">GREEN<");

        verify(postService).findPosts("Spring");
    }

    @Test
    @DisplayName("投稿検索_q空文字_通常一覧を表示し検索文字列は空で保持する")
    void 投稿検索_q空文字_通常一覧を表示し検索文字列は空で保持する() throws Exception {
        given(postService.findPosts("")).willReturn(List.of());

        mockMvc.perform(get("/posts").param("q", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of()))
                .andExpect(model().attribute("query", ""))
                .andExpect(content().string(containsString("value=\"\"")));

        verify(postService).findPosts("");
    }

    @Test
    @DisplayName("投稿検索_結果0件_一覧画面を正常表示し検索文字列を保持する")
    void 投稿検索_結果0件_一覧画面を正常表示し検索文字列を保持する() throws Exception {
        given(postService.findPosts("NoHit")).willReturn(List.of());

        mockMvc.perform(get("/posts").param("q", "NoHit"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of()))
                .andExpect(model().attribute("query", "NoHit"))
                .andExpect(content().string(containsString("まだ投稿はありません")))
                .andExpect(content().string(containsString("value=\"NoHit\"")));
    }

    @Test
    @DisplayName("投稿詳細_存在するid_投稿をModelに入れて詳細画面を表示する")
    void 投稿詳細_存在するid_投稿をModelに入れて詳細画面を表示する() throws Exception {
        Post post = new Post(1L, "alice", "PURPLE", "詳細表示を確認します #java #spring",
                Instant.parse("2026-06-26T09:00:00Z"), List.of("java", "spring"));
        given(postService.getDetail(1L)).willReturn(new PostDetail(post, 15L));

        MvcResult result = mockMvc.perform(get("/posts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", new PostResponse(
                        1L,
                        "alice",
                        "PURPLE",
                        "詳細表示を確認します #java #spring",
                        Instant.parse("2026-06-26T09:00:00Z"),
                        List.of("java", "spring"))))
                .andExpect(model().attribute("likeCount", 15L))
                .andExpect(content().string(containsString("♥ 15")))
                .andExpect(content().string(containsString("Like")))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        int likeIndex = html.indexOf("post__likes");
        int createdAtIndex = html.indexOf("2026-06-26T09:00:00Z");

        assertThat(html)
                .contains("post__avatar-color--purple")
                .contains(">詳細表示を確認します</p>")
                .contains("action=\"/posts/1/delete\"")
                .contains("method=\"post\"")
                .contains(">削除</button>")
                .doesNotContain(">詳細表示を確認します #java #spring</p>")
                .contains("href=\"/tags/java\"")
                .contains("href=\"/tags/spring\"")
                .doesNotContain(">PURPLE<");
        assertThat(createdAtIndex).isGreaterThan(likeIndex);
    }

    @Test
    @DisplayName("投稿詳細_存在しないid_404を返す")
    void 投稿詳細_存在しないid_404を返す() throws Exception {
        given(postService.getDetail(999L)).willThrow(new PostNotFoundException(999L));

        mockMvc.perform(get("/posts/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿詳細_論理削除済み投稿_404を返す")
    void 投稿詳細_論理削除済み投稿_404を返す() throws Exception {
        given(postService.getDetail(10L)).willThrow(new PostNotFoundException(10L));

        mockMvc.perform(get("/posts/{id}", 10L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("新規投稿フォーム_GET_posts_new_空の投稿フォームを表示する")
    void 新規投稿フォーム_GET_posts_new_空の投稿フォームを表示する() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)))
                .andExpect(model().attributeExists("avatarColors"))
                .andExpect(content().string(containsString("avatarColor")))
                .andExpect(content().string(containsString("BLUE")))
                .andExpect(content().string(containsString("RED")))
                .andExpect(content().string(containsString("PURPLE")));
    }

    @Test
    @DisplayName("投稿作成_正常入力_投稿を保存して一覧へリダイレクトする")
    void 投稿作成_正常入力_投稿を保存して一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .with(csrf())
                        .param("author", "alice")
                        .param("avatarColor", "ORANGE")
                        .param("body", "今日の共有です"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "ORANGE", "今日の共有です");
    }

    @Test
    @DisplayName("投稿作成_CSRFトークンなし_403を返し保存しない")
    void 投稿作成_CSRFトークンなし_403を返し保存しない() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "今日の共有です"))
                .andExpect(status().isForbidden());

        verify(postService, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("いいね_POST_posts_id_likes_clientHashでトグルして詳細へリダイレクトする")
    void いいね_POST_posts_id_likes_clientHashでトグルして詳細へリダイレクトする() throws Exception {
        String ip = "192.0.2.10";
        String userAgent = "JUnit Browser";
        given(clientHashService.generate(ip, userAgent)).willReturn("abc12345");

        mockMvc.perform(post("/posts/{id}/likes", 1L)
                        .with(csrf())
                        .with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        })
                        .header("User-Agent", userAgent))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        verify(clientHashService).generate(ip, userAgent);
        verify(likeService).toggleLike(1L, "abc12345");
    }

    @Test
    @DisplayName("投稿削除_POST_posts_id_delete_論理削除して一覧へリダイレクトする")
    void 投稿削除_POST_posts_id_delete_論理削除して一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts/{id}/delete", 1L).with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).delete(1L);
    }

    @Test
    @DisplayName("投稿作成_author未入力_フォームを再表示してエラー情報を含める")
    void 投稿作成_author未入力_フォームを再表示してエラー情報を含める() throws Exception {
        assertInvalidPost("", "本文があります", "author");
    }

    @Test
    @DisplayName("投稿作成_body未入力_フォームを再表示してエラー情報を含める")
    void 投稿作成_body未入力_フォームを再表示してエラー情報を含める() throws Exception {
        assertInvalidPost("alice", "", "body");
    }

    @Test
    @DisplayName("投稿作成_author31文字以上_フォームを再表示してエラー情報を含める")
    void 投稿作成_author31文字以上_フォームを再表示してエラー情報を含める() throws Exception {
        assertInvalidPost("a".repeat(31), "本文があります", "author");
    }

    @Test
    @DisplayName("投稿作成_body281文字以上_フォームを再表示してエラー情報を含める")
    void 投稿作成_body281文字以上_フォームを再表示してエラー情報を含める() throws Exception {
        assertInvalidPost("alice", "あ".repeat(281), "body");
    }

    @Test
    @DisplayName("投稿作成_空白のみ_フォームを再表示してエラー情報を含める")
    void 投稿作成_空白のみ_フォームを再表示してエラー情報を含める() throws Exception {
        assertInvalidPost("   ", "　　", "author", "body");
    }

    private void assertInvalidPost(String author, String body, String... errorFields) throws Exception {
        mockMvc.perform(post("/posts")
                        .with(csrf())
                        .param("author", author)
                        .param("body", body))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", errorFields));

        verify(postService, never()).create(anyString(), anyString());
    }
}
