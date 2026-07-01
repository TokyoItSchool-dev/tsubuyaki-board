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
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
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

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void 投稿一覧_0件の場合_まだ投稿はありませんを表示する() throws Exception {
        given(postService.latestWithLikes(anyString())).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of()))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsスラッシュへGETする")
    void 投稿一覧_更新ボタン_押すとpostsスラッシュへGetする() throws Exception {
        given(postService.latestWithLikes(anyString())).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/posts/\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("method=\"get\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("更新")));
    }

    @Test
    @DisplayName("投稿一覧_新規投稿リンク_押すとフォームURLへ遷移できる")
    void 投稿一覧_新規投稿リンク_押すとフォームUrlへ遷移できる() throws Exception {
        given(postService.latestWithLikes(anyString())).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/posts/new\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("新規投稿")));
    }

    @Test
    @DisplayName("投稿一覧_上部に枠なし下線付き検索ボックスとグレーのプレースホルダーを表示する")
    void 投稿一覧_上部に枠なし下線付き検索ボックスとグレーのプレースホルダーを表示する() throws Exception {
        given(postService.latestWithLikes(anyString())).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"search-box\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"search-box__input\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("placeholder=\"キーワードを入力\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"search-box__button\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("検索")));
    }

    @Test
    @DisplayName("投稿一覧_検索ボックス_検索ボタン押下でGET_posts_qへ送信できる")
    void 投稿一覧_検索ボックス_検索ボタン押下でGetPostsQへ送信できる() throws Exception {
        given(postService.latestWithLikes(anyString())).willReturn(List.of());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/posts\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("method=\"get\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"q\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("type=\"submit\"")));
    }

    @Test
    @DisplayName("投稿検索_q指定_本文部分一致の投稿だけを表示し結果件数と検索キーワードを保持する")
    void 投稿検索_q指定_本文部分一致の投稿だけを表示し結果件数と検索キーワードを保持する() throws Exception {
        Post older = postWithId(41L, "alice", "検索対象の古い本文", Instant.parse("2026-05-23T10:00:00Z"));
        Post newer = postWithId(42L, "bob", "検索対象の新しい本文", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.searchWithLikes(eq("検索対象"), anyString())).willReturn(List.of(newer, older));
        given(postService.countSearchResults("検索対象")).willReturn(2L);

        MvcResult result = mockMvc.perform(get("/posts").param("q", "検索対象"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("query", "検索対象"))
                .andExpect(model().attribute("searchExecuted", true))
                .andExpect(model().attribute("searchResultCount", 2L))
                .andExpect(model().attribute("posts", List.of(newer, older)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("検索結果：2件")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"検索対象\"")))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).contains("検索対象の新しい本文", "検索対象の古い本文");
        assertThat(html.indexOf("検索対象の新しい本文")).isLessThan(html.indexOf("検索対象の古い本文"));
    }

    @Test
    @DisplayName("投稿検索_q指定_0件の場合_検索結果0件だけを表示し通常の空一覧文言を表示しない")
    void 投稿検索_q指定_0件の場合_検索結果0件だけを表示し通常の空一覧文言を表示しない() throws Exception {
        given(postService.searchWithLikes(eq("なし"), anyString())).willReturn(List.of());
        given(postService.countSearchResults("なし")).willReturn(0L);

        mockMvc.perform(get("/posts").param("q", "なし"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("query", "なし"))
                .andExpect(model().attribute("searchExecuted", true))
                .andExpect(model().attribute("searchResultCount", 0L))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("検索結果：0件")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("まだ投稿はありません"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("class=\"post post--link\""))));
    }

    @Test
    @DisplayName("投稿一覧_投稿がある場合_投稿者内容投稿日の順に表示する")
    void 投稿一覧_投稿がある場合_投稿者内容投稿日の順に表示する() throws Exception {
        Post post = postWithId(
                42L,
                "alice",
                "本文は長くても画面上で折り返して表示する",
                Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.latestWithLikes(anyString())).willReturn(List.of(post));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).contains("alice", "本文は長くても画面上で折り返して表示する", "2026-05-23");
        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("本文は長くても画面上で折り返して表示する"));
        assertThat(html.indexOf("本文は長くても画面上で折り返して表示する")).isLessThan(html.indexOf("2026-05-23"));
    }

    @Test
    @DisplayName("投稿一覧_投稿枠にカーソルを合わせた場合_枠色変更用クラスを適用する")
    void 投稿一覧_投稿枠にカーソルを合わせた場合_枠色変更用クラスを適用する() throws Exception {
        Post post = postWithId(42L, "alice", "本文", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.latestWithLikes(anyString())).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"post post--link\"")));
    }

    @Test
    @DisplayName("投稿一覧_投稿枠内のどこを押下しても_対象の詳細URLへ遷移する")
    void 投稿一覧_投稿枠内のどこを押下しても_対象の詳細Urlへ遷移する() throws Exception {
        Post post = postWithId(42L, "alice", "リンク対象の本文", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.latestWithLikes(anyString())).willReturn(List.of(post));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        int linkStart = html.indexOf("<a class=\"post__main-link\" href=\"/posts/42\"");
        int author = html.indexOf("alice");
        int body = html.indexOf("リンク対象の本文");
        int createdAt = html.indexOf("2026-05-23");
        int linkEnd = html.indexOf("</a>", createdAt);

        assertThat(linkStart).isNotNegative();
        assertThat(linkStart).isLessThan(author);
        assertThat(author).isLessThan(body);
        assertThat(body).isLessThan(createdAt);
        assertThat(createdAt).isLessThan(linkEnd);
    }

    @Test
    @DisplayName("投稿一覧_各投稿の右下に_白抜きハートのいいねトグルを表示する")
    void 投稿一覧_各投稿の右下に_白抜きハートのいいねトグルを表示する() throws Exception {
        Post post = postWithId(42L, "alice", "本文", Instant.parse("2026-05-23T10:15:00Z"));
        post.applyLikeState(0, false);
        given(postService.latestWithLikes(anyString())).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"post__like-form\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/posts/42/likes\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"like-toggle like-toggle--empty\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("♡")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("class=\"like-count\""))));
    }

    @Test
    @DisplayName("投稿一覧_いいねトグル押下_posts_id_likesへ送信し一覧へ戻る")
    void 投稿一覧_いいねトグル押下_postsIdLikesへ送信し一覧へ戻る() throws Exception {
        given(postService.toggleLike(42L, "829dd182")).willReturn(Optional.of(postWithId(
                42L, "alice", "本文", Instant.parse("2026-05-23T10:15:00Z"))));

        mockMvc.perform(post("/posts/42/likes")
                        .param("returnTo", "/posts")
                        .header("User-Agent", "JUnit")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        }))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).toggleLike(42L, "829dd182");
    }

    @Test
    @DisplayName("投稿一覧_未いいねの投稿を押下後_塗りつぶしハートだけを表示する")
    void 投稿一覧_未いいねの投稿を押下後_塗りつぶしハートだけを表示する() throws Exception {
        Post post = postWithId(42L, "alice", "本文", Instant.parse("2026-05-23T10:15:00Z"));
        post.applyLikeState(1, true);
        given(postService.latestWithLikes(anyString())).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"like-toggle like-toggle--liked\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("♥")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("class=\"like-count\""))));
    }

    @Test
    @DisplayName("投稿一覧_同一clientHashで再押下_白抜きハートだけを表示する")
    void 投稿一覧_同一clientHashで再押下_白抜きハートだけを表示する() throws Exception {
        Post post = postWithId(42L, "alice", "本文", Instant.parse("2026-05-23T10:15:00Z"));
        post.applyLikeState(0, false);
        given(postService.latestWithLikes(anyString())).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"like-toggle like-toggle--empty\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("♡")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("class=\"like-count\""))));
    }

    @Test
    @DisplayName("投稿詳細_存在する投稿idの場合_ステータス200で詳細画面を表示する")
    void 投稿詳細_存在する投稿Idの場合_ステータス200で詳細画面を表示する() throws Exception {
        Post post = postWithId(42L, "alice", "詳細の本文", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.findByIdWithLike(42L, "1ad93342")).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/42").header("User-Agent", "JUnit").with(request -> {
                    request.setRemoteAddr("127.0.0.1");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post));
    }

    @Test
    @DisplayName("投稿詳細_存在する投稿idの場合_投稿者内容投稿日を表示する")
    void 投稿詳細_存在する投稿Idの場合_投稿者内容投稿日を表示する() throws Exception {
        Post post = postWithId(42L, "alice", "詳細の本文", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.findByIdWithLike(eq(42L), anyString())).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/42"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("alice")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("詳細の本文")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2026-05-23")));
    }

    @Test
    @DisplayName("投稿詳細_存在する投稿idの場合_詳細用レイアウトクラスを表示し本文ラベルは表示しない")
    void 投稿詳細_存在する投稿Idの場合_詳細用レイアウトクラスを表示し本文ラベルは表示しない() throws Exception {
        Post post = postWithId(42L, "alice", "詳細の本文", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.findByIdWithLike(42L, "1ad93342")).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/42").header("User-Agent", "JUnit").with(request -> {
                    request.setRemoteAddr("127.0.0.1");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"post-detail-nav\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"post-detail\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿者：")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("本文："))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"post-detail__body-box\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"post-detail__created-at\"")));
    }

    @Test
    @DisplayName("投稿詳細_存在しない投稿idの場合_404を返す")
    void 投稿詳細_存在しない投稿Idの場合_404を返す() throws Exception {
        given(postService.findByIdWithLike(999L, "1ad93342")).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999").header("User-Agent", "JUnit").with(request -> {
                    request.setRemoteAddr("127.0.0.1");
                    return request;
                }))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿詳細_本文枠のすぐ下に_いいね数と投稿時間を同じ高さで表示する")
    void 投稿詳細_本文枠のすぐ下に_いいね数と投稿時間を同じ高さで表示する() throws Exception {
        Post post = postWithId(42L, "alice", "詳細の本文", Instant.parse("2026-05-23T10:15:00Z"));
        post.applyLikeState(3, false);
        given(postService.findByIdWithLike(42L, "1ad93342")).willReturn(Optional.of(post));

        MvcResult result = mockMvc.perform(get("/posts/42").header("User-Agent", "JUnit").with(request -> {
                    request.setRemoteAddr("127.0.0.1");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"post-detail__meta-row\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"post-detail__like-panel\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"like-count\">3</span>")))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        int bodyBox = html.indexOf("class=\"post-detail__body-box\"");
        int metaRow = html.indexOf("class=\"post-detail__meta-row\"");
        int likePanel = html.indexOf("class=\"post-detail__like-panel\"");
        int createdAt = html.indexOf("class=\"post-detail__created-at\"");
        assertThat(bodyBox).isNotNegative();
        assertThat(metaRow).isGreaterThan(bodyBox);
        assertThat(likePanel).isGreaterThan(metaRow);
        assertThat(createdAt).isGreaterThan(likePanel);
        assertThat(createdAt).isLessThan(html.indexOf("</article>", metaRow));
    }

    @Test
    @DisplayName("投稿詳細_本文枠外の下の左側に_いいねトグルを表示し詳細へ戻る")
    void 投稿詳細_本文枠外の下の左側に_いいねトグルを表示し詳細へ戻る() throws Exception {
        Post post = postWithId(42L, "alice", "詳細の本文", Instant.parse("2026-05-23T10:15:00Z"));
        post.applyLikeState(4, true);
        given(postService.findByIdWithLike(42L, "1ad93342")).willReturn(Optional.of(post));
        given(postService.toggleLike(42L, "1ad93342")).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/42").header("User-Agent", "JUnit").with(request -> {
                    request.setRemoteAddr("127.0.0.1");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/posts/42/likes\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("class=\"like-toggle like-toggle--liked\"")));

        mockMvc.perform(post("/posts/42/likes")
                        .param("returnTo", "/posts/42")
                        .header("User-Agent", "JUnit")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/42"));

        verify(postService).toggleLike(42L, "1ad93342");
    }

    @Test
    @DisplayName("新規投稿フォーム_GET_posts_new_空のフォームをビューに渡す")
    void 新規投稿フォーム_GET_postsNew_空のフォームをビューに渡す() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.instanceOf(PostForm.class)))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("author",
                        org.hamcrest.Matchers.nullValue())))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("body",
                        org.hamcrest.Matchers.nullValue())));
    }

    @Test
    @DisplayName("投稿作成_投稿者が空文字の場合_エラーを表示し入力内容を保持する")
    void 投稿作成_投稿者が空文字の場合_エラーを表示し入力内容を保持する() throws Exception {
        mockMvc.perform(post("/posts").param("author", "").param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("author",
                        org.hamcrest.Matchers.equalTo(""))))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("body",
                        org.hamcrest.Matchers.equalTo("本文"))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿者名を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_投稿者が空白のみの場合_エラーを表示し入力内容を保持する")
    void 投稿作成_投稿者が空白のみの場合_エラーを表示し入力内容を保持する() throws Exception {
        mockMvc.perform(post("/posts").param("author", "   ").param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("author",
                        org.hamcrest.Matchers.equalTo("   "))))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("body",
                        org.hamcrest.Matchers.equalTo("本文"))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿者名を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_投稿者が31文字以上の場合_エラーを表示し入力内容を保持する")
    void 投稿作成_投稿者が31文字以上の場合_エラーを表示し入力内容を保持する() throws Exception {
        String author = "a".repeat(31);

        mockMvc.perform(post("/posts").param("author", author).param("body", "本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("author",
                        org.hamcrest.Matchers.equalTo(author))))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("body",
                        org.hamcrest.Matchers.equalTo("本文"))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿者名は 30 文字以内で入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_本文が空文字の場合_エラーを表示し入力内容を保持する")
    void 投稿作成_本文が空文字の場合_エラーを表示し入力内容を保持する() throws Exception {
        mockMvc.perform(post("/posts").param("author", "alice").param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("author",
                        org.hamcrest.Matchers.equalTo("alice"))))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("body",
                        org.hamcrest.Matchers.equalTo(""))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_本文が空白のみの場合_エラーを表示し入力内容を保持する")
    void 投稿作成_本文が空白のみの場合_エラーを表示し入力内容を保持する() throws Exception {
        mockMvc.perform(post("/posts").param("author", "alice").param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("author",
                        org.hamcrest.Matchers.equalTo("alice"))))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("body",
                        org.hamcrest.Matchers.equalTo("   "))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文を入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_本文が281文字以上の場合_エラーを表示し入力内容を保持する")
    void 投稿作成_本文が281文字以上の場合_エラーを表示し入力内容を保持する() throws Exception {
        String body = "a".repeat(281);

        mockMvc.perform(post("/posts").param("author", "alice").param("body", body))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("author",
                        org.hamcrest.Matchers.equalTo("alice"))))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.hasProperty("body",
                        org.hamcrest.Matchers.equalTo(body))))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文は 280 文字以内で入力してください")));

        verifyNoInteractions(postService);
    }

    @Test
    @DisplayName("投稿作成_入力が妥当な場合_投稿後にpostsへリダイレクトする")
    void 投稿作成_入力が妥当な場合_投稿後にpostsへリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts").param("author", "alice").param("body", "本文"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "本文");
    }

    private static Post postWithId(Long id, String author, String body, Instant createdAt) {
        Post post = new Post(author, body, createdAt);
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }
}
