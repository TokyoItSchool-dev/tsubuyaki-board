package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.PostTimeFormatter;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
@Import(PostTimeFormatter.class)
class PostControllerTest {

    private static final ZoneId TEST_ZONE = ZoneId.of("Asia/Tokyo");

    private static final ZonedDateTime TEST_NOW = ZonedDateTime.of(2026, 5, 24, 15, 30, 0, 0, TEST_ZONE);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUpClock() {
        lenient().when(clock.instant()).thenReturn(TEST_NOW.toInstant());
        lenient().when(clock.getZone()).thenReturn(TEST_ZONE);
    }

    @Test
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void 投稿一覧_0件の場合_まだ投稿はありませんを表示する() throws Exception {
        given(postService.findLatestPosts()).willReturn(Collections.emptyList());

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", Collections.emptyList()))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("まだ投稿はありません");
    }

    @Test
    @DisplayName("投稿一覧_最新投稿があるとき_model_postsにListを積む")
    void 投稿一覧_最新投稿があるとき_model_postsにListを積む() throws Exception {
        List<Post> latestPosts = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z")),
                new Post("bob", "古い投稿", Instant.parse("2026-05-23T09:00:00Z"))
        );
        given(postService.findLatestPosts()).willReturn(latestPosts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", sameInstance(latestPosts)));

        then(postService).should().findLatestPosts();
    }

    @Test
    @DisplayName("投稿一覧_更新ボタンがあるとき_postsスラッシュへリクエストできる")
    void 投稿一覧_更新ボタンがあるとき_postsスラッシュへリクエストできる() throws Exception {
        given(postService.findLatestPosts()).willReturn(Collections.emptyList());

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("更新")
                .contains("action=\"/posts/\"")
                .contains("method=\"get\"");

        mockMvc.perform(get("/posts/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"));
    }

