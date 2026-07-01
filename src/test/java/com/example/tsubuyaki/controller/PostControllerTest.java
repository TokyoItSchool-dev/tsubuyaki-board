package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostControllerTest {

    private TimeZone defaultTimeZone;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @BeforeEach
    void setUpTimeZone() {
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
    }

    @AfterEach
    void tearDownTimeZone() {
        TimeZone.setDefault(defaultTimeZone);
    }

    @Test
    @DisplayName("投稿一覧_投稿0件_空メッセージを表示する")
    void 投稿一覧_投稿0件_空メッセージを表示する() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", Collections.emptyList()))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_qあり_検索結果をposts_listへ渡す")
    void 投稿一覧_qあり_検索結果をpostsListへ渡す() throws Exception {
        Post post = new Post("alice", "hello world", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.list("hello")).willReturn(List.of(post));

        mockMvc.perform(get("/posts").param("q", "hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of(post)));
    }

    @Test
    @DisplayName("投稿一覧_qあり_qをmodelに保持する")
    void 投稿一覧_qあり_qをmodelに保持する() throws Exception {
        given(postService.list("hello")).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts").param("q", "hello"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("q", "hello"));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsスラッシュへGETリクエストする")
    void 投稿一覧_更新ボタン_押すとpostsスラッシュへGetリクエストする() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<form class=\"refresh-form\" action=\"/posts/\" method=\"get\">")))
                .andExpect(content().string(containsString("<button type=\"submit\">更新</button>")));
    }

    @Test
    @DisplayName("投稿一覧_検索フォーム_GET_postsへqを送信できる")
    void 投稿一覧_検索フォーム_GetPostsへqを送信できる() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("(?s).*<form class=\"search-form\" action=\"/posts\" method=\"get\">\\s*"
                        + "<input[^>]+type=\"search\"[^>]+name=\"q\"[^>]*>\\s*"
                        + "<button type=\"submit\">検索</button>\\s*</form>.*")));
    }

    @Test
    @DisplayName("投稿一覧_検索後_入力欄にqを保持する")
    void 投稿一覧_検索後_入力欄にqを保持する() throws Exception {
        given(postService.list("hello")).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts").param("q", "hello"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("(?s).*<input[^>]+type=\"search\"[^>]+name=\"q\""
                        + "[^>]+value=\"hello\"[^>]*>.*")));
    }

    @Test
    @DisplayName("投稿一覧_投稿あり_投稿者内容投稿日の順に表示する")
    void 投稿一覧_投稿あり_投稿者内容投稿日の順に表示する() throws Exception {
        Post post = new Post(
                "alice",
                "長い本文でも読みやすく折り返して表示する投稿です。",
                Instant.parse("2026-05-23T10:15:00Z"));
        ReflectionTestUtils.setField(post, "id", 1L);
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("(?s).*<article class=\"post\">\\s*"
                        + "<div class=\"post__author\">.*</div>\\s*"
                        + "<p class=\"post__body\">長い本文でも読みやすく折り返して表示する投稿です。</p>\\s*"
                        + "<div class=\"post__meta\">\\s*"
                        + "<time class=\"post__created-at\".*>2026-05-23 19:15</time>\\s*"
                        + "<a href=\"/posts/1\">詳細</a>\\s*</div>.*")));
    }

    @Test
    @DisplayName("投稿一覧_投稿あり_投稿者名の左に頭文字アバターを表示する")
    void 投稿一覧_投稿あり_投稿者名の左に頭文字アバターを表示する() throws Exception {
        Post post = new Post("alice", "hello", "#e91e63", Instant.parse("2026-05-23T10:15:00Z"));
        ReflectionTestUtils.setField(post, "id", 1L);
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("(?s).*<div class=\"post__author\">\\s*"
                        + "<span class=\"post__avatar\" style=\"background-color: #e91e63\">A</span>\\s*"
                        + "<span class=\"post__author-name\">alice</span>\\s*</div>.*")));
    }

    @Test
    @DisplayName("投稿一覧_投稿あり_各投稿に詳細リンクを表示する")
    void 投稿一覧_投稿あり_各投稿に詳細リンクを表示する() throws Exception {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:15:00Z"));
        ReflectionTestUtils.setField(post, "id", 1L);
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("(?s).*<article class=\"post\">.*"
                        + "<a[^>]+href=\"/posts/1\"[^>]*>詳細</a>.*</article>.*")));
    }

    @Test
    @DisplayName("投稿詳細_存在するid_posts_detailビューに投稿を渡す")
    void 投稿詳細_存在するid_posts_detailビューに投稿を渡す() throws Exception {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:15:00Z"));
        ReflectionTestUtils.setField(post, "id", 1L);
        given(postService.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(content().string(containsString("<h1>投稿詳細</h1>")))
                .andExpect(content().string(containsString("<a href=\"/posts\">一覧に戻る</a>")))
                .andExpect(content().string(matchesPattern("(?s).*<article class=\"post\">\\s*"
                        + "<div class=\"post__author\">.*</div>\\s*"
                        + "<p class=\"post__body\">hello</p>\\s*"
                        + ".*<time class=\"post__created-at\".*>2026-05-23 19:15</time>.*")));
    }

    @Test
    @DisplayName("投稿詳細_投稿者名の左に頭文字アバターを表示する")
    void 投稿詳細_投稿者名の左に頭文字アバターを表示する() throws Exception {
        Post post = new Post("kaana", "hello", "#3498db", Instant.parse("2026-05-23T10:15:00Z"));
        ReflectionTestUtils.setField(post, "id", 1L);
        given(postService.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("(?s).*<div class=\"post__author\">\\s*"
                        + "<span class=\"post__avatar\" style=\"background-color: #3498db\">K</span>\\s*"
                        + "<span class=\"post__author-name\">kaana</span>\\s*</div>.*")));
    }

    @Test
    @DisplayName("投稿詳細_存在するid_likeCountをmodelに渡す")
    void 投稿詳細_存在するid_likeCountをmodelに渡す() throws Exception {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:15:00Z"));
        ReflectionTestUtils.setField(post, "id", 1L);
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(postService.countLikes(1L)).willReturn(1312L);

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("likeCount", 1312L));
    }

    @Test
    @DisplayName("投稿詳細_いいねボタン_本文の下かつ作成日の上に表示する")
    void 投稿詳細_いいねボタン_本文の下かつ作成日の上に表示する() throws Exception {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:15:00Z"));
        ReflectionTestUtils.setField(post, "id", 1L);
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(postService.countLikes(1L)).willReturn(1312L);

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("(?s).*<p class=\"post__body\">hello</p>\\s*"
                        + "<form class=\"post__like-form\" action=\"/posts/1/likes\" method=\"post\">.*"
                        + "<button[^>]*class=\"post__like-button\"[^>]*>\\s*"
                        + "<span class=\"post__like-heart\"[^>]*>♥</span>\\s*"
                        + "<span class=\"post__like-count\">1312</span>\\s*"
                        + "<span class=\"post__like-label\">いいね</span>\\s*</button>\\s*</form>\\s*"
                        + "<time class=\"post__created-at\".*")));
    }

    @Test
    @DisplayName("投稿詳細_存在しないid_404を返す")
    void 投稿詳細_存在しないid_404を返す() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_空のフォームを表示する")
    void 投稿作成フォーム_GetPostsNew_空のフォームを表示する() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成フォーム_avatarColorを選択できる")
    void 投稿作成フォーム_avatarColorを選択できる() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("(?s).*<label for=\"avatarColor\">アバター色</label>\\s*"
                        + "<input[^>]+type=\"color\"[^>]+id=\"avatarColor\"[^>]+name=\"avatarColor\""
                        + "[^>]+value=\"#3498db\"[^>]*>.*")));
    }

    @ParameterizedTest
    @CsvSource({
        "false, #3498db, #3498db",
        "true, #e91e63, #e91e63"
    })
    @DisplayName("投稿登録_avatarColor指定有無_投稿を作成して一覧へリダイレクトする")
    void 投稿登録_avatarColor指定有無_投稿を作成して一覧へリダイレクトする(
            boolean includeAvatarColor, String avatarColor, String expectedColor) throws Exception {
        MockHttpServletRequestBuilder request = post("/posts")
                .param("author", "alice")
                .param("body", "hello");
        if (includeAvatarColor) {
            request.param("avatarColor", avatarColor);
        }

        mockMvc.perform(request)
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().create("alice", "hello", expectedColor);
    }

    @Test
    @DisplayName("投稿登録_postsスラッシュ_投稿を作成して一覧へリダイレクトする")
    void 投稿登録_postsスラッシュ_投稿を作成して一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts/")
                        .param("author", "alice")
                        .param("body", "hello"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().create("alice", "hello", "#3498db");
    }

    @Test
    @DisplayName("投稿登録_author空白のみ_フォームを再表示し投稿者名必須エラーを表示する")
    void 投稿登録_author空白のみ_フォームを再表示し投稿者名必須エラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿登録_author空文字_フォームを再表示し投稿者名必須エラーを表示する")
    void 投稿登録_author空文字_フォームを再表示し投稿者名必須エラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "")
                        .param("body", "hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿登録_author31文字_フォームを再表示し投稿者名文字数エラーを表示する")
    void 投稿登録_author31文字_フォームを再表示し投稿者名文字数エラーを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "a".repeat(31))
                        .param("body", "hello"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名は 30 文字以内で入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿登録_body空白のみ_フォームを再表示し本文必須エラーを表示する")
    void 投稿登録_body空白のみ_フォームを再表示し本文必須エラーを表示する() throws Exception {
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
    @DisplayName("投稿登録_body空文字_フォームを再表示し本文必須エラーを表示する")
    void 投稿登録_body空文字_フォームを再表示し本文必須エラーを表示する() throws Exception {
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
    @DisplayName("投稿登録_body281文字_フォームを再表示し本文文字数エラーを表示する")
    void 投稿登録_body281文字_フォームを再表示し本文文字数エラーを表示する() throws Exception {
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
    @DisplayName("いいね_POST_posts_id_likes_clientHashでトグルし詳細へリダイレクトする")
    void いいね_PostPostsIdLikes_clientHashでトグルし詳細へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts/1/likes")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.1");
                            return request;
                        })
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        then(postService).should().toggleLike(1L, "c44b2068");
    }

    @Test
    @DisplayName("いいねトグル_同一クライアント2回押下_同じclientHashで2回トグルする")
    void いいねトグル_同一クライアント2回押下_同じclientHashで2回トグルする() throws Exception {
        mockMvc.perform(post("/posts/1/likes")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.1");
                            return request;
                        })
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));
        mockMvc.perform(post("/posts/1/likes")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.1");
                            return request;
                        })
                        .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        then(postService).should(times(2)).toggleLike(1L, "c44b2068");
    }
}
