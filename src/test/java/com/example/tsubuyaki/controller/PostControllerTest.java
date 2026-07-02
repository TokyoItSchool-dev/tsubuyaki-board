package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
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
    void list_emptyPosts_showsEmptyMessage() throws Exception {
        given(postService.latestPage(0)).willReturn(pageOf(List.of(), 0, 0));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of()))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsスラッシュへGETリクエストする")
    void list_refreshButton_requestsPostsSlash() throws Exception {
        given(postService.latestPage(0)).willReturn(pageOf(List.of(), 0, 0));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/posts/\"")))
                .andExpect(content().string(containsString("method=\"get\"")))
                .andExpect(content().string(containsString("<button type=\"submit\"")));
    }

    @Test
    @DisplayName("投稿一覧_検索ボックス_一覧画面上部に表示する")
    void list_searchBox_existsInToolbar() throws Exception {
        given(postService.latestPage(0)).willReturn(pageOf(List.of(), 0, 0));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"searchQuery\"")))
                .andExpect(content().string(containsString("name=\"q\"")))
                .andExpect(content().string(containsString("id=\"searchSubmit\"")))
                .andExpect(content().string(containsString("disabled")));
    }

    @Test
    @DisplayName("投稿一覧_キーワード検索_GET_posts_qで本文検索結果を表示する")
    void list_searchKeyword_displaysSearchResults() throws Exception {
        List<Post> posts = List.of(postWithId(10L, "alice", "検索対象の本文", Instant.parse("2026-05-23T10:15:00Z")));
        given(postService.searchPage("検索", 0)).willReturn(pageOf(posts, 0, 1));

        mockMvc.perform(get("/posts").param("q", "検索"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", posts))
                .andExpect(model().attribute("searchQuery", "検索"))
                .andExpect(content().string(containsString("value=\"検索\"")));

        verify(postService).searchPage("検索", 0);
    }

    @Test
    @DisplayName("投稿一覧_空白のみ検索_検索せず通常一覧を表示する")
    void list_blankSearchQuery_usesLatestPage() throws Exception {
        given(postService.latestPage(0)).willReturn(pageOf(List.of(), 0, 0));

        mockMvc.perform(get("/posts").param("q", " 　"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("searchQuery", " 　"))
                .andExpect(content().string(containsString("id=\"searchSubmit\"")))
                .andExpect(content().string(containsString("disabled")));

        verify(postService).latestPage(0);
        verify(postService, never()).searchPage(anyString(), anyInt());
    }

    @Test
    @DisplayName("投稿一覧_検索ボックス_空文字空白タグ疑い文字では検索ボタンを非活性にする")
    void list_searchBox_disablesSubmitForBlankOrSuspiciousInput() throws Exception {
        given(postService.latestPage(0)).willReturn(pageOf(List.of(), 0, 0));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("const unsafeSearchPattern = /[<>]/;")))
                .andExpect(content().string(containsString("searchSubmit.disabled")))
                .andExpect(content().string(containsString("!searchQuery.value.trim()")))
                .andExpect(content().string(containsString("unsafeSearchPattern.test(searchQuery.value)")));
    }

    @Test
    @DisplayName("投稿一覧_投稿あり_投稿者内容投稿日の順に表示する")
    void list_withPost_displaysAuthorBodyCreatedAtInOrder() throws Exception {
        Post post = postWithId(10L, "alice", "本文です", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.latestPage(0)).willReturn(pageOf(List.of(post), 0, 1));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).contains("alice", "本文です", "2026-05-23 19:15");
        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("本文です"));
        assertThat(html.indexOf("本文です")).isLessThan(html.indexOf("2026-05-23 19:15"));
    }

    @Test
    @DisplayName("投稿一覧_投稿あり_IDを表示し詳細へのリンクにする")
    void list_withPost_displaysIdLinkToDetail() throws Exception {
        Post post = postWithId(10L, "alice", "本文です", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.latestPage(0)).willReturn(pageOf(List.of(post), 0, 1));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/posts/10\"")))
                .andExpect(content().string(containsString("ID 10")));
    }

    @Test
    @DisplayName("投稿一覧_投稿あり_円形のアバター表示箇所を持つ")
    void list_withPost_displaysAvatarSlot() throws Exception {
        Post post = postWithId(10L, "alice", "本文です", Instant.parse("2026-05-23T10:15:00Z"));
        post.setAvatarColor("#2563eb");
        given(postService.latestPage(0)).willReturn(pageOf(List.of(post), 0, 1));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("post__avatar")))
                .andExpect(content().string(containsString("background-color: #2563eb")));
    }

    @Test
    @DisplayName("投稿一覧_画像アバターあり_円形箇所に画像を表示する")
    void list_withImageAvatar_displaysAvatarImage() throws Exception {
        Post post = postWithId(10L, "alice", "本文です", Instant.parse("2026-05-23T10:15:00Z"));
        post.setAvatarImageContentType("image/png");
        post.setAvatarImageData("image".getBytes(StandardCharsets.UTF_8));
        given(postService.latestPage(0)).willReturn(pageOf(List.of(post), 0, 1));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<img class=\"post__avatar\"")))
                .andExpect(content().string(containsString("src=\"data:image/png;base64,aW1hZ2U=\"")));
    }

    @Test
    @DisplayName("投稿一覧_本文が100文字を超える場合_100文字と省略記号で表示する")
    void list_bodyOverOneHundredCharacters_truncatesBody() throws Exception {
        String body = "あ".repeat(101);
        given(postService.latestPage(0)).willReturn(pageOf(List.of(
                new Post("alice", body, Instant.parse("2026-05-23T10:15:00Z"))), 0, 1));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).contains("あ".repeat(100) + "....");
        assertThat(html).contains("data-tooltip=\"" + "あ".repeat(101) + "\"");
    }

    @Test
    @DisplayName("投稿一覧_三点リーダー投稿_ホバー用ツールチップ属性を持つ")
    void list_truncatedBody_hasTooltip() throws Exception {
        String body = "ツールチップ確認".repeat(13);
        given(postService.latestPage(0)).willReturn(pageOf(List.of(
                new Post("alice", body, Instant.parse("2026-05-23T10:15:00Z"))), 0, 1));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("post__body--truncated")))
                .andExpect(content().string(containsString("data-tooltip=")))
                .andExpect(content().string(containsString("title=")))
                .andExpect(content().string(containsString("tabindex=\"0\"")));
    }

    @Test
    @DisplayName("投稿一覧_1ページあたり_50件の投稿で収まる")
    void list_firstPage_containsFiftyPosts() throws Exception {
        List<Post> posts = posts(50);
        given(postService.latestPage(0)).willReturn(pageOf(posts, 0, 102));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", posts))
                .andExpect(model().attribute("totalPages", 3));
    }

    @Test
    @DisplayName("投稿一覧_ページ遷移リンク_クリック先の別ページを表示できる")
    void list_pageLink_requestsDifferentPage() throws Exception {
        List<Post> secondPagePosts = posts(50);
        given(postService.latestPage(1)).willReturn(pageOf(secondPagePosts, 1, 102));

        mockMvc.perform(get("/posts/").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", secondPagePosts))
                .andExpect(content().string(containsString("href=\"/posts/?page=0\"")))
                .andExpect(content().string(containsString("href=\"/posts/?page=2\"")));
        verify(postService).latestPage(1);
    }

    @Test
    @DisplayName("投稿一覧_TOPへ戻るボタン_クリック時にページ最上部アンカーへ移動する")
    void list_backToTopButton_pointsTopAnchor() throws Exception {
        given(postService.latestPage(0)).willReturn(pageOf(List.of(), 0, 0));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"top\"")))
                .andExpect(content().string(containsString("class=\"back-to-top\"")))
                .andExpect(content().string(containsString("href=\"#top\"")));
    }

    @Test
    @DisplayName("投稿作成_本文scriptタグ対策_投稿ボタンを初期無効化し入力監視を持つ")
    void newForm_hasScriptTagGuardForSubmitButton() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-forbidden-pattern=\"&lt;script\"")))
                .andExpect(content().string(containsString("id=\"postSubmit\"")))
                .andExpect(content().string(containsString("disabled")));
    }

    @Test
    @DisplayName("投稿作成_GET_フォーム表示時にpostFormをmodelへ積む")
    void newForm_addsPostFormToModel() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"));
    }

    @Test
    @DisplayName("投稿作成_未入力_エラーでフォームを再表示し保存しない")
    void create_emptyValues_redisplaysForm() throws Exception {
        mockMvc.perform(post("/posts").param("author", "").param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(content().string(containsString("投稿者名を入力してください")))
                .andExpect(content().string(containsString("本文を入力してください")));

        verify(postService, never()).create(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("投稿作成_上限超え_エラーでフォームを再表示する")
    void create_overMaxLength_redisplaysForm() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "a".repeat(31))
                        .param("body", "b".repeat(281)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(content().string(containsString("投稿者名は 30 文字以内で入力してください")))
                .andExpect(content().string(containsString("本文は 280 文字以内で入力してください")));
    }

    @Test
    @DisplayName("投稿作成_半角全角スペースのみ_エラーでフォームを再表示する")
    void create_blankSpaces_redisplaysForm() throws Exception {
        mockMvc.perform(post("/posts").param("author", " ").param("body", "　"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(content().string(containsString("投稿者名を入力してください")))
                .andExpect(content().string(containsString("本文を入力してください")));
    }

    @Test
    @DisplayName("投稿作成_bodyだけエラー_再表示時にauthorの入力値を残す")
    void create_bodyError_keepsAuthorValue() throws Exception {
        mockMvc.perform(post("/posts").param("author", "alice").param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(content().string(containsString("value=\"alice\"")))
                .andExpect(content().string(containsString("本文を入力してください")));
    }

    @Test
    @DisplayName("投稿作成_正常最小値_登録してpostsへリダイレクトする")
    void create_minLength_redirectsPosts() throws Exception {
        mockMvc.perform(post("/posts").param("author", "a").param("body", "b").param("avatarColor", "#2563eb"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("a", "b", "#2563eb", null, null);
    }

    @Test
    @DisplayName("投稿作成_正常最大値_登録してpostsへリダイレクトする")
    void create_maxLength_redirectsPosts() throws Exception {
        String author = "a".repeat(30);
        String body = "b".repeat(280);

        mockMvc.perform(post("/posts").param("author", author).param("body", body).param("avatarColor", "#2563eb"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create(author, body, "#2563eb", null, null);
    }

    @Test
    @DisplayName("投稿作成_画像アップロード_カラーなしで登録できる")
    void create_imageAvatarWithoutColor_redirectsPosts() throws Exception {
        MockMultipartFile avatarImage = new MockMultipartFile(
                "avatarImage", "avatar.png", "image/png", "image".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/posts")
                        .file(avatarImage)
                        .param("author", "alice")
                        .param("body", "本文"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "本文", null, "image/png", "image".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("投稿作成_カラーと画像を同時指定_エラーでフォームを再表示する")
    void create_colorAndImage_redisplaysForm() throws Exception {
        MockMultipartFile avatarImage = new MockMultipartFile(
                "avatarImage", "avatar.png", "image/png", "image".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/posts")
                        .file(avatarImage)
                        .param("author", "alice")
                        .param("body", "本文")
                        .param("avatarColor", "#2563eb"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(content().string(containsString("カラーと画像は同時に選択できません")));
    }

    @Test
    @DisplayName("投稿詳細_存在しないID_404を返す")
    void detail_unknownId_returnsNotFound() throws Exception {
        given(postService.findVisibleById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿詳細_存在するID_詳細ビューに投稿者本文全文日時と編集削除ボタンを表示する")
    void detail_existingId_showsPostDetail() throws Exception {
        Post post = postWithId(10L, "alice", "本文の全文です", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.findVisibleById(10L)).willReturn(Optional.of(post));
        given(postService.likeCount(10L)).willReturn(3L);

        mockMvc.perform(get("/posts/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("本文の全文です")))
                .andExpect(content().string(containsString("2026-05-23 19:15")))
                .andExpect(content().string(containsString("href=\"/posts/10/edit\"")))
                .andExpect(content().string(containsString("action=\"/posts/10/delete\"")))
                .andExpect(content().string(containsString("いいね数: 3")))
                .andExpect(content().string(containsString("action=\"/posts/10/likes\"")))
                .andExpect(content().string(containsString("Like")));
    }

    @Test
    @DisplayName("投稿詳細_いいねボタン_同じIPとUAからclientHashを作ってトグルする")
    void like_sameClientHash_togglesLike() throws Exception {
        String remoteAddress = "192.0.2.10";
        String userAgent = "JUnit Browser";
        String expectedClientHash = sha256First8(remoteAddress + userAgent);
        Post post = postWithId(10L, "alice", "本文です", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.toggleLike(10L, expectedClientHash)).willReturn(Optional.of(post));

        mockMvc.perform(post("/posts/10/likes")
                        .with(request -> {
                            request.setRemoteAddr(remoteAddress);
                            return request;
                        })
                        .header("User-Agent", userAgent))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/10"));

        verify(postService).toggleLike(10L, expectedClientHash);
    }

    @Test
    @DisplayName("投稿詳細_編集ボタン_対象投稿の編集フォームへ遷移できる")
    void editForm_existingId_showsOnlyTargetPostForm() throws Exception {
        Post post = postWithId(10L, "alice", "本文です", Instant.parse("2026-05-23T10:15:00Z"));
        post.setAvatarColor("#2563eb");
        given(postService.findVisibleById(10L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/10/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postId", 10L))
                .andExpect(content().string(containsString("value=\"alice\"")))
                .andExpect(content().string(containsString("value=\"#2563eb\"")))
                .andExpect(content().string(containsString("本文です")))
                .andExpect(content().string(containsString("action=\"/posts/10/edit\"")));
    }

    @Test
    @DisplayName("投稿詳細_削除ボタン_対象投稿をごみ箱へ移して一覧へ戻る")
    void delete_existingId_movesPostToTrash() throws Exception {
        Post post = postWithId(10L, "alice", "本文です", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.moveToTrash(10L)).willReturn(Optional.of(post));

        mockMvc.perform(post("/posts/10/delete"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"))
                .andExpect(request().sessionAttribute("deletedPostId", 10L));

        verify(postService).moveToTrash(10L);
    }

    @Test
    @DisplayName("ごみ箱_削除された投稿_ごみ箱画面で表示できる")
    void trash_deletedPost_showsInTrash() throws Exception {
        Post deletedPost = postWithId(10L, "alice", "削除済み本文", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.trashedPosts()).willReturn(List.of(deletedPost));

        mockMvc.perform(get("/posts/trash"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/trash"))
                .andExpect(model().attribute("posts", List.of(deletedPost)))
                .andExpect(content().string(containsString("ID 10")))
                .andExpect(content().string(containsString("削除済み本文")));
    }

    @Test
    @DisplayName("投稿一覧_通知欄_編集削除された投稿IDを表示する")
    void list_flashNotice_showsEditedAndDeletedPostIds() throws Exception {
        given(postService.latestPage(0)).willReturn(pageOf(List.of(), 0, 0));

        mockMvc.perform(get("/posts").sessionAttr("editedPostId", 10L).sessionAttr("deletedPostId", 11L))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ID10が編集されました。")))
                .andExpect(content().string(containsString("ID11が削除されました。")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_お知らせ欄の投稿IDを消さずに表示する")
    void list_refresh_keepsNoticeIds() throws Exception {
        given(postService.latestPage(0)).willReturn(pageOf(List.of(), 0, 0));

        mockMvc.perform(get("/posts/").sessionAttr("editedPostId", 10L).sessionAttr("deletedPostId", 11L))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ID10が編集されました。")))
                .andExpect(content().string(containsString("ID11が削除されました。")));
    }

    @Test
    @DisplayName("投稿編集_正常値_対象投稿だけ更新して詳細へリダイレクトする")
    void update_validValues_updatesTargetPost() throws Exception {
        Post post = postWithId(10L, "bob", "更新本文", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.update(10L, "bob", "更新本文", "#dc2626", null, null)).willReturn(Optional.of(post));

        mockMvc.perform(post("/posts/10/edit")
                        .param("author", "bob")
                        .param("body", "更新本文")
                        .param("avatarColor", "#dc2626"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/10"))
                .andExpect(request().sessionAttribute("editedPostId", 10L));

        verify(postService).update(10L, "bob", "更新本文", "#dc2626", null, null);
    }

    @Test
    @DisplayName("投稿編集_画像アップロード_アバター画像を更新できる")
    void update_imageAvatar_updatesTargetPost() throws Exception {
        MockMultipartFile avatarImage = new MockMultipartFile(
                "avatarImage", "avatar.png", "image/png", "image".getBytes(StandardCharsets.UTF_8));
        Post post = postWithId(10L, "bob", "更新本文", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.update(10L, "bob", "更新本文", null, "image/png",
                "image".getBytes(StandardCharsets.UTF_8))).willReturn(Optional.of(post));

        mockMvc.perform(multipart("/posts/10/edit")
                        .file(avatarImage)
                        .param("author", "bob")
                        .param("body", "更新本文"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/10"));

        verify(postService).update(10L, "bob", "更新本文", null, "image/png",
                "image".getBytes(StandardCharsets.UTF_8));
    }

    private static Page<Post> pageOf(List<Post> posts, int page, long total) {
        return new PageImpl<>(posts, PageRequest.of(page, 50), total);
    }

    private static List<Post> posts(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(index -> new Post("user" + index, "body" + index,
                        Instant.parse("2026-05-23T10:00:00Z").plusSeconds(index)))
                .toList();
    }

    private static Post postWithId(Long id, String author, String body, Instant createdAt) {
        Post post = new Post(author, body, createdAt);
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    private static String sha256First8(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