    @Test
    @DisplayName("投稿一覧_投稿があるとき_投稿者_内容_投稿日の順に表示する")
    void 投稿一覧_投稿があるとき_投稿者_内容_投稿日の順に表示する() throws Exception {
        given(postService.findLatestPosts()).willReturn(List.of(
                new Post("alice", "本文です", Instant.parse("2026-05-23T10:00:00Z"))
        ));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).containsSubsequence("alice", "本文です", "昨日");
    }

    @Test
    @DisplayName("投稿一覧_投稿があるとき_各投稿に詳細画面へのリンクを表示する")
    void 投稿一覧_投稿があるとき_各投稿に詳細画面へのリンクを表示する() throws Exception {
        given(postService.findLatestPosts()).willReturn(List.of(
                postWithId(1L),
                postWithId(2L)
        ));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("href=\"/posts/1\"")
                .contains("href=\"/posts/2\"");
    }

    @Test
    @DisplayName("投稿一覧_投稿カードにホバー時のスタイルを適用し詳細画面への遷移を維持する")
    void 投稿一覧_投稿カードにホバー時のスタイルを適用し詳細画面への遷移を維持する() throws Exception {
        given(postService.findLatestPosts()).willReturn(List.of(postWithId(1L)));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("class=\"post post--list\"")
                .contains("class=\"post__link\"")
                .contains("href=\"/posts/1\"");

        String css = Files.readString(Path.of("src/main/resources/static/css/app.css"));
        assertThat(css)
                .contains(".post--list:hover")
                .doesNotContain(".post:hover")
                .contains("box-shadow")
                .contains("transform")
                .contains("transition");
    }

    @Test
    @DisplayName("投稿一覧_投稿があるとき_avatarColorに応じたアバター色表示要素を表示する")
    void 投稿一覧_投稿があるとき_avatarColorに応じたアバター色表示要素を表示する() throws Exception {
        given(postService.findLatestPosts()).willReturn(List.of(
                postWithIdAndAvatarColor(1L, "purple")
        ));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("post__avatar")
                .contains("post__avatar--purple");
    }

    @Test
    @DisplayName("投稿一覧_投稿があるとき_各投稿の右下にハート形式のいいね数を表示する")
    void 投稿一覧_投稿があるとき_各投稿の右下にハート形式のいいね数を表示する() throws Exception {
        List<Post> posts = List.of(postWithId(1L), postWithId(2L));
        given(postService.findLatestPosts()).willReturn(posts);
        given(postService.countLikes(1L)).willReturn(3L);
        given(postService.countLikes(2L)).willReturn(0L);

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("likeCounts"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Map<Long, Long> likeCounts = (Map<Long, Long>) result.getModelAndView().getModel().get("likeCounts");
        assertThat(likeCounts).containsEntry(1L, 3L).containsEntry(2L, 0L);

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("post__list-like-count")
                .contains("post__like-heart\">♥")
                .contains("post__like-number\">3")
                .contains("post__like-number\">0")
                .doesNotContain("action=\"/posts/1/likes\"")
                .doesNotContain("action=\"/posts/2/likes\"");
    }

    @Test
    @DisplayName("投稿一覧_投稿カード下部に投稿日といいね数を左右同じ高さで表示し既存機能を維持する")
    void 投稿一覧_投稿カード下部に投稿日といいね数を左右同じ高さで表示し既存機能を維持する() throws Exception {
        List<Post> posts = List.of(postWithId(1L));
        given(postService.findLatestPosts()).willReturn(posts);
        given(postService.countLikes(1L)).willReturn(3L);

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", sameInstance(posts)))
                .andExpect(model().attributeExists("likeCounts"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .containsSubsequence(
                        "class=\"post__footer\"",
                        "class=\"post__footer-meta\"",
                        "class=\"post__created-at\"",
                        "昨日",
                        "class=\"post__footer-actions\"",
                        "class=\"post__list-like-count\"",
                        "post__like-heart\">♥",
                        "post__like-number\">3")
                .contains("href=\"/posts/1\"")
                .doesNotContain("action=\"/posts/1/likes\"");

        String css = Files.readString(Path.of("src/main/resources/static/css/app.css"));
        assertThat(css)
                .contains(".post__footer")
                .contains("justify-content: space-between")
                .contains("align-items: center")
                .contains(".post__footer-actions")
                .contains("display: flex");
    }

    @Test
    @DisplayName("投稿一覧_投稿日時をTwitter風のルールで表示する")
    void 投稿一覧_投稿日時をTwitter風のルールで表示する() throws Exception {
        ZonedDateTime olderPostTime = TEST_NOW.minusDays(2).withHour(9).withMinute(5);
        List<Post> posts = List.of(
                postWithIdAndCreatedAt(1L, TEST_NOW.minusSeconds(30).toInstant()),
                postWithIdAndCreatedAt(2L, TEST_NOW.minusMinutes(5).toInstant()),
                postWithIdAndCreatedAt(3L, TEST_NOW.minusHours(2).toInstant()),
                postWithIdAndCreatedAt(4L, TEST_NOW.minusDays(1).toInstant()),
                postWithIdAndCreatedAt(5L, olderPostTime.toInstant())
        );
        given(postService.findLatestPosts()).willReturn(posts);

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("たった今")
                .contains("5分前")
                .contains("2時間前")
                .contains("昨日")
                .contains(olderPostTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
    }

    @Test
    @DisplayName("投稿一覧_投稿をクリックした場合_GET_posts_idで詳細ビューを表示する")
    void 投稿一覧_投稿をクリックした場合_GET_posts_idで詳細ビューを表示する() throws Exception {
        Post post = postWithId(1L);
        given(postService.findPost(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", sameInstance(post)));
    }

    @Test
    @DisplayName("投稿検索_キーワードを指定した場合_検索結果のみ一覧に表示する")
    void 投稿検索_キーワードを指定した場合_検索結果のみ一覧に表示する() throws Exception {
        List<Post> searchResults = List.of(
                new Post("alice", "検索キーワードを含む投稿", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postService.searchPosts("検索キーワード")).willReturn(searchResults);

        MvcResult result = mockMvc.perform(get("/posts").param("q", "検索キーワード"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", sameInstance(searchResults)))
                .andExpect(model().attribute("keyword", "検索キーワード"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("検索キーワードを含む投稿")
                .doesNotContain("一致しない投稿");
        then(postService).should().searchPosts("検索キーワード");
    }

    @Test
    @DisplayName("投稿検索_キーワードを指定しない場合_従来どおり投稿一覧を表示する")
    void 投稿検索_キーワードを指定しない場合_従来どおり投稿一覧を表示する() throws Exception {
        List<Post> latestPosts = List.of(
                new Post("alice", "最新投稿", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postService.findLatestPosts()).willReturn(latestPosts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", sameInstance(latestPosts)))
                .andExpect(model().attribute("keyword", ""));

        then(postService).should().findLatestPosts();
    }

    @Test
    @DisplayName("投稿検索_一覧画面の上部に検索ボックスを表示する")
    void 投稿検索_一覧画面の上部に検索ボックスを表示する() throws Exception {
        given(postService.findLatestPosts()).willReturn(Collections.emptyList());

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("action=\"/posts\"")
                .contains("method=\"get\"")
                .contains("name=\"q\"")
                .contains("検索");
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_200を返しフォームビューを表示する")
    void 投稿作成フォーム_GET_posts_new_200を返しフォームビューを表示する() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"));
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_modelに空のpostFormを積む")
    void 投稿作成フォーム_GET_posts_new_modelに空のpostFormを積む() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("postForm"))
                .andReturn();

        Object postForm = result.getModelAndView().getModel().get("postForm");
        assertThat(postForm).isInstanceOf(PostForm.class);
        PostForm form = (PostForm) postForm;
        assertThat(form.getAuthor()).isNull();
        assertThat(form.getBody()).isNull();
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_アバター色の選択項目を表示する")
    void 投稿作成フォーム_GET_posts_new_アバター色の選択項目を表示する() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("avatarColors"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("select")
                .contains("name=\"avatarColor\"")
                .contains("赤")
                .contains("青")
                .contains("緑")
                .contains("黄")
                .contains("紫");
    }

    @Test
    @DisplayName("投稿詳細_存在するidの場合_詳細ビューを表示しmodelにpostを積む")
    void 投稿詳細_存在するidの場合_詳細ビューを表示しmodelにpostを積む() throws Exception {
        Post post = new Post("alice", "詳細本文", Instant.parse("2026-05-23T10:00:00Z"));
        given(postService.findPost(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", sameInstance(post)));
    }

    @Test
    @DisplayName("投稿詳細_存在するidの場合_投稿者_内容_投稿日_戻るリンクを表示する")
    void 投稿詳細_存在するidの場合_投稿者_内容_投稿日_戻るリンクを表示する() throws Exception {
        given(postService.findPost(1L)).willReturn(Optional.of(
                new Post("alice", "詳細本文", Instant.parse("2026-05-23T10:00:00Z"))
        ));

        MvcResult result = mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .containsSubsequence("alice", "詳細本文", "昨日")
                .contains("href=\"/posts\"");
    }

    @Test
    @DisplayName("投稿詳細_投稿日時をTwitter風のルールで表示する")
    void 投稿詳細_投稿日時をTwitter風のルールで表示する() throws Exception {
        given(postService.findPost(1L)).willReturn(Optional.of(
                new Post("alice", "詳細本文", TEST_NOW.minus(Duration.ofMinutes(5)).toInstant())
        ));

        MvcResult result = mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("5分前");
    }

    @Test
    @DisplayName("投稿詳細_いいねしていない場合_ハート付きLikeボタンと右側にいいね数を表示する")
    void 投稿詳細_いいねしていない場合_ハート付きLikeボタンと右側にいいね数を表示する() throws Exception {
        String remoteAddress = "203.0.113.10";
        String userAgent = "JUnit Browser";
        given(postService.findPost(1L)).willReturn(Optional.of(postWithId(1L)));
        given(postService.countLikes(1L)).willReturn(3L);
        given(postService.isLiked(1L, clientHash(remoteAddress, userAgent))).willReturn(false);

        MvcResult result = mockMvc.perform(get("/posts/1")
                        .with(request -> {
                            request.setRemoteAddr(remoteAddress);
                            return request;
                        })
                        .header("User-Agent", userAgent))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("likeCount", 3L))
                .andExpect(model().attribute("liked", false))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("action=\"/posts/1/likes\"")
                .contains("method=\"post\"")
                .contains("post__like-heart\">♡")
                .contains("post__like-number\">3");
    }

    @Test
    @DisplayName("投稿詳細_いいねしている場合_塗りつぶしハートと右側にいいね数を表示する")
    void 投稿詳細_いいねしている場合_塗りつぶしハートと右側にいいね数を表示する() throws Exception {
        String remoteAddress = "203.0.113.10";
        String userAgent = "JUnit Browser";
        given(postService.findPost(1L)).willReturn(Optional.of(postWithId(1L)));
        given(postService.countLikes(1L)).willReturn(3L);
        given(postService.isLiked(1L, clientHash(remoteAddress, userAgent))).willReturn(true);

        MvcResult result = mockMvc.perform(get("/posts/1")
                        .with(request -> {
                            request.setRemoteAddr(remoteAddress);
                            return request;
                        })
                        .header("User-Agent", userAgent))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("likeCount", 3L))
                .andExpect(model().attribute("liked", true))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("action=\"/posts/1/likes\"")
                .contains("method=\"post\"")
                .contains("post__like-heart\">♥")
                .contains("post__like-number\">3");
    }

    @Test
    @DisplayName("投稿詳細_存在するidの場合_アクションエリアの左側にいいね数_右側に削除ボタンを表示する")
    void 投稿詳細_存在するidの場合_アクションエリアの左側にいいね数_右側に削除ボタンを表示する() throws Exception {
        String remoteAddress = "203.0.113.10";
        String userAgent = "JUnit Browser";
        given(postService.findPost(1L)).willReturn(Optional.of(postWithId(1L)));
        given(postService.countLikes(1L)).willReturn(3L);
        given(postService.isLiked(1L, clientHash(remoteAddress, userAgent))).willReturn(true);

        MvcResult result = mockMvc.perform(get("/posts/1")
                        .with(request -> {
                            request.setRemoteAddr(remoteAddress);
                            return request;
                        })
                        .header("User-Agent", userAgent))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("post__actions")
                .contains("post__actions-like")
                .contains("post__actions-delete")
                .contains("post__delete-button")
                .containsSubsequence(
                        "post__actions-like",
                        "post__like-heart\">♥",
                        "post__like-number\">3",
                        "post__actions-delete",
                        "🗑 削除");
    }

    @Test
    @DisplayName("投稿詳細_存在するidの場合_左下に日時_右下にいいねボタンと削除ボタンを横並びで表示する")
    void 投稿詳細_存在するidの場合_左下に日時_右下にいいねボタンと削除ボタンを横並びで表示する() throws Exception {
        String remoteAddress = "203.0.113.10";
        String userAgent = "JUnit Browser";
        given(postService.findPost(1L)).willReturn(Optional.of(postWithId(1L)));
        given(postService.countLikes(1L)).willReturn(3L);
        given(postService.isLiked(1L, clientHash(remoteAddress, userAgent))).willReturn(true);

        MvcResult result = mockMvc.perform(get("/posts/1")
                        .with(request -> {
                            request.setRemoteAddr(remoteAddress);
                            return request;
                        })
                        .header("User-Agent", userAgent))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .containsSubsequence(
                        "post__actions",
                        "post__actions-meta",
                        "post__created-at",
                        "昨日",
                        "post__actions-buttons",
                        "post__actions-like",
                        "post__actions-delete")
                .contains("action=\"/posts/1/likes\"")
                .contains("action=\"/posts/1/delete\"");

        String css = Files.readString(Path.of("src/main/resources/static/css/app.css"));
        assertThat(css)
                .contains(".post__actions")
                .contains("justify-content: space-between")
                .contains("align-items: center")
                .contains(".post__actions-buttons")
                .contains("display: flex")
                .contains("gap:");
    }

    @Test
    @DisplayName("投稿詳細_存在するidの場合_avatarColorに応じたアバター色表示要素を表示する")
    void 投稿詳細_存在するidの場合_avatarColorに応じたアバター色表示要素を表示する() throws Exception {
        given(postService.findPost(1L)).willReturn(Optional.of(postWithIdAndAvatarColor(1L, "red")));

        MvcResult result = mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("post__avatar")
                .contains("post__avatar--red");
    }

    @Test
    @DisplayName("投稿詳細_存在するidの場合_削除ボタンを表示する")
    void 投稿詳細_存在するidの場合_削除ボタンを表示する() throws Exception {
        given(postService.findPost(1L)).willReturn(Optional.of(postWithId(1L)));

        MvcResult result = mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("action=\"/posts/1/delete\"")
                .contains("method=\"post\"")
                .contains(">🗑 削除<");
    }

    @Test
    @DisplayName("投稿詳細_存在しないidの場合_404を返す")
    void 投稿詳細_存在しないidの場合_404を返す() throws Exception {
        given(postService.findPost(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("いいね切り替え_POST_posts_id_likes_clientHashを渡して詳細へリダイレクトする")
    void いいね切り替え_POST_posts_id_likes_clientHashを渡して詳細へリダイレクトする() throws Exception {
        String remoteAddress = "203.0.113.10";
        String userAgent = "JUnit Browser";

        mockMvc.perform(post("/posts/1/likes")
                        .with(request -> {
                            request.setRemoteAddr(remoteAddress);
                            return request;
                        })
                        .header("User-Agent", userAgent))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        then(postService).should().toggleLike(1L, clientHash(remoteAddress, userAgent));
    }

    @Test
    @DisplayName("投稿削除_POST_posts_id_delete_削除後に投稿一覧へリダイレクトする")
    void 投稿削除_POST_posts_id_delete_削除後に投稿一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts/1/delete"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().deletePost(1L);
    }

    @Test
    @DisplayName("投稿登録_正常な入力の場合_投稿を登録しpostsへリダイレクトする")
    void 投稿登録_正常な入力の場合_投稿を登録しpostsへリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "本文です")
                        .param("avatarColor", "blue"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().createPost("alice", "本文です", "blue");
    }

    @Test
    @DisplayName("投稿登録_アバター色を選択した場合_選択した色で投稿を登録する")
    void 投稿登録_アバター色を選択した場合_選択した色で投稿を登録する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "本文です")
                        .param("avatarColor", "purple"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().createPost("alice", "本文です", "purple");
    }

    @Test
    @DisplayName("投稿登録_author未入力の場合_登録せずフォームを再表示する")
    void 投稿登録_author未入力の場合_登録せずフォームを再表示する() throws Exception {
        assertInvalidPost("", "本文です", "投稿者名を入力してください");
    }

    @Test
    @DisplayName("投稿登録_authorが31文字以上の場合_登録せずフォームを再表示する")
    void 投稿登録_authorが31文字以上の場合_登録せずフォームを再表示する() throws Exception {
        assertInvalidPost("a".repeat(31), "本文です", "投稿者名は 30 文字以内で入力してください");
    }

    @Test
    @DisplayName("投稿登録_body未入力の場合_登録せずフォームを再表示する")
    void 投稿登録_body未入力の場合_登録せずフォームを再表示する() throws Exception {
        assertInvalidPost("alice", "", "本文を入力してください");
    }

    @Test
    @DisplayName("投稿登録_bodyが281文字以上の場合_登録せずフォームを再表示する")
    void 投稿登録_bodyが281文字以上の場合_登録せずフォームを再表示する() throws Exception {
        assertInvalidPost("alice", "あ".repeat(281), "本文は 280 文字以内で入力してください");
    }

    @Test
    @DisplayName("投稿登録_authorが空白文字のみの場合_登録せずフォームを再表示する")
    void 投稿登録_authorが空白文字のみの場合_登録せずフォームを再表示する() throws Exception {
        assertInvalidPost("   ", "本文です", "投稿者名を入力してください");
    }

    @Test
    @DisplayName("投稿登録_bodyが空白文字のみの場合_登録せずフォームを再表示する")
    void 投稿登録_bodyが空白文字のみの場合_登録せずフォームを再表示する() throws Exception {
        assertInvalidPost("alice", "   ", "本文を入力してください");
    }

    @Test
    @DisplayName("投稿登録_バリデーション失敗時_エラーメッセージを表示し入力内容を保持する")
    void 投稿登録_バリデーション失敗時_エラーメッセージを表示し入力内容を保持する() throws Exception {
        String invalidAuthor = "a".repeat(31);
        MvcResult result = mockMvc.perform(post("/posts")
                        .param("author", invalidAuthor)
                        .param("body", "保持する本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("投稿者名は 30 文字以内で入力してください")
                .contains("value=\"" + invalidAuthor + "\"")
                .contains(">保持する本文</textarea>");
        then(postService).should(never()).createPost(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    private void assertInvalidPost(String author, String body, String expectedMessage) throws Exception {
        MvcResult result = mockMvc.perform(post("/posts")
                        .param("author", author)
                        .param("body", body))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().hasErrors())
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains(expectedMessage)
                .contains("name=\"author\"")
                .contains("name=\"body\"");
        then(postService).should(never()).createPost(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    private String clientHash(String remoteAddress, String userAgent) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((remoteAddress + userAgent).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    private Post postWithId(Long id) {
        Post post = new Post("alice", "詳細本文", Instant.parse("2026-05-23T10:00:00Z"));
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    private Post postWithIdAndCreatedAt(Long id, Instant createdAt) {
        Post post = new Post("alice", "詳細本文", createdAt);
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    private Post postWithIdAndAvatarColor(Long id, String avatarColor) {
        Post post = new Post("alice", "詳細本文", avatarColor, Instant.parse("2026-05-23T10:00:00Z"));
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }
}
