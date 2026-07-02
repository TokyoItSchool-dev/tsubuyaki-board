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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
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
    @DisplayName("投稿一覧_GET_posts_model_postsにListを積みposts_listビューを返す")
    void 投稿一覧_GETPosts_modelPostsにListを積みPostsListビューを返す() throws Exception {
        List<Post> latestPosts = List.of(
                new Post("alice", "新しい投稿", LocalDateTime.parse("2026-06-26T10:00:00")),
                new Post("bob", "古い投稿", LocalDateTime.parse("2026-06-26T09:00:00")));
        given(postService.search(null)).willReturn(latestPosts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", sameInstance(latestPosts)));
    }

    @Test
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void 投稿一覧_0件の場合_まだ投稿はありませんを表示する() throws Exception {
        given(postService.search(null)).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsスラッシュへGETリクエストする")
    void 投稿一覧_更新ボタン_押すとPostsスラッシュへGetリクエストする() throws Exception {
        given(postService.search(null)).willReturn(Collections.emptyList());

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).contains("method=\"get\"", "action=\"/posts/\"", ">更新</button>");

        mockMvc.perform(get("/posts/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"));
    }

    @Test
    @DisplayName("投稿一覧_投稿カード_投稿者_内容_投稿日の順に表示する")
    void 投稿一覧_投稿カード_投稿者_内容_投稿日の順に表示する() throws Exception {
        given(postService.search(null)).willReturn(List.of(
                new Post("alice", "順序を確認する本文", LocalDateTime.parse("2026-06-26T10:00:00"))));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("順序を確認する本文"));
        assertThat(html.indexOf("順序を確認する本文")).isLessThan(html.indexOf("2026-06-26"));
    }

    @Test
    @DisplayName("投稿一覧_投稿カード_詳細画面へのリンクを表示する")
    void 投稿一覧_投稿カード_詳細画面へのリンクを表示する() throws Exception {
        Post post = new Post("alice", "リンクを確認する本文", LocalDateTime.parse("2026-06-26T10:00:00"));
        ReflectionTestUtils.setField(post, "id", 10L);
        given(postService.search(null)).willReturn(List.of(post));

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).contains("href=\"/posts/10\"");
    }

    @Test
    @DisplayName("投稿検索_一覧から詳細へ遷移する場合_検索条件をリンクに保持する")
    void 投稿検索_一覧から詳細へ遷移する場合_検索条件をリンクに保持する() throws Exception {
        Post post = new Post("alice", "検索対象です", LocalDateTime.parse("2026-06-26T10:00:00"));
        ReflectionTestUtils.setField(post, "id", 10L);
        given(postService.search("検索")).willReturn(List.of(post));

        MvcResult result = mockMvc.perform(get("/posts").param("q", "検索"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).contains("href=\"/posts/10?q=%E6%A4%9C%E7%B4%A2\"");
    }

    @Test
    @DisplayName("投稿検索_一覧から新規投稿へ遷移する場合_検索条件をリンクに保持する")
    void 投稿検索_一覧から新規投稿へ遷移する場合_検索条件をリンクに保持する() throws Exception {
        given(postService.search("検索")).willReturn(Collections.emptyList());

        MvcResult result = mockMvc.perform(get("/posts").param("q", "検索"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).contains("href=\"/posts/new?q=%E6%A4%9C%E7%B4%A2\"");
    }

    @Test
    @DisplayName("投稿検索_q指定あり_検索結果をmodel_postsに設定しキーワードを表示する")
    void 投稿検索_q指定あり_検索結果をModelPostsに設定しキーワードを表示する() throws Exception {
        List<Post> searchResults = List.of(
                new Post("alice", "検索対象です", LocalDateTime.parse("2026-06-26T10:00:00")));
        given(postService.search("検索")).willReturn(searchResults);

        mockMvc.perform(get("/posts").param("q", "検索"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", sameInstance(searchResults)))
                .andExpect(model().attribute("q", "検索"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"検索\"")));
    }

    @Test
    @DisplayName("投稿検索_q未指定_通常の投稿一覧をmodel_postsに設定する")
    void 投稿検索_q未指定_通常の投稿一覧をModelPostsに設定する() throws Exception {
        List<Post> latestPosts = List.of(
                new Post("alice", "通常一覧です", LocalDateTime.parse("2026-06-26T10:00:00")));
        given(postService.search(null)).willReturn(latestPosts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", sameInstance(latestPosts)));
    }

    @Test
    @DisplayName("投稿検索_q空文字_通常の投稿一覧をmodel_postsに設定する")
    void 投稿検索_q空文字_通常の投稿一覧をModelPostsに設定する() throws Exception {
        List<Post> latestPosts = List.of(
                new Post("alice", "通常一覧です", LocalDateTime.parse("2026-06-26T10:00:00")));
        given(postService.search("")).willReturn(latestPosts);

        mockMvc.perform(get("/posts").param("q", ""))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", sameInstance(latestPosts)));
    }

    @Test
    @DisplayName("投稿検索_一致なし_該当する投稿はありませんを表示する")
    void 投稿検索_一致なし_該当する投稿はありませんを表示する() throws Exception {
        given(postService.search("該当なし")).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts").param("q", "該当なし"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("該当する投稿はありません")));
    }

    @Test
    @DisplayName("投稿検索_検索ボックス_一覧画面上部に表示する")
    void 投稿検索_検索ボックス_一覧画面上部に表示する() throws Exception {
        given(postService.search(null)).willReturn(Collections.emptyList());

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).contains("name=\"q\"", "type=\"search\"", ">検索</button>");
        assertThat(html.indexOf("name=\"q\"")).isLessThan(html.indexOf("class=\"reload-form\""));
    }

    @Test
    @DisplayName("投稿フォーム_初期表示_posts_formビューと空のpostFormをmodelに設定する")
    void 投稿フォーム_初期表示_postsFormビューと空のPostFormをModelに設定する() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"))
                .andReturn();

        Object postFormAttribute = result.getModelAndView().getModel().get("postForm");
        assertThat(postFormAttribute).isInstanceOf(PostForm.class);

        PostForm postForm = (PostForm) postFormAttribute;
        assertThat(postForm.getAuthor()).isNull();
        assertThat(postForm.getBody()).isNull();
    }

    @Test
    @DisplayName("投稿フォーム_検索条件付き初期表示_一覧へ戻るリンクと投稿フォームに検索条件を保持する")
    void 投稿フォーム_検索条件付き初期表示_一覧へ戻るリンクと投稿フォームに検索条件を保持する() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/new").param("q", "検索"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("q", "検索"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html)
                .contains("href=\"/posts?q=%E6%A4%9C%E7%B4%A2\"")
                .contains("action=\"/posts?q=%E6%A4%9C%E7%B4%A2\"");
    }

    @Test
    @DisplayName("投稿フォーム_初期表示_required属性を出力しない")
    void 投稿フォーム_初期表示_required属性を出力しない() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).doesNotContain(" required", "required=");
    }

    @Test
    @DisplayName("投稿詳細_存在するIDの場合_posts_detailビューとpostをmodelに設定する")
    void 投稿詳細_存在するIDの場合_postsDetailビューとPostをModelに設定する() throws Exception {
        Post post = new Post("alice", "詳細に表示する本文", LocalDateTime.parse("2026-06-26T10:00:00"));
        ReflectionTestUtils.setField(post, "id", 10L);
        given(postService.findById(10L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", sameInstance(post)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("10")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("alice")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("詳細に表示する本文")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2026-06-26 10:00")));
    }

    @Test
    @DisplayName("投稿詳細_検索条件付き表示_一覧へ戻るリンクに検索条件を保持する")
    void 投稿詳細_検索条件付き表示_一覧へ戻るリンクに検索条件を保持する() throws Exception {
        Post post = new Post("alice", "詳細に表示する本文", LocalDateTime.parse("2026-06-26T10:00:00"));
        ReflectionTestUtils.setField(post, "id", 10L);
        given(postService.findById(10L)).willReturn(Optional.of(post));

        MvcResult result = mockMvc.perform(get("/posts/10").param("q", "検索"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("q", "検索"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).contains("href=\"/posts?q=%E6%A4%9C%E7%B4%A2\"");
    }

    @Test
    @DisplayName("投稿詳細_存在しないIDの場合_404を返す")
    void 投稿詳細_存在しないIDの場合_404を返す() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿登録_authorが空の場合_エラーとなりフォームを再表示する")
    void 投稿登録_authorが空の場合_エラーとなりフォームを再表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "")
                        .param("body", "本文です"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿者名を入力してください")));
    }

    @Test
    @DisplayName("投稿登録_authorが空白のみの場合_エラーとなる")
    void 投稿登録_authorが空白のみの場合_エラーとなる() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "本文です"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿者名を入力してください")));

        then(postService).should(never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("投稿登録_authorが全角空白のみの場合_エラーとなる")
    void 投稿登録_authorが全角空白のみの場合_エラーとなる() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "　　")
                        .param("body", "本文です"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿者名を入力してください")));

        then(postService).should(never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("投稿登録_authorが30文字の場合_エラーにならずリダイレクトする")
    void 投稿登録_authorが30文字の場合_エラーにならずリダイレクトする() throws Exception {
        String author = "a".repeat(30);

        mockMvc.perform(post("/posts")
                        .param("author", author)
                        .param("body", "本文です"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    @DisplayName("投稿登録_authorが31文字の場合_エラーとなる")
    void 投稿登録_authorが31文字の場合_エラーとなる() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "a".repeat(31))
                        .param("body", "本文です"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿者名は 30 文字以内で入力してください")));
    }

    @Test
    @DisplayName("投稿登録_bodyが空の場合_エラーとなりフォームを再表示する")
    void 投稿登録_bodyが空の場合_エラーとなりフォームを再表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文を入力してください")));
    }

    @Test
    @DisplayName("投稿登録_bodyが空白のみの場合_エラーとなる")
    void 投稿登録_bodyが空白のみの場合_エラーとなる() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文を入力してください")));

        then(postService).should(never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("投稿登録_bodyが全角空白のみの場合_エラーとなる")
    void 投稿登録_bodyが全角空白のみの場合_エラーとなる() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "　　"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文を入力してください")));

        then(postService).should(never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("投稿登録_bodyが280文字の場合_エラーにならずリダイレクトする")
    void 投稿登録_bodyが280文字の場合_エラーにならずリダイレクトする() throws Exception {
        String body = "b".repeat(280);

        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", body))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    @DisplayName("投稿登録_bodyが281文字の場合_エラーとなる")
    void 投稿登録_bodyが281文字の場合_エラーとなる() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "b".repeat(281)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文は 280 文字以内で入力してください")));
    }

    @Test
    @DisplayName("投稿登録_正常な入力の場合_投稿を保存してpostsへリダイレクトする")
    void 投稿登録_正常な入力の場合_投稿を保存してPostsへリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "今日の共有です"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().create("alice", "今日の共有です");
    }

    @Test
    @DisplayName("投稿登録_検索条件付き正常入力の場合_検索条件をクリアして一覧へリダイレクトする")
    void 投稿登録_検索条件付き正常入力の場合_検索条件をクリアして一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("q", "検索")
                        .param("author", "alice")
                        .param("body", "今日の共有です"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().create("alice", "今日の共有です");
    }

    @Test
    @DisplayName("投稿登録_バリデーションエラー時_入力値postFormを保持する")
    void 投稿登録_バリデーションエラー時_入力値PostFormを保持する() throws Exception {
        MvcResult result = mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"))
                .andReturn();

        PostForm postForm = (PostForm) result.getModelAndView().getModel().get("postForm");
        assertThat(postForm.getAuthor()).isEqualTo("alice");
        assertThat(postForm.getBody()).isEmpty();
    }

    @Test
    @DisplayName("投稿登録_検索条件付きバリデーションエラー時_検索条件を保持する")
    void 投稿登録_検索条件付きバリデーションエラー時_検索条件を保持する() throws Exception {
        MvcResult result = mockMvc.perform(post("/posts")
                        .param("q", "検索")
                        .param("author", "alice")
                        .param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("q", "検索"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).contains("action=\"/posts?q=%E6%A4%9C%E7%B4%A2\"");
    }
}
