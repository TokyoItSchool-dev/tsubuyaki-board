package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    @AfterEach
    void cleanDatabase() {
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void list_whenEmpty_showsEmptyMessage() throws Exception {
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_51件以上ある場合_新着50件だけ表示する")
    void list_whenMoreThan50Posts_showsOnlyLatest50() throws Exception {
        Instant base = Instant.parse("2026-06-26T09:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int index = 0; index < 51; index++) {
            posts.add(new Post("user" + index, "body" + index, base.plusSeconds(index)));
        }
        postRepository.saveAll(posts);

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("body50")))
                .andExpect(content().string(containsString("body1")))
                .andExpect(content().string(not(containsString("body0"))))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("body50");
        assertThat(html).doesNotContain("body0");
    }

    @Test
    @DisplayName("投稿検索_検索結果0件_検索結果0件メッセージを検索テキストボックスの上に表示する")
    void list_whenSearchResultIsEmpty_showsNoSearchResultsMessageAboveSearchInput() throws Exception {
        postRepository.save(new Post("alice", "朝会メモ", Instant.parse("2026-06-26T09:00:00Z")));

        MvcResult result = mockMvc.perform(get("/posts").param("q", "存在しない"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(content().string(containsString("検索結果が0件です。")))
                .andExpect(content().string(not(containsString("まだ投稿はありません"))))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html.indexOf("検索結果が0件です。")).isLessThan(html.indexOf("name=\"q\""));
        assertThat(html).contains("value=\"存在しない\"");
        assertThat(html).contains("0件");
        assertThat(html).doesNotContain("朝会メモ");
    }

    @Test
    @DisplayName("投稿検索_検索テキスト未入力_初期表示と同じく新着50件を表示する")
    void list_whenSearchQueryIsBlank_showsLatest50Posts() throws Exception {
        Instant base = Instant.parse("2026-06-26T09:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int index = 0; index < 51; index++) {
            posts.add(new Post("user" + index, "body" + index, base.plusSeconds(index)));
        }
        postRepository.saveAll(posts);

        MvcResult result = mockMvc.perform(get("/posts").param("q", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("body50")))
                .andExpect(content().string(containsString("body1")))
                .andExpect(content().string(not(containsString("body0"))))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("50件");
        assertThat(html.indexOf("一覧</a>")).isLessThan(html.indexOf("name=\"q\""));
        assertThat(html.indexOf("name=\"q\"")).isLessThan(html.indexOf("更新</button>"));
        assertThat(html.indexOf("50件")).isLessThan(html.indexOf("更新</button>"));
    }

    @Test
    @DisplayName("投稿検索_検索語あり_本文を前後あいまい検索する")
    void list_whenSearchQueryIsPresent_showsPostsContainingQueryInBody() throws Exception {
        Instant base = Instant.parse("2026-06-26T09:00:00Z");
        postRepository.save(new Post("alice", "朝会メモ", base));
        postRepository.save(new Post("bob", "週次の朝会で共有", base.plusSeconds(1)));
        postRepository.save(new Post("carol", "ランチ予定", base.plusSeconds(2)));

        MvcResult result = mockMvc.perform(get("/posts").param("q", "朝会"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("朝会メモ")))
                .andExpect(content().string(containsString("週次の朝会で共有")))
                .andExpect(content().string(not(containsString("ランチ予定"))))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("2件");
        assertThat(html).contains("value=\"朝会\"");
        assertThat(html.indexOf("週次の朝会で共有")).isLessThan(html.indexOf("朝会メモ"));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsへGETリクエストする")
    void list_hasRefreshButtonRequestingPosts() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("更新")))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("method=\"get\"");
        assertThat(html).contains("action=\"/posts\"");
        assertThat(html).contains("<button type=\"submit\">更新</button>");
    }

    @Test
    @DisplayName("投稿一覧_投稿_投稿者内容投稿日の順に表示する")
    void list_showsAuthorBodyCreatedAtInOrder() throws Exception {
        postRepository.save(new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("hello"));
        assertThat(html.indexOf("hello")).isLessThan(html.indexOf("class=\"post__created-at\""));
    }

    @Test
    @DisplayName("投稿一覧_投稿セル_クリックすると詳細画面へ遷移するリンクになっている")
    void list_postCellLinksToDetail() throws Exception {
        Post saved = postRepository.save(new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/posts/" + saved.getId() + "?clientHash=")))
                .andExpect(content().string(containsString("class=\"post post--link\"")));
    }

    @Test
    @DisplayName("投稿一覧_clientHashがない場合_生成して詳細リンクと更新フォームに積む")
    void list_whenClientHashIsMissing_generatesAndPassesClientHash() throws Exception {
        Post saved = postRepository.save(new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));

        MvcResult result = mockMvc.perform(get("/posts")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.10");
                            return request;
                        })
                        .header("User-Agent", "JUnit Browser"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("clientHash"))
                .andReturn();

        String clientHash = (String) result.getModelAndView().getModel().get("clientHash");
        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(clientHash).hasSize(8);
        assertThat(html).contains("href=\"/posts/" + saved.getId() + "?clientHash=" + clientHash + "\"");
        assertThat(html).contains("name=\"clientHash\"");
        assertThat(html).contains("value=\"" + clientHash + "\"");
    }

    @Test
    @DisplayName("投稿一覧_clientHashがある場合_その値を詳細リンクと更新フォームに引き回す")
    void list_whenClientHashIsPresent_passesClientHash() throws Exception {
        Post saved = postRepository.save(new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));

        MvcResult result = mockMvc.perform(get("/posts").param("clientHash", "facefeed"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("clientHash", "facefeed"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("href=\"/posts/" + saved.getId() + "?clientHash=facefeed\"");
        assertThat(html).contains("name=\"clientHash\"");
        assertThat(html).contains("value=\"facefeed\"");
    }

    @Test
    @DisplayName("投稿詳細_存在するid_posts_detailビューに投稿を表示する")
    void detail_whenFound_rendersDetailView() throws Exception {
        Post saved = postRepository.save(new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));

        mockMvc.perform(get("/posts/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("clientHash"))
                .andExpect(content().string(containsString("<title>投稿詳細 - 社内つぶやきボード</title>")))
                .andExpect(content().string(containsString("一覧に戻る")))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("hello")));
    }

    @Test
    @DisplayName("投稿詳細_存在しないid_404を返す")
    void detail_whenNotFound_returns404() throws Exception {
        mockMvc.perform(get("/posts/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("いいね_押されたら_いいね数が加算される")
    void like_whenPressed_incrementsLikeCount() throws Exception {
        Post saved = postRepository.save(new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));

        mockMvc.perform(post("/posts/{id}/likes", saved.getId())
                        .param("clientHash", "facefeed"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/" + saved.getId() + "?clientHash=facefeed"));

        mockMvc.perform(get("/posts/{id}", saved.getId()).param("clientHash", "facefeed"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("いいね 1")));
    }

    @Test
    @DisplayName("いいね_同じclientHashでもう一度押されたら_いいね数が減算される")
    void like_whenPressedAgainBySameClient_decrementsLikeCount() throws Exception {
        Post saved = postRepository.save(new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));

        mockMvc.perform(post("/posts/{id}/likes", saved.getId())
                        .param("clientHash", "facefeed"))
                .andExpect(status().isFound());
        mockMvc.perform(post("/posts/{id}/likes", saved.getId())
                        .param("clientHash", "facefeed"))
                .andExpect(status().isFound());

        mockMvc.perform(get("/posts/{id}", saved.getId()).param("clientHash", "facefeed"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("いいね 0")));
    }

    @Test
    @DisplayName("いいね_別clientHashパラメータで押されたら_別ユーザとして管理される")
    void like_whenPressedWithDifferentClientHashParameters_countsSeparately() throws Exception {
        Post saved = postRepository.save(new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));

        mockMvc.perform(post("/posts/{id}/likes", saved.getId())
                        .param("clientHash", "facefeed"))
                .andExpect(status().isFound());
        mockMvc.perform(post("/posts/{id}/likes", saved.getId())
                        .param("clientHash", "cafebabe"))
                .andExpect(status().isFound());

        mockMvc.perform(get("/posts/{id}", saved.getId()).param("clientHash", "facefeed"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("いいね 2")));
    }

    @Test
    @DisplayName("投稿詳細_clientHashがある場合_いいねフォームと戻るリンクに引き回す")
    void detail_whenClientHashIsPresent_passesClientHashToLikeFormAndBackLink() throws Exception {
        Post saved = postRepository.save(new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));

        MvcResult result = mockMvc.perform(get("/posts/{id}", saved.getId()).param("clientHash", "facefeed"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("clientHash", "facefeed"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("href=\"/posts?clientHash=facefeed\"");
        assertThat(html).contains("name=\"clientHash\"");
        assertThat(html).contains("value=\"facefeed\"");
    }

    @Test
    @DisplayName("投稿詳細_いいね表示_投稿日時の下に表示する")
    void detail_likeActions_areShownBelowCreatedAt() throws Exception {
        Post saved = postRepository.save(new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));

        MvcResult result = mockMvc.perform(get("/posts/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"post__likes\"")))
                .andExpect(content().string(containsString("いいね 0")))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html.indexOf("class=\"post__created-at\"")).isLessThan(html.indexOf("class=\"post__likes\""));
        assertThat(html.indexOf("class=\"post__likes\"")).isLessThan(html.indexOf("</article>"));
    }

    @Test
    @DisplayName("新規投稿フォーム_GET_posts_newはpostFormを積んでフォームビューを返す")
    void newForm_addsPostFormAndRendersFormView() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("class=\"post-form\"");
        assertThat(html).contains("class=\"form-row\"");
    }

    @Test
    @DisplayName("新規投稿フォーム_初期表示_投稿者と色は未入力未選択で表示する")
    void newForm_whenSessionIsEmpty_showsBlankAuthorAndNoSelectedAvatarColor() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("name=\"author\" value=\"\"");
        assertThat(html).contains("name=\"avatarColor\"");
        assertThat(html).contains("value=\"red\"");
        assertThat(html).contains("value=\"blue\"");
        assertThat(html).contains("value=\"yellow\"");
        assertThat(html).doesNotContain("checked=\"checked\"");
    }

    @Test
    @DisplayName("投稿一覧_色なし投稿_従来通り白背景の投稿として表示する")
    void list_whenPostHasNoAvatarColor_showsPostWithoutAvatarColorClass() throws Exception {
        postRepository.save(new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("hello")))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("class=\"post post--link\"");
        assertThat(html).doesNotContain("post--avatar-");
    }

    @Test
    @DisplayName("投稿登録_色を選択_一覧で選択色の淡い背景として表示する")
    void create_whenAvatarColorIsSelected_showsPostWithAvatarColorClassInList() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "hello")
                        .param("avatarColor", "blue"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("hello")))
                .andExpect(content().string(containsString("post--avatar-blue")));
    }

    @Test
    @DisplayName("投稿登録_同じ投稿者で色を変更_過去投稿も新しい投稿者色で表示する")
    void create_whenSameAuthorChangesAvatarColor_updatesPreviousPostsToNewAuthorColor() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "first")
                        .param("avatarColor", "red"))
                .andExpect(status().isFound());
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "second")
                        .param("avatarColor", "blue"))
                .andExpect(status().isFound());

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("first")))
                .andExpect(content().string(containsString("second")))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("post--avatar-blue");
        assertThat(html).doesNotContain("post--avatar-red");
    }

    @Test
    @DisplayName("投稿登録_同じ投稿者で色未選択_既存の投稿者色を引き継いで表示する")
    void create_whenSameAuthorDoesNotSelectAvatarColor_usesExistingAuthorColor() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "first")
                        .param("avatarColor", "red"))
                .andExpect(status().isFound());
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "second"))
                .andExpect(status().isFound());

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("first")))
                .andExpect(content().string(containsString("second")))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("post--avatar-red");
        assertThat(html).doesNotContain("class=\"post post--link\" href=");
    }

    @Test
    @DisplayName("投稿登録_色未選択_一覧で従来通り白背景の投稿として表示する")
    void create_whenAvatarColorIsNotSelected_showsPostWithoutAvatarColorClassInList() throws Exception {
        MvcResult result = mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "hello"))
                .andExpect(status().isFound())
                .andReturn();

        MvcResult listResult = mockMvc.perform(get("/posts").session((MockHttpSession) result.getRequest().getSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("hello")))
                .andReturn();

        String html = listResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("class=\"post post--link\"");
        assertThat(html).doesNotContain("post--avatar-");
    }

    @Test
    @DisplayName("投稿登録_投稿者と色を選択_再度新規投稿画面を開くと投稿者と色を復元する")
    void newForm_afterCreatingPostWithAuthorAndAvatarColor_restoresAuthorAndAvatarColorFromSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/posts")
                        .session(session)
                        .param("author", "alice")
                        .param("body", "hello")
                        .param("avatarColor", "yellow"))
                .andExpect(status().isFound());

        MvcResult result = mockMvc.perform(get("/posts/new").session(session))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("name=\"author\" value=\"alice\"");
        assertThat(html.indexOf("value=\"yellow\"")).isLessThan(html.indexOf("checked=\"checked\""));
    }

    @Test
    @DisplayName("投稿詳細_色なし投稿_従来通り白背景の投稿として表示する")
    void detail_whenPostHasNoAvatarColor_showsPostWithoutAvatarColorClass() throws Exception {
        Post saved = postRepository.save(new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));

        MvcResult result = mockMvc.perform(get("/posts/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("hello")))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("class=\"post\"");
        assertThat(html).doesNotContain("post--avatar-");
    }

    @Test
    @DisplayName("投稿詳細_色あり投稿_選択色の淡い背景として表示する")
    void detail_whenPostHasAvatarColor_showsPostWithAvatarColorClass() throws Exception {
        Post saved = postRepository.save(new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z"), "red"));

        mockMvc.perform(get("/posts/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("hello")))
                .andExpect(content().string(containsString("post--avatar-red")));
    }

    @Test
    @DisplayName("投稿登録_成功_302でpostsへリダイレクトし投稿を保存する")
    void create_whenValid_redirectsToPostsAndSavesPost() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "hello"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        List<Post> posts = postRepository.findAll();
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getAuthor()).isEqualTo("alice");
        assertThat(posts.get(0).getBody()).isEqualTo("hello");
        assertThat(posts.get(0).getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("投稿登録_必須エラー_項目名は必須ですを表示しフォームを再表示する")
    void create_whenRequiredError_showsMessagesAndRendersForm() throws Exception {
        MvcResult result = mockMvc.perform(post("/posts")
                        .param("author", "")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(content().string(containsString("投稿者は必須です。")))
                .andExpect(content().string(containsString("本文は必須です。")))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html.indexOf("一覧に戻る")).isLessThan(html.indexOf("投稿者は必須です。"));
        assertThat(html.indexOf("投稿者は必須です。")).isLessThan(html.indexOf("本文は必須です。"));
        assertThat(html.indexOf("本文は必須です。")).isLessThan(html.indexOf("<label for=\"author\">投稿者</label>"));
        assertThat(html).contains("class=\"form-errors\"");
        assertThat(postRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("投稿登録_全角スペースだけ_必須エラーを表示しフォームを再表示する")
    void create_whenOnlyFullWidthSpaces_showsRequiredMessagesAndRendersForm() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "　")
                        .param("body", "　　"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(content().string(containsString("投稿者は必須です。")))
                .andExpect(content().string(containsString("本文は必須です。")));

        assertThat(postRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("投稿登録_桁数エラー_項目名は桁数文字以内で入力してくださいを表示しフォームを再表示する")
    void create_whenLengthError_showsMessagesAndRendersForm() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "a".repeat(31))
                        .param("body", "b".repeat(281)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(content().string(containsString("投稿者は30文字以内で入力してください。")))
                .andExpect(content().string(containsString("本文は280文字以内で入力してください。")));

        assertThat(postRepository.findAll()).isEmpty();
    }
}
