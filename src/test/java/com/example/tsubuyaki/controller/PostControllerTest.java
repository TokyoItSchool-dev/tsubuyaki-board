package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
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
    void 投稿一覧_0件の場合_まだ投稿はありませんを表示する() throws Exception {
        given(postService.latestPage(0, 10)).willReturn(pageOf(List.of(), 1, 0));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_postsへGETリクエストするフォームを表示する")
    void 投稿一覧_更新ボタン_postsへGETリクエストするフォームを表示する() throws Exception {
        given(postService.latestPage(0, 10)).willReturn(pageOf(List.of(), 1, 0));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<form action=\"/posts/\" method=\"get\">")))
                .andExpect(content().string(containsString("<button type=\"submit\">更新</button>")));
    }

    @Test
    @DisplayName("投稿一覧_投稿がある場合_投稿者内容投稿日の順に表示する")
    void 投稿一覧_投稿がある場合_投稿者内容投稿日の順に表示する() throws Exception {
        given(postService.latestPage(0, 10)).willReturn(pageOf(List.of(postWithTags(
                1L,
                "alice",
                "今日の共有です #java",
                "Red",
                List.of("java"))), 1, 1));

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("今日の共有です #java"));
        assertThat(html.indexOf("今日の共有です #java")).isLessThan(html.indexOf("2026-05-23T10:00"));
        assertThat(html).contains("avatar--red");
        assertThat(html).contains("href=\"/tags/java\"");
        assertThat(html).contains("#java");
    }

    @Test
    @DisplayName("投稿検索_qが未指定の場合_全件一覧を表示する")
    void 投稿検索_qが未指定の場合_全件一覧を表示する() throws Exception {
        given(postService.latestPage(0, 10)).willReturn(pageOf(
                List.of(postWithId(1L, "alice", "今日の共有です")),
                1,
                1));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("query", ""))
                .andExpect(content().string(containsString("今日の共有です")));

        verify(postService).latestPage(0, 10);
    }

    @Test
    @DisplayName("投稿検索_qに一致する投稿が存在する場合_一致する投稿のみ表示する")
    void 投稿検索_qに一致する投稿が存在する場合_一致する投稿のみ表示する() throws Exception {
        given(postService.searchByBodyPage("共有", 0, 10)).willReturn(pageOf(
                List.of(postWithId(1L, "alice", "今日の共有です")),
                1,
                1));

        mockMvc.perform(get("/posts").param("q", "共有"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("query", "共有"))
                .andExpect(content().string(containsString("今日の共有です")))
                .andExpect(content().string(containsString("value=\"共有\"")));

        verify(postService).searchByBodyPage("共有", 0, 10);
    }

    @Test
    @DisplayName("投稿検索_qに一致する投稿が存在しない場合_空一覧を200で表示する")
    void 投稿検索_qに一致する投稿が存在しない場合_空一覧を200で表示する() throws Exception {
        given(postService.searchByBodyPage("missing", 0, 10)).willReturn(pageOf(List.of(), 1, 0));

        mockMvc.perform(get("/posts").param("q", "missing"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("query", "missing"))
                .andExpect(content().string(containsString("まだ投稿はありません")))
                .andExpect(content().string(containsString("value=\"missing\"")));
    }

    @Test
    @DisplayName("投稿一覧_詳細リンク_投稿詳細画面へ遷移できる")
    void 投稿一覧_詳細リンク_投稿詳細画面へ遷移できる() throws Exception {
        given(postService.latestPage(0, 10)).willReturn(pageOf(
                List.of(postWithId(1L, "alice", "今日の共有です")),
                1,
                1));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/posts/1\"")))
                .andExpect(content().string(containsString(">詳細</a>")));
    }

    @Test
    @DisplayName("投稿一覧_新規投稿リンク_投稿作成フォーム画面へ遷移できる")
    void 投稿一覧_新規投稿リンク_投稿作成フォーム画面へ遷移できる() throws Exception {
        given(postService.latestPage(0, 10)).willReturn(pageOf(List.of(), 1, 0));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/posts/new\"")));

        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(content().string(containsString("新規投稿")));
    }

    @Test
    @DisplayName("投稿作成フォーム_画面表示時_postFormをmodelに格納する")
    void 投稿作成フォーム_画面表示時_postFormをmodelに格納する() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)))
                .andExpect(model().attribute("avatarColors", PostForm.avatarColorOptions()))
                .andExpect(content().string(containsString("<select id=\"avatarColor\"")))
                .andExpect(content().string(containsString("value=\"Red\"")))
                .andExpect(content().string(containsString("value=\"Gray\" selected=\"selected\"")));
    }

    @Test
    @DisplayName("投稿詳細_存在するidの場合_投稿内容を表示する")
    void 投稿詳細_存在するidの場合_投稿内容を表示する() throws Exception {
        given(postService.findById(1L)).willReturn(Optional.of(postWithTags(
                1L,
                "alice",
                "今日の共有です #spring",
                "Blue",
                List.of("spring"))));
        given(postService.countLikes(1L)).willReturn(3L);

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attribute("likeCount", 3L))
                .andExpect(content().string(containsString("投稿者")))
                .andExpect(content().string(containsString("avatar--blue")))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("関連投稿一覧:")))
                .andExpect(content().string(containsString("本文")))
                .andExpect(content().string(containsString("今日の共有です #spring")))
                .andExpect(content().string(containsString("href=\"/tags/spring\"")))
                .andExpect(content().string(containsString("いいね数")))
                .andExpect(content().string(containsString("action=\"/posts/1/likes\"")))
                .andExpect(content().string(containsString(">Like</button>")))
                .andExpect(content().string(containsString("2026-05-23T10:00")));
    }

    @Test
    @DisplayName("ハッシュタグ_GET_tags_name_関連投稿のみ一覧表示する")
    void ハッシュタグ_GET_tags_name_関連投稿のみ一覧表示する() throws Exception {
        given(postService.findByTagPage("java", 0, 10)).willReturn(pageOf(
                List.of(postWithId(1L, "alice", "#java の話です")),
                1,
                1));

        mockMvc.perform(get("/tags/java"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("tagName", "java"))
                .andExpect(model().attribute("query", ""))
                .andExpect(content().string(containsString("#java")))
                .andExpect(content().string(containsString("#java の話です")));

        verify(postService).findByTagPage("java", 0, 10);
    }

    @Test
    @DisplayName("ハッシュタグ_GET_tags_name_存在しないタグは空一覧を200で表示する")
    void ハッシュタグ_GET_tags_name_存在しないタグは空一覧を200で表示する() throws Exception {
        given(postService.findByTagPage("missing", 0, 10)).willReturn(pageOf(List.of(), 1, 0));

        mockMvc.perform(get("/tags/missing"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("tagName", "missing"))
                .andExpect(content().string(containsString("#missing")))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿詳細_存在しないidの場合_404を返す")
    void 投稿詳細_存在しないidの場合_404を返す() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("いいね_POST_posts_id_likes_clientHashを生成して詳細へリダイレクトする")
    void いいね_POST_posts_id_likes_clientHashを生成して詳細へリダイレクトする() throws Exception {
        given(postService.toggleLike(1L, clientHash("192.0.2.10", "JUnit"))).willReturn(true);

        mockMvc.perform(post("/posts/1/likes")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.10");
                            return request;
                        })
                        .header("User-Agent", "JUnit"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).toggleLike(1L, clientHash("192.0.2.10", "JUnit"));
    }

    @Test
    @DisplayName("いいね_POST_存在しない投稿idの場合_404を返す")
    void いいね_POST_存在しない投稿idの場合_404を返す() throws Exception {
        given(postService.toggleLike(999L, clientHash("192.0.2.10", "JUnit"))).willReturn(false);

        mockMvc.perform(post("/posts/999/likes")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.10");
                            return request;
                        })
                        .header("User-Agent", "JUnit"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿作成_authorとbodyが範囲内のとき_302で投稿一覧へリダイレクトする")
    void 投稿作成_authorとbodyが範囲内のとき_302で投稿一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("avatarColor", "Blue")
                        .param("body", "今日の共有です"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "今日の共有です", "Blue");
    }

    @Test
    @DisplayName("投稿作成_authorとbodyの前後空白を除いた長さが範囲内のとき_302で保存する")
    void 投稿作成_authorとbodyの前後空白を除いた長さが範囲内のとき_302で保存する() throws Exception {
        String author = "  " + "a".repeat(30) + "  ";
        String body = "  " + "b".repeat(280) + "  ";

        mockMvc.perform(post("/posts")
                        .param("author", author)
                        .param("avatarColor", "Purple")
                        .param("body", body))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("a".repeat(30), "b".repeat(280), "Purple");
    }

    @Test
    @DisplayName("投稿作成_authorが空白のみのとき_200でフォームを再表示しエラー表示する")
    void 投稿作成_authorが空白のみのとき_200でフォームを再表示しエラー表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("avatarColor", "Green")
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者を入力してください")));

        verify(postService, never()).create(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("投稿作成_bodyが空白のみのとき_200でフォームを再表示しエラー表示する")
    void 投稿作成_bodyが空白のみのとき_200でフォームを再表示しエラー表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("avatarColor", "Green")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")));

        verify(postService, never()).create(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("投稿作成_authorが31文字のとき_200でフォームを再表示しエラー表示する")
    void 投稿作成_authorが31文字のとき_200でフォームを再表示しエラー表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "a".repeat(31))
                        .param("avatarColor", "Green")
                        .param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者は30文字以内で入力してください")));

        verify(postService, never()).create(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("投稿作成_bodyが281文字のとき_200でフォームを再表示しエラー表示する")
    void 投稿作成_bodyが281文字のとき_200でフォームを再表示しエラー表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("avatarColor", "Green")
                        .param("body", "あ".repeat(281)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文は280文字以内で入力してください")));

        verify(postService, never()).create(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("投稿作成_バリデーションエラー時_入力値をフォームに保持する")
    void 投稿作成_バリデーションエラー時_入力値をフォームに保持する() throws Exception {
        String body = "b".repeat(281);

        mockMvc.perform(post("/posts")
                        .param("author", "  alice  ")
                        .param("avatarColor", "Orange")
                        .param("body", body))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(content().string(containsString("value=\"  alice  \"")))
                .andExpect(content().string(containsString("value=\"Orange\" selected=\"selected\"")))
                .andExpect(content().string(containsString(">" + body + "</textarea>")));

        verify(postService, never()).create(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("投稿一覧_次ページがある場合_ページリンクを表示する")
    void 投稿一覧_次ページがある場合_ページリンクを表示する() throws Exception {
        given(postService.latestPage(0, 10)).willReturn(pageOf(
                List.of(postWithId(1L, "alice", "1ページ目の投稿")),
                1,
                11));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("totalPages", 2))
                .andExpect(content().string(containsString("1 / 2")))
                .andExpect(content().string(containsString("page=2")));
    }

    @Test
    @DisplayName("投稿一覧_page指定の場合_指定ページを取得する")
    void 投稿一覧_page指定の場合_指定ページを取得する() throws Exception {
        given(postService.latestPage(1, 10)).willReturn(pageOf(
                List.of(postWithId(11L, "bob", "2ページ目の投稿")),
                2,
                11));

        mockMvc.perform(get("/posts").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentPage", 2))
                .andExpect(content().string(containsString("2ページ目の投稿")));

        verify(postService).latestPage(1, 10);
    }

    @Test
    @DisplayName("投稿検索_ページリンク_検索キーワードを保持する")
    void 投稿検索_ページリンク_検索キーワードを保持する() throws Exception {
        given(postService.searchByBodyPage("java", 0, 10)).willReturn(pageOf(
                List.of(postWithId(1L, "alice", "javaの話です")),
                1,
                11));

        mockMvc.perform(get("/posts").param("q", "java"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("q=java")))
                .andExpect(content().string(containsString("page=2")));
    }

    @Test
    @DisplayName("ハッシュタグ_ページリンク_tagsパスを保持する")
    void ハッシュタグ_ページリンク_tagsパスを保持する() throws Exception {
        given(postService.findByTagPage("java", 0, 10)).willReturn(pageOf(
                List.of(postWithId(1L, "alice", "#java の話です")),
                1,
                11));

        mockMvc.perform(get("/tags/java"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/tags/java?page=2")));
    }

    private Post postWithId(Long id, String author, String body) {
        return postWithId(id, author, body, "Gray");
    }

    private Post postWithId(Long id, String author, String body, String avatarColor) {
        Post post = new Post(author, body, avatarColor, LocalDateTime.parse("2026-05-23T10:00:00"));
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    private Post postWithTags(Long id, String author, String body, String avatarColor, List<String> tags) {
        Post post = postWithId(id, author, body, avatarColor);
        post.replaceTags(tags.stream().map(Tag::new).toList());
        return post;
    }

    private PageImpl<Post> pageOf(List<Post> posts, int currentPage, long totalElements) {
        return new PageImpl<>(posts, PageRequest.of(currentPage - 1, 10), totalElements);
    }

    private String clientHash(String remoteAddress, String userAgent) throws NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest((remoteAddress + userAgent).getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest).substring(0, 8);
    }
}
