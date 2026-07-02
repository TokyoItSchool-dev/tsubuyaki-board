package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostTag;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    void list_whenEmpty_showsEmptyMessage() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", Collections.emptyList()))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_検索ボックス_一覧画面上部に表示する")
    void list_hasSearchBoxAtTop() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"q\"")))
                .andExpect(content().string(containsString("検索")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html.indexOf("name=\"q\"")).isLessThan(html.indexOf("まだ投稿はありません"));
    }

    @Test
    @DisplayName("投稿一覧_検索ボタン_GET_postsへ送信する")
    void list_searchButtonSubmitsGetToPosts() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("method=\"get\"")))
                .andExpect(content().string(containsString("action=\"/posts\"")))
                .andExpect(content().string(containsString("type=\"submit\"")))
                .andExpect(content().string(containsString("検索")));

        mockMvc.perform(get("/posts/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"));
    }

    @Test
    @DisplayName("投稿検索_GET_posts_q指定_bodyにキーワードを含む投稿だけを表示する")
    void list_whenQueryPresent_showsSearchResult() throws Exception {
        Post post = new Post("alice", "abcを含む投稿", Instant.parse("2026-05-23T01:00:00Z"));
        given(postService.search("abc")).willReturn(List.of(post));

        mockMvc.perform(get("/posts").param("q", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of(post)))
                .andExpect(content().string(containsString("abcを含む投稿")));

        verify(postService).search("abc");
        verify(postService, never()).latest();
    }

    @Test
    @DisplayName("投稿検索_q空_通常の新着50件を表示する")
    void list_whenQueryEmpty_showsLatestPosts() throws Exception {
        Post post = new Post("alice", "通常一覧の投稿", Instant.parse("2026-05-23T01:00:00Z"));
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts").param("q", ""))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", List.of(post)))
                .andExpect(content().string(containsString("通常一覧の投稿")));

        verify(postService).latest();
        verify(postService, never()).search(anyString());
    }

    @Test
    @DisplayName("投稿一覧_タグあり_各投稿のタグ一覧とタグリンクを表示する")
    void list_whenPostHasTags_showsTagLinks() throws Exception {
        Post post = postWithTags("alice", "#java #spring 本文です", List.of("java", "spring"));
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("post__tags")))
                .andExpect(content().string(containsString("#java")))
                .andExpect(content().string(containsString("#spring")))
                .andExpect(content().string(containsString("href=\"/tags/java\"")))
                .andExpect(content().string(containsString("href=\"/tags/spring\"")));
    }

    @Test
    @DisplayName("投稿一覧_タグなし_タグ欄を表示しない")
    void list_whenPostHasNoTags_hidesTagsBlock() throws Exception {
        Post post = new Post("alice", "タグなし本文です", Instant.parse("2026-05-23T01:00:00Z"));
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("post__tags"))));
    }

    @Test
    @DisplayName("投稿検索_タグあり_検索結果一覧でもタグ一覧を表示する")
    void list_whenSearchResultHasTags_showsTagLinks() throws Exception {
        Post post = postWithTags("alice", "abc #java 本文です", List.of("java"));
        given(postService.search("abc")).willReturn(List.of(post));

        mockMvc.perform(get("/posts").param("q", "abc"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("#java")))
                .andExpect(content().string(containsString("href=\"/tags/java\"")));
    }

    @Test
    @DisplayName("タグ一覧_GET_tags_name_タグに紐づく投稿一覧とタグ名を表示する")
    void tagList_whenTagNamePresent_showsTaggedPostsAndTagHeading() throws Exception {
        Post post = new Post("alice", "#java 本文です", Instant.parse("2026-05-23T01:00:00Z"));
        given(postService.findByTagName("java")).willReturn(List.of(post));

        mockMvc.perform(get("/tags/java"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of(post)))
                .andExpect(model().attribute("tagName", "java"))
                .andExpect(content().string(containsString("タグ：java")))
                .andExpect(content().string(containsString("#java 本文です")));

        verify(postService).findByTagName("java");
    }

    @Test
    @DisplayName("投稿検索_検索結果0件_まだ投稿はありませんを表示する")
    void list_whenSearchResultEmpty_showsEmptyMessage() throws Exception {
        given(postService.search("none")).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts").param("q", "none"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", Collections.emptyList()))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿検索_入力した検索語_フォームに保持する")
    void list_whenQueryPresent_keepsSearchWordInForm() throws Exception {
        given(postService.search("abc")).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts").param("q", "abc"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("q", "abc"))
                .andExpect(content().string(containsString("value=\"abc\"")));
    }

    @Test
    @DisplayName("投稿一覧_投稿表示_投稿者内容登録日時更新日時の順に表示する")
    void list_displaysAuthorBodyCreatedAtAndUpdatedAtInOrder() throws Exception {
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T01:00:00Z"));
        post.update("alice", "本文です", "blue", Instant.parse("2026-05-24T02:00:00Z"));
        given(postService.latest()).willReturn(List.of(post));

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("alice", "本文です", "登録日:", "2026-05-23 10:00", "更新日:", "2026-05-24 11:00");
        assertThat(html).contains("class=\"post__dates\"");
        assertThat(html).contains("class=\"post__created-at\"");
        assertThat(html).contains("class=\"post__updated-at\"");
        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("本文です"));
        assertThat(html.indexOf("本文です")).isLessThan(html.indexOf("登録日:"));
        assertThat(html.indexOf("登録日:")).isLessThan(html.indexOf("2026-05-23 10:00"));
        assertThat(html.indexOf("2026-05-23 10:00")).isLessThan(html.indexOf("更新日:"));
        assertThat(html.indexOf("更新日:")).isLessThan(html.indexOf("2026-05-24 11:00"));
    }

    @Test
    @DisplayName("投稿検索_検索結果一覧_登録日時と更新日時を表示する")
    void list_whenSearchResult_displaysCreatedAtAndUpdatedAt() throws Exception {
        Post post = new Post("alice", "abcを含む投稿", Instant.parse("2026-05-23T01:00:00Z"));
        post.update("alice", "abcを含む投稿", "blue", Instant.parse("2026-05-24T02:00:00Z"));
        given(postService.search("abc")).willReturn(List.of(post));

        mockMvc.perform(get("/posts").param("q", "abc"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("登録日:")))
                .andExpect(content().string(containsString("2026-05-23 10:00")))
                .andExpect(content().string(containsString("更新日:")))
                .andExpect(content().string(containsString("2026-05-24 11:00")));
    }

    @Test
    @DisplayName("投稿一覧_投稿表示_投稿者名の横にアバター色を表示する")
    void list_displaysAvatarColorNextToAuthor() throws Exception {
        given(postService.latest()).willReturn(List.of(
                new Post("alice", "本文です", "purple", Instant.parse("2026-05-23T01:00:00Z"))
        ));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("● purple")))
                .andExpect(content().string(containsString("post__avatar-color--purple")));
    }

    @Test
    @DisplayName("投稿一覧_投稿ごとに編集リンクを表示する")
    void list_displaysEditLinkForEachPost() throws Exception {
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T01:00:00Z"));
        ReflectionTestUtils.setField(post, "id", 42L);
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/posts/42/edit\"")))
                .andExpect(content().string(containsString("編集")));
    }

    @Test
    @DisplayName("投稿一覧_投稿ごとに詳細リンクを表示する")
    void list_displaysDetailLinkForEachPost() throws Exception {
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T01:00:00Z"));
        ReflectionTestUtils.setField(post, "id", 42L);
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/posts/42\"")))
                .andExpect(content().string(containsString("詳細")));
    }

    @Test
    @DisplayName("投稿一覧_いいね数_各投稿にグッドマークと件数を表示する")
    void list_displaysLikeMarkAndCountForEachPost() throws Exception {
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T01:00:00Z"));
        ReflectionTestUtils.setField(post, "id", 42L);
        given(postService.latest()).willReturn(List.of(post));
        given(postService.countLikesByPostIds(List.of(42L))).willReturn(Map.of(42L, 3L));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("likeCounts", Map.of(42L, 3L)))
                .andExpect(content().string(containsString("👍")))
                .andExpect(content().string(containsString("👍 3")));
    }

    @Test
    @DisplayName("投稿一覧_いいねフォーム_posts_id_likesへPOST送信し一覧へ戻る")
    void list_hasLikeFormPostingToLikesAndReturningList() throws Exception {
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T01:00:00Z"));
        ReflectionTestUtils.setField(post, "id", 42L);
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(containsString("action=\"/posts/42/likes\"")))
                .andExpect(content().string(containsString("name=\"redirectTo\"")))
                .andExpect(content().string(containsString("value=\"/posts\"")))
                .andExpect(content().string(containsString("class=\"post__like-mark\"")));
    }

    @Test
    @DisplayName("投稿一覧_メニュー_削除一覧リンクを表示する")
    void list_menuShowsDeletedListLink() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/posts/deleted\"")))
                .andExpect(content().string(containsString("削除一覧")));
    }

    @Test
    @DisplayName("削除一覧_GET_posts_deleted_削除済み投稿を一覧ビューに渡す")
    void deletedList_showsDeletedPosts() throws Exception {
        Post post = postWithTags("alice", "#java 削除済み投稿", List.of("java"));
        ReflectionTestUtils.setField(post, "id", 42L);
        post.update("alice", "#java 削除済み投稿", "purple", Instant.parse("2026-05-24T02:00:00Z"));
        post.delete(Instant.parse("2026-05-25T00:00:00Z"));
        given(postService.deleted()).willReturn(List.of(post));

        mockMvc.perform(get("/posts/deleted"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of(post)))
                .andExpect(model().attribute("deletedMode", true))
                .andExpect(content().string(containsString("削除済み一覧")))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("#java 削除済み投稿")))
                .andExpect(content().string(containsString("● purple")))
                .andExpect(content().string(containsString("登録日:")))
                .andExpect(content().string(containsString("2026-05-23 10:00")))
                .andExpect(content().string(containsString("更新日:")))
                .andExpect(content().string(containsString("2026-05-24 11:00")))
                .andExpect(content().string(containsString("href=\"/tags/java\"")));

        verify(postService).deleted();
        verify(postService, never()).latest();
        verify(postService, never()).countLikesByPostIds(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    @DisplayName("削除一覧_削除済み投稿_いいねボタンと詳細編集リンクを表示しない")
    void deletedList_hidesLikeButtonAndPostLinks() throws Exception {
        Post post = new Post("alice", "削除済み投稿", Instant.parse("2026-05-23T01:00:00Z"));
        ReflectionTestUtils.setField(post, "id", 42L);
        post.delete(Instant.parse("2026-05-24T00:00:00Z"));
        given(postService.deleted()).willReturn(List.of(post));

        String html = mockMvc.perform(get("/posts/deleted"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).doesNotContain("action=\"/posts/42/likes\"");
        assertThat(html).doesNotContain("class=\"post__like-mark\"");
        assertThat(html).doesNotContain("href=\"/posts/42\"");
        assertThat(html).doesNotContain("href=\"/posts/42/edit\"");
    }

    @Test
    @DisplayName("投稿作成フォーム_GET_posts_new_postFormをビューに渡す")
    void newForm_setsPostForm() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"));
    }

    @Test
    @DisplayName("投稿作成フォーム_入力欄_投稿者名と内容を入力できる")
    void newForm_hasAuthorAndBodyFields() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"author\"")))
                .andExpect(content().string(containsString("type=\"text\"")))
                .andExpect(content().string(containsString("name=\"body\"")))
                .andExpect(content().string(containsString("<textarea")));
    }

    @Test
    @DisplayName("投稿作成フォーム_アバター色_selectで選択肢とデフォルトblueを表示する")
    void newForm_hasAvatarColorSelectAndDefaultBlue() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("avatarColors", List.of("red", "blue", "green", "yellow", "purple")))
                .andExpect(model().attribute("postForm",
                        org.hamcrest.Matchers.hasProperty("avatarColor", org.hamcrest.Matchers.equalTo("blue"))))
                .andExpect(content().string(containsString("name=\"avatarColor\"")))
                .andExpect(content().string(containsString("<select")))
                .andExpect(content().string(containsString("value=\"red\"")))
                .andExpect(content().string(containsString("value=\"blue\"")))
                .andExpect(content().string(containsString("value=\"green\"")))
                .andExpect(content().string(containsString("value=\"yellow\"")))
                .andExpect(content().string(containsString("value=\"purple\"")));
    }

    @Test
    @DisplayName("投稿作成フォーム_登録ボタン_POST_postsへ送信する")
    void newForm_submitButtonPostsToPosts() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(containsString("action=\"/posts\"")))
                .andExpect(content().string(containsString("登録")));
    }

    @Test
    @DisplayName("投稿作成フォーム_キャンセルボタン_postsへ戻る")
    void newForm_cancelButtonReturnsPosts() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/posts\"")))
                .andExpect(content().string(containsString("キャンセル")));
    }

    @Test
    @DisplayName("投稿作成_author空白のみ_posts_formを再表示し入力値を保持する")
    void create_whenAuthorBlank_redisplaysFormAndKeepsInput() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "本文です")
                        .param("avatarColor", "green"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(model().attribute("postForm",
                        org.hamcrest.Matchers.hasProperty("author", org.hamcrest.Matchers.equalTo("   "))))
                .andExpect(model().attribute("postForm",
                        org.hamcrest.Matchers.hasProperty("avatarColor", org.hamcrest.Matchers.equalTo("green"))))
                .andExpect(content().string(containsString("投稿者名を入力してください")));

        verify(postService, never()).create(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("投稿作成_body空白のみ_posts_formを再表示する")
    void create_whenBodyBlank_redisplaysForm() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "   ")
                        .param("avatarColor", "blue"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")));

        verify(postService, never()).create(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("投稿作成_author31文字_posts_formを再表示する")
    void create_whenAuthorTooLong_redisplaysForm() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "a".repeat(31))
                        .param("body", "本文です")
                        .param("avatarColor", "blue"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名は 30 文字以内で入力してください")));

        verify(postService, never()).create(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("投稿作成_body281文字_posts_formを再表示する")
    void create_whenBodyTooLong_redisplaysForm() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "a".repeat(281))
                        .param("avatarColor", "blue"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文は 280 文字以内で入力してください")));

        verify(postService, never()).create(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("投稿作成_入力正常_postsへリダイレクトしアバター色を保存する")
    void create_whenValid_redirectsToPostsAndCreatesPostWithAvatarColor() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "本文です")
                        .param("avatarColor", "purple"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "本文です", "purple");
    }

    @Test
    @DisplayName("投稿詳細_存在するid_posts_detailを表示しpostをビューに渡す")
    void detail_whenPostExists_showsDetailAndSetsPost() throws Exception {
        Post post = new Post("alice", "詳細本文です", "green", Instant.parse("2026-05-23T01:00:00Z"));
        given(postService.findById(42L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/42"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("green")))
                .andExpect(content().string(containsString("詳細本文です")))
                .andExpect(content().string(containsString("2026-05-23 10:00")));
    }

    @Test
    @DisplayName("投稿詳細_タグあり_タグ一覧とタグ別一覧リンクを表示する")
    void detail_whenPostHasTags_showsTagLinks() throws Exception {
        Post post = new Post("alice", "#java #spring 本文です", Instant.parse("2026-05-23T01:00:00Z"));
        given(postService.findById(42L)).willReturn(Optional.of(post));
        given(postService.findTagsByPostId(42L)).willReturn(List.of(new Tag("java"), new Tag("spring")));

        mockMvc.perform(get("/posts/42"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("tags"))
                .andExpect(content().string(containsString("#java")))
                .andExpect(content().string(containsString("#spring")))
                .andExpect(content().string(containsString("href=\"/tags/java\"")))
                .andExpect(content().string(containsString("href=\"/tags/spring\"")));
    }

    @Test
    @DisplayName("投稿詳細_存在しないid_404を返す")
    void detail_whenPostDoesNotExist_returns404() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿詳細_一覧に戻るボタン_postsへ遷移する")
    void detail_hasBackToListButton() throws Exception {
        Post post = new Post("alice", "詳細本文です", Instant.parse("2026-05-23T01:00:00Z"));
        given(postService.findById(42L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/42"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/posts\"")))
                .andExpect(content().string(containsString("一覧に戻る")));
    }

    @Test
    @DisplayName("投稿詳細_編集リンク_posts_id_editへ遷移する")
    void detail_hasEditLink() throws Exception {
        Post post = new Post("alice", "詳細本文です", Instant.parse("2026-05-23T01:00:00Z"));
        given(postService.findById(42L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/42"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/posts/42/edit\"")))
                .andExpect(content().string(containsString("編集")));
    }

    @Test
    @DisplayName("投稿詳細_いいね数_ビューに表示する")
    void detail_displaysLikeCount() throws Exception {
        Post post = new Post("alice", "詳細本文です", Instant.parse("2026-05-23T01:00:00Z"));
        given(postService.findById(42L)).willReturn(Optional.of(post));
        given(postService.countLikes(42L)).willReturn(3L);

        mockMvc.perform(get("/posts/42"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("likeCount", 3L))
                .andExpect(content().string(containsString("👍 3")));
    }

    @Test
    @DisplayName("投稿詳細_グッドマーク_posts_id_likesへPOST送信する")
    void detail_hasLikeMarkPostingToLikes() throws Exception {
        Post post = new Post("alice", "詳細本文です", Instant.parse("2026-05-23T01:00:00Z"));
        given(postService.findById(42L)).willReturn(Optional.of(post));
        given(postService.countLikes(42L)).willReturn(5L);

        mockMvc.perform(get("/posts/42"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(containsString("action=\"/posts/42/likes\"")))
                .andExpect(content().string(containsString("class=\"post__like-mark\"")))
                .andExpect(content().string(containsString("👍")))
                .andExpect(content().string(containsString("👍 5")));
    }

    @Test
    @DisplayName("投稿詳細_削除ボタン_posts_id_deleteへPOST送信する")
    void detail_hasDeleteButtonPostingToDelete() throws Exception {
        Post post = new Post("alice", "詳細本文です", Instant.parse("2026-05-23T01:00:00Z"));
        given(postService.findById(42L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/42"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(containsString("action=\"/posts/42/delete\"")))
                .andExpect(content().string(containsString("削除")));
    }

    @Test
    @DisplayName("いいね_POST_posts_id_likes_likesCountを増やして詳細へリダイレクトする")
    void like_whenPostExists_incrementsLikeAndRedirectsToDetail() throws Exception {
        given(postService.incrementLike(42L)).willReturn(Optional.of(4L));

        mockMvc.perform(post("/posts/42/likes"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/42"));

        verify(postService).incrementLike(42L);
    }

    @Test
    @DisplayName("いいね_POST_posts_id_likes_一覧から呼び出した場合は一覧へリダイレクトする")
    void like_whenRedirectToPosts_redirectsToList() throws Exception {
        given(postService.incrementLike(42L)).willReturn(Optional.of(4L));

        mockMvc.perform(post("/posts/42/likes")
                        .param("redirectTo", "/posts"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).incrementLike(42L);
    }

    @Test
    @DisplayName("いいね_POST_posts_id_likes_存在しないidは404を返す")
    void like_whenPostDoesNotExist_returns404() throws Exception {
        given(postService.incrementLike(999L)).willReturn(Optional.empty());

        mockMvc.perform(post("/posts/999/likes"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿削除_POST_posts_id_delete_削除して一覧へリダイレクトする")
    void delete_whenPostExists_deletesAndRedirectsToList() throws Exception {
        given(postService.delete(42L)).willReturn(true);

        mockMvc.perform(post("/posts/42/delete"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).delete(42L);
    }

    @Test
    @DisplayName("投稿削除_POST_posts_id_delete_存在しないidは404を返す")
    void delete_whenPostDoesNotExist_returns404() throws Exception {
        given(postService.delete(999L)).willReturn(false);

        mockMvc.perform(post("/posts/999/delete"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿編集フォーム_存在するid_既存値をフォームに表示する")
    void editForm_whenPostExists_showsFormWithExistingValues() throws Exception {
        Post post = new Post("alice", "編集前本文です", "purple", Instant.parse("2026-05-23T01:00:00Z"));
        given(postService.findById(42L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/42/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm",
                        org.hamcrest.Matchers.hasProperty("author", org.hamcrest.Matchers.equalTo("alice"))))
                .andExpect(model().attribute("postForm",
                        org.hamcrest.Matchers.hasProperty("body", org.hamcrest.Matchers.equalTo("編集前本文です"))))
                .andExpect(model().attribute("postForm",
                        org.hamcrest.Matchers.hasProperty("avatarColor", org.hamcrest.Matchers.equalTo("purple"))))
                .andExpect(content().string(containsString("value=\"alice\"")))
                .andExpect(content().string(containsString("編集前本文です")))
                .andExpect(content().string(containsString("value=\"purple\" selected=\"selected\"")))
                .andExpect(content().string(containsString("action=\"/posts/42\"")))
                .andExpect(content().string(containsString("href=\"/posts/42\"")));
    }

    @Test
    @DisplayName("投稿編集フォーム_存在しないid_404を返す")
    void editForm_whenPostDoesNotExist_returns404() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999/edit"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿編集_author空白のみ_posts_formを再表示し入力値を保持する")
    void update_whenAuthorBlank_redisplaysFormAndKeepsInput() throws Exception {
        mockMvc.perform(post("/posts/42")
                        .param("author", "   ")
                        .param("body", "入力中の本文です")
                        .param("avatarColor", "yellow"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(model().attribute("postForm",
                        org.hamcrest.Matchers.hasProperty("author", org.hamcrest.Matchers.equalTo("   "))))
                .andExpect(model().attribute("postForm",
                        org.hamcrest.Matchers.hasProperty("body", org.hamcrest.Matchers.equalTo("入力中の本文です"))))
                .andExpect(model().attribute("postForm",
                        org.hamcrest.Matchers.hasProperty("avatarColor", org.hamcrest.Matchers.equalTo("yellow"))))
                .andExpect(content().string(containsString("投稿者名を入力してください")))
                .andExpect(content().string(containsString("action=\"/posts/42\"")))
                .andExpect(content().string(containsString("入力中の本文です")));

        verify(postService, never()).update(
                org.mockito.ArgumentMatchers.anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("投稿編集_body空白のみ_posts_formを再表示する")
    void update_whenBodyBlank_redisplaysForm() throws Exception {
        mockMvc.perform(post("/posts/42")
                        .param("author", "alice")
                        .param("body", "   ")
                        .param("avatarColor", "blue"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")));

        verify(postService, never()).update(
                org.mockito.ArgumentMatchers.anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("投稿編集_author31文字_posts_formを再表示する")
    void update_whenAuthorTooLong_redisplaysForm() throws Exception {
        mockMvc.perform(post("/posts/42")
                        .param("author", "a".repeat(31))
                        .param("body", "本文です")
                        .param("avatarColor", "blue"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者名は 30 文字以内で入力してください")));

        verify(postService, never()).update(
                org.mockito.ArgumentMatchers.anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("投稿編集_body281文字_posts_formを再表示する")
    void update_whenBodyTooLong_redisplaysForm() throws Exception {
        mockMvc.perform(post("/posts/42")
                        .param("author", "alice")
                        .param("body", "a".repeat(281))
                        .param("avatarColor", "blue"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文は 280 文字以内で入力してください")));

        verify(postService, never()).update(
                org.mockito.ArgumentMatchers.anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("投稿編集_入力正常_詳細へリダイレクトしアバター色を更新する")
    void update_whenValid_redirectsToDetailAndUpdatesPostWithAvatarColor() throws Exception {
        given(postService.update(42L, "alice", "更新後本文です", "green"))
                .willReturn(Optional.of(new Post("alice", "更新後本文です",
                        "green", Instant.parse("2026-05-23T01:00:00Z"))));

        mockMvc.perform(post("/posts/42")
                        .param("author", "alice")
                        .param("body", "更新後本文です")
                        .param("avatarColor", "green"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/42"));

        verify(postService).update(42L, "alice", "更新後本文です", "green");
    }

    private Post postWithTags(String author, String body, List<String> tagNames) {
        Post post = new Post(author, body, Instant.parse("2026-05-23T01:00:00Z"));
        List<PostTag> postTags = tagNames.stream()
                .map(tagName -> new PostTag(post, new Tag(tagName)))
                .toList();
        ReflectionTestUtils.setField(post, "postTags", postTags);
        return post;
    }
}
