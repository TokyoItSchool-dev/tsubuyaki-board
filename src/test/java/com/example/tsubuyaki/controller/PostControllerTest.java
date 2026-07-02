package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostComment;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.PostTimeFormatter;
import com.example.tsubuyaki.web.dto.CommentForm;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.any;
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
        lenient().when(postService.findComments(anyLong())).thenReturn(Collections.emptyList());
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
    @DisplayName("投稿一覧_投稿件数があるとき_タイトル下に投稿数を表示する")
    void 投稿一覧_投稿件数があるとき_タイトル下に投稿数を表示する() throws Exception {
        given(postService.findLatestPosts()).willReturn(Collections.emptyList());
        given(postService.countActivePosts()).willReturn(3L);

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("postCount", 3L))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("class=\"post-count\"")
                .contains("投稿数：3件");
    }

    @Test
    @DisplayName("投稿一覧_ヘッダー_タイトルと投稿数を同じ行に表示しタブ風ナビを表示する")
    void 投稿一覧_ヘッダー_タイトルと投稿数を同じ行に表示しタブ風ナビを表示する() throws Exception {
        given(postService.findLatestPosts()).willReturn(Collections.emptyList());
        given(postService.countActivePosts()).willReturn(3L);

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .containsSubsequence(
                        "class=\"timeline-header__title-row\"",
                        "<h1>社内つぶやきボード</h1>",
                        "class=\"post-count\"",
                        "投稿数：3件")
                .contains("class=\"timeline-tabs\"")
                .contains("class=\"timeline-tabs__link timeline-tabs__link--active\"")
                .contains(">一覧</a>")
                .contains("class=\"timeline-tabs__link\"")
                .contains(">新規投稿</a>");

        String css = Files.readString(Path.of("src/main/resources/static/css/app.css"));
        assertThat(css)
                .contains(".timeline-header__title-row")
                .contains(".post-count")
                .contains(".timeline-tabs__link")
                .contains(".timeline-tabs__link--active")
                .contains(".timeline-tabs__link:not(.timeline-tabs__link--active):hover");
    }

    @Test
    @DisplayName("投稿一覧_ヘッダー_検索入力と検索ボタンと更新ボタンを横並びで表示する")
    void 投稿一覧_ヘッダー_検索入力と検索ボタンと更新ボタンを横並びで表示する() throws Exception {
        given(postService.findLatestPosts()).willReturn(Collections.emptyList());

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("class=\"timeline-search\"")
                .contains("class=\"post-search\"")
                .contains("method=\"get\"")
                .contains("class=\"post-search__field\"")
                .contains("class=\"post-search__icon\"")
                .contains(">🔍</span>")
                .contains("type=\"search\"")
                .contains("name=\"q\"")
                .contains("placeholder=\"キーワードを検索...\"")
                .contains("class=\"post-search__button\"")
                .contains("class=\"post-refresh\"")
                .contains("class=\"post-refresh__button\"")
                .contains("title=\"更新\"")
                .contains(">↺</button>");

        String css = Files.readString(Path.of("src/main/resources/static/css/app.css"));
        assertThat(css)
                .contains(".timeline-search")
                .contains(".post-search__field")
                .contains(".post-search__icon")
                .contains(".post-search__input")
                .contains(".post-search__button")
                .contains(".post-refresh__button")
                .contains("grid-template-columns")
                .contains("border-bottom");
    }

    @Test
    @DisplayName("投稿検索_キーワード検索時_検索語を入力欄に保持し検索中のみ検索結果を表示する")
    void 投稿検索_キーワード検索時_検索語を入力欄に保持し検索中のみ検索結果を表示する() throws Exception {
        List<Post> searchResults = List.of(
                new Post("alice", "Oracle本文1", Instant.parse("2026-05-23T10:00:00Z")),
                new Post("bob", "Oracle本文2", Instant.parse("2026-05-23T09:00:00Z"))
        );
        given(postService.searchPosts("Oracle")).willReturn(searchResults);

        MvcResult searchResult = mockMvc.perform(get("/posts").param("q", "Oracle"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("keyword", "Oracle"))
                .andExpect(model().attribute("searchResultCount", 2))
                .andReturn();

        String searchHtml = searchResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(searchHtml)
                .contains("value=\"Oracle\"")
                .contains("「<strong class=\"search-result__keyword\">Oracle</strong>」の検索結果")
                .contains(">2</span>件");

        given(postService.findLatestPosts()).willReturn(Collections.emptyList());

        MvcResult listResult = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeDoesNotExist("searchResultCount"))
                .andReturn();

        String listHtml = listResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(listHtml).doesNotContain("の検索結果：");

        then(postService).should().searchPosts("Oracle");
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
    @DisplayName("投稿一覧_ピンクの投稿があるとき_ピンクのアバター色表示要素を表示する")
    void 投稿一覧_ピンクの投稿があるとき_ピンクのアバター色表示要素を表示する() throws Exception {
        given(postService.findLatestPosts()).willReturn(List.of(
                postWithIdAndAvatarColor(1L, "pink")
        ));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("post__avatar")
                .contains("post__avatar--pink");
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
    @DisplayName("投稿一覧_投稿があるとき_いいね数の右側にコメント件数を表示する")
    void 投稿一覧_投稿があるとき_いいね数の右側にコメント件数を表示する() throws Exception {
        List<Post> posts = List.of(postWithId(1L), postWithId(2L));
        given(postService.findLatestPosts()).willReturn(posts);
        given(postService.countLikes(1L)).willReturn(3L);
        given(postService.countLikes(2L)).willReturn(0L);
        given(postService.countComments(1L)).willReturn(2L);
        given(postService.countComments(2L)).willReturn(0L);

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("commentCounts"))
                .andReturn();

        @SuppressWarnings("unchecked")
        Map<Long, Long> commentCounts = (Map<Long, Long>) result.getModelAndView().getModel().get("commentCounts");
        assertThat(commentCounts).containsEntry(1L, 2L).containsEntry(2L, 0L);

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .containsSubsequence(
                        "class=\"post__footer-actions\"",
                        "class=\"post__list-like-count\"",
                        "post__like-heart\">♥",
                        "post__like-number\">3",
                        "class=\"post__list-comment-count\"",
                        "post__comment-icon\">💬",
                        "post__comment-number\">2")
                .contains("post__comment-number\">0")
                .doesNotContain("コメント本文");
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
    @DisplayName("投稿検索_キーワード検索時_検索結果件数と投稿総数を表示する")
    void 投稿検索_キーワード検索時_検索結果件数と投稿総数を表示する() throws Exception {
        List<Post> searchResults = List.of(
                new Post("alice", "Oracle本文1", Instant.parse("2026-05-23T10:00:00Z")),
                new Post("bob", "Oracle本文2", Instant.parse("2026-05-23T09:00:00Z"))
        );
        given(postService.searchPosts("Oracle")).willReturn(searchResults);
        given(postService.countActivePosts()).willReturn(5L);

        MvcResult result = mockMvc.perform(get("/posts").param("q", "Oracle"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", sameInstance(searchResults)))
                .andExpect(model().attribute("keyword", "Oracle"))
                .andExpect(model().attribute("searchResultCount", 2))
                .andExpect(model().attribute("postCount", 5L))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("投稿数：5件")
                .contains("class=\"search-result\"")
                .contains("class=\"search-result__keyword\"")
                .contains(">Oracle</strong>")
                .contains("」の検索結果：<span")
                .contains(">2</span>件")
                .contains("Oracle本文1")
                .contains("Oracle本文2");
    }

    @Test
    @DisplayName("投稿検索_キーワードを指定しない場合_従来どおり投稿一覧を表示する")
    void 投稿検索_キーワードを指定しない場合_従来どおり投稿一覧を表示する() throws Exception {
        List<Post> latestPosts = List.of(
                new Post("alice", "最新投稿", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postService.findLatestPosts()).willReturn(latestPosts);
        given(postService.countActivePosts()).willReturn(5L);

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", sameInstance(latestPosts)))
                .andExpect(model().attribute("keyword", ""))
                .andExpect(model().attributeDoesNotExist("searchResultCount"))
                .andExpect(model().attribute("postCount", 5L))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("投稿数：5件")
                .doesNotContain("class=\"search-result\"")
                .doesNotContain("の検索結果：");

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
    @DisplayName("投稿作成フォーム_GET_posts_new_アバター色を色付き丸アイコンから選択できる")
    void 投稿作成フォーム_GET_posts_new_アバター色を色付き丸アイコンから選択できる() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("avatarColors"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("class=\"avatar-color-picker\"")
                .contains("class=\"avatar-color-picker__option\"")
                .contains("class=\"avatar-color-picker__input\"")
                .contains("class=\"avatar-color-picker__swatch avatar-color-picker__swatch--red\"")
                .contains("class=\"avatar-color-picker__swatch avatar-color-picker__swatch--blue\"")
                .contains("class=\"avatar-color-picker__swatch avatar-color-picker__swatch--green\"")
                .contains("class=\"avatar-color-picker__swatch avatar-color-picker__swatch--yellow\"")
                .contains("class=\"avatar-color-picker__swatch avatar-color-picker__swatch--purple\"")
                .contains("class=\"avatar-color-picker__swatch avatar-color-picker__swatch--pink\"")
                .contains("name=\"avatarColor\"")
                .contains("type=\"radio\"")
                .contains("value=\"red\"")
                .contains("value=\"blue\"")
                .contains("value=\"green\"")
                .contains("value=\"yellow\"")
                .contains("value=\"purple\"")
                .contains("value=\"pink\"")
                .contains("赤")
                .contains("青")
                .contains("緑")
                .contains("黄")
                .contains("紫")
                .contains("ピンク")
                .doesNotContain("<select");
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_入力項目を縦並びのカード内に表示し投稿ボタンを右下に配置する")
    void 投稿作成フォーム_GET_posts_new_入力項目を縦並びのカード内に表示し投稿ボタンを右下に配置する() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("class=\"post-form post-form--new\"")
                .containsSubsequence(
                        "class=\"post-form__field\"",
                        "for=\"author\"",
                        "name=\"author\"",
                        "class=\"post-form__field\"",
                        "class=\"post-form__label\"",
                        "アバター色",
                        "name=\"avatarColor\"",
                        "class=\"post-form__field\"",
                        "for=\"body\"",
                        "name=\"body\"",
                        "class=\"post-form__actions\"",
                        "type=\"submit\"",
                        ">投稿</button>");

        String css = Files.readString(Path.of("src/main/resources/static/css/app.css"));
        assertThat(css)
                .contains(".post-form")
                .contains(".post-form__field")
                .contains(".post-form__actions")
                .contains("max-width")
                .contains("flex-direction: column")
                .contains("justify-content: flex-end");
    }

    @Test
    @DisplayName("投稿作成フォーム_レイアウト_一覧画面と同じ幅の中央寄せカードと控えめな戻るリンクを表示する")
    void 投稿作成フォーム_レイアウト_一覧画面と同じ幅の中央寄せカードと控えめな戻るリンクを表示する() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm", "avatarColors"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("<main class=\"post-form-page\">")
                .contains("<h1>新規投稿</h1>")
                .contains("class=\"post-form-nav\"")
                .contains("class=\"post-form-nav__back\"")
                .contains("href=\"/posts\"")
                .contains(">一覧に戻る</a>")
                .contains("class=\"post-form post-form--new\"")
                .containsSubsequence(
                        "投稿者名",
                        "アバター色",
                        "本文",
                        "class=\"post-form__actions\"",
                        ">投稿</button>");

        String css = Files.readString(Path.of("src/main/resources/static/css/app.css"));
        assertThat(css)
                .contains(".post-form-page")
                .contains(".post-form-nav__back")
                .contains(".post-form--new")
                .contains("max-width: 720px")
                .contains("border-radius: 8px")
                .contains("box-shadow: 0 1px 3px rgb(15 23 42 / 6%)")
                .contains("justify-content: flex-end");
    }

    @Test
    @DisplayName("投稿作成フォーム_ヘッダー_戻るリンク_タイトル_区切り線_フォームカードの順に表示する")
    void 投稿作成フォーム_ヘッダー_戻るリンク_タイトル_区切り線_フォームカードの順に表示する() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .containsSubsequence(
                        "class=\"page-header page-header--form\"",
                        "class=\"post-form-nav\"",
                        ">一覧に戻る</a>",
                        "<h1>新規投稿</h1>",
                        "class=\"post-form post-form--new\"");

        String css = Files.readString(Path.of("src/main/resources/static/css/app.css"));
        assertThat(css)
                .contains(".page-header")
                .contains("border-bottom: 1px solid var(--color-border)")
                .contains(".page-header h1");
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
    @DisplayName("投稿詳細_レイアウト_投稿カードとコメント一覧とコメント投稿フォームを一覧画面と統一した幅で表示する")
    void 投稿詳細_レイアウト_投稿カードとコメント一覧とコメント投稿フォームを一覧画面と統一した幅で表示する() throws Exception {
        given(postService.findPost(1L)).willReturn(Optional.of(postWithId(1L)));
        given(postService.findComments(1L)).willReturn(List.of(
                commentWithId(11L, 1L, "alice", "コメント本文", "blue", TEST_NOW.minusMinutes(5).toInstant())
        ));

        MvcResult result = mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attributeExists("post", "comments", "commentForm"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("<main class=\"post-detail-page\">")
                .contains("class=\"post-detail-nav\"")
                .contains("class=\"post-detail-nav__back\"")
                .contains("href=\"/posts\"")
                .contains(">一覧に戻る</a>")
                .contains("class=\"post post--detail\"")
                .contains("class=\"comments comments--detail\"")
                .contains("class=\"post-form comment-form comment-form--detail\"")
                .containsSubsequence("class=\"post post--detail\"", "class=\"comments comments--detail\"",
                        "class=\"post-form comment-form comment-form--detail\"");

        String css = Files.readString(Path.of("src/main/resources/static/css/app.css"));
        assertThat(css)
                .contains(".post-detail-page")
                .contains(".post-detail-nav__back")
                .contains(".post--detail")
                .contains(".comments--detail")
                .contains(".comment-form--detail")
                .contains("max-width: 720px")
                .contains("box-shadow: 0 1px 3px rgb(15 23 42 / 6%)")
                .contains("border-radius: 8px");
    }

    @Test
    @DisplayName("投稿詳細_ヘッダーとカード_戻るリンク_タイトル_区切り線_投稿カード_コメントカード_フォームカードの順に表示する")
    void 投稿詳細_ヘッダーとカード_戻るリンク_タイトル_区切り線_投稿カード_コメントカード_フォームカードの順に表示する() throws Exception {
        given(postService.findPost(1L)).willReturn(Optional.of(postWithId(1L)));
        given(postService.findComments(1L)).willReturn(List.of(
                commentWithId(11L, 1L, "alice", "コメント本文", "blue", TEST_NOW.minusMinutes(5).toInstant())
        ));

        MvcResult result = mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .containsSubsequence(
                        "class=\"page-header page-header--detail\"",
                        "class=\"post-detail-nav\"",
                        ">一覧に戻る</a>",
                        "<h1>投稿詳細</h1>",
                        "class=\"post post--detail\"",
                        "class=\"comments comments--detail\"",
                        "</section>",
                        "class=\"post-form comment-form comment-form--detail\"");

        String css = Files.readString(Path.of("src/main/resources/static/css/app.css"));
        assertThat(css)
                .contains(".page-header")
                .contains(".comments--detail")
                .contains(".comment-form--detail")
                .contains("border-bottom: 1px solid var(--color-border)");
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
    @DisplayName("投稿詳細_ピンクの投稿の場合_ピンクのアバター色表示要素を表示する")
    void 投稿詳細_ピンクの投稿の場合_ピンクのアバター色表示要素を表示する() throws Exception {
        given(postService.findPost(1L)).willReturn(Optional.of(postWithIdAndAvatarColor(1L, "pink")));

        MvcResult result = mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("post__avatar")
                .contains("post__avatar--pink");
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
    @DisplayName("投稿詳細_コメントがある場合_新しい順で投稿者名本文投稿日を表示する")
    void 投稿詳細_コメントがある場合_新しい順で投稿者名本文投稿日を表示する() throws Exception {
        given(postService.findPost(1L)).willReturn(Optional.of(postWithId(1L)));
        List<PostComment> comments = List.of(
                new PostComment(1L, "new-user", "新しいコメント", "pink", TEST_NOW.minusMinutes(5).toInstant()),
                new PostComment(1L, "old-user", "古いコメント", "green", TEST_NOW.minusDays(1).toInstant())
        );
        given(postService.findComments(1L)).willReturn(comments);

        MvcResult result = mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("comments", sameInstance(comments)))
                .andExpect(model().attributeExists("commentForm"))
                .andReturn();

        assertThat(result.getModelAndView().getModel().get("commentForm")).isInstanceOf(CommentForm.class);
        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("💬 コメント（2）")
                .contains("post__avatar--pink")
                .contains("post__avatar--green")
                .containsSubsequence("new-user", "5分前", "新しいコメント",
                        "old-user", "昨日", "古いコメント");
    }

    @Test
    @DisplayName("投稿詳細_コメント削除後_件数を更新し削除済みコメントを表示せず上部に削除アイコンを表示する")
    void 投稿詳細_コメント削除後_件数を更新し削除済みコメントを表示せず上部に削除アイコンを表示する() throws Exception {
        given(postService.findPost(1L)).willReturn(Optional.of(postWithId(1L)));
        List<PostComment> comments = List.of(
                commentWithId(11L, 1L, "remaining-user", "残るコメント", "blue",
                        TEST_NOW.minusMinutes(5).toInstant())
        );
        given(postService.findComments(1L)).willReturn(comments);

        MvcResult result = mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("💬 コメント（1）")
                .containsSubsequence(
                        "class=\"comment__header\"",
                        "class=\"comment__author-line\"",
                        "remaining-user",
                        "class=\"comment__meta-actions\"",
                        "5分前",
                        "action=\"/posts/1/comments/11/delete\"",
                        "class=\"comment__delete-button\"",
                        "🗑")
                .doesNotContain("削除済みコメント")
                .doesNotContain(">削除<");
    }

    @Test
    @DisplayName("投稿詳細_コメント投稿フォーム_新規投稿画面と同じアバター色選択UIを表示する")
    void 投稿詳細_コメント投稿フォーム_新規投稿画面と同じアバター色選択UIを表示する() throws Exception {
        given(postService.findPost(1L)).willReturn(Optional.of(postWithId(1L)));

        MvcResult result = mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html)
                .contains("action=\"/posts/1/comments\"")
                .contains("class=\"avatar-color-picker\"")
                .contains("class=\"avatar-color-picker__option\"")
                .contains("class=\"avatar-color-picker__input\"")
                .contains("class=\"avatar-color-picker__swatch avatar-color-picker__swatch--red\"")
                .contains("class=\"avatar-color-picker__swatch avatar-color-picker__swatch--blue\"")
                .contains("class=\"avatar-color-picker__swatch avatar-color-picker__swatch--green\"")
                .contains("class=\"avatar-color-picker__swatch avatar-color-picker__swatch--yellow\"")
                .contains("class=\"avatar-color-picker__swatch avatar-color-picker__swatch--purple\"")
                .contains("class=\"avatar-color-picker__swatch avatar-color-picker__swatch--pink\"")
                .contains("name=\"avatarColor\"")
                .contains("type=\"radio\"")
                .contains("value=\"red\"")
                .contains("value=\"blue\"")
                .contains("value=\"green\"")
                .contains("value=\"yellow\"")
                .contains("value=\"purple\"")
                .contains("value=\"pink\"")
                .contains("ピンク")
                .doesNotContain("<select");
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
    @DisplayName("コメント削除_POST_posts_postId_comments_commentId_delete_削除後に投稿詳細へリダイレクトする")
    void コメント削除_POST_posts_postId_comments_commentId_delete_削除後に投稿詳細へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts/1/comments/11/delete"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        then(postService).should().deleteComment(11L);
    }

    @Test
    @DisplayName("コメント投稿_正常な入力の場合_コメントを登録し投稿詳細へリダイレクトする")
    void コメント投稿_正常な入力の場合_コメントを登録し投稿詳細へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts/1/comments")
                        .param("author", "alice")
                        .param("body", "コメント本文")
                        .param("avatarColor", "purple"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        then(postService).should().createComment(1L, "alice", "コメント本文", "purple");
    }

    @Test
    @DisplayName("コメント投稿_ピンクを選択した場合_ピンクでコメントを登録する")
    void コメント投稿_ピンクを選択した場合_ピンクでコメントを登録する() throws Exception {
        mockMvc.perform(post("/posts/1/comments")
                        .param("author", "alice")
                        .param("body", "コメント本文")
                        .param("avatarColor", "pink"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        then(postService).should().createComment(1L, "alice", "コメント本文", "pink");
    }

    @Test
    @DisplayName("コメント投稿_投稿者名未入力の場合_登録せず詳細画面を再表示する")
    void コメント投稿_投稿者名未入力の場合_登録せず詳細画面を再表示する() throws Exception {
        given(postService.findPost(1L)).willReturn(Optional.of(postWithId(1L)));

        MvcResult result = mockMvc.perform(post("/posts/1/comments")
                        .param("author", "")
                        .param("body", "コメント本文")
                        .param("avatarColor", "blue"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attributeHasFieldErrors("commentForm", "author"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("投稿者名を入力してください");
        then(postService).should(never()).createComment(anyLong(), any(), any(), any());
    }

    @Test
    @DisplayName("コメント投稿_本文未入力の場合_登録せず詳細画面を再表示する")
    void コメント投稿_本文未入力の場合_登録せず詳細画面を再表示する() throws Exception {
        given(postService.findPost(1L)).willReturn(Optional.of(postWithId(1L)));

        MvcResult result = mockMvc.perform(post("/posts/1/comments")
                        .param("author", "alice")
                        .param("body", "")
                        .param("avatarColor", "blue"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attributeHasFieldErrors("commentForm", "body"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("コメント本文を入力してください");
        then(postService).should(never()).createComment(anyLong(), any(), any(), any());
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
    @DisplayName("投稿登録_ピンクを選択した場合_ピンクで投稿を登録する")
    void 投稿登録_ピンクを選択した場合_ピンクで投稿を登録する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "本文です")
                        .param("avatarColor", "pink"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().createPost("alice", "本文です", "pink");
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

    private PostComment commentWithId(Long id, Long postId, String author, String body, String avatarColor,
            Instant createdAt) {
        PostComment comment = new PostComment(postId, author, body, avatarColor, createdAt);
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }
}
