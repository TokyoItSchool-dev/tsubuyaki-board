package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.ClientHashService;
import com.example.tsubuyaki.service.PostLikeService;
import com.example.tsubuyaki.service.PostNotFoundException;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.testsupport.PostTestFactory;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostControllerTest {

    // MockMvc で HTTP リクエストを疑似実行し、Controller とビューの振る舞いを検証する。
    @Autowired
    private MockMvc mockMvc;

    // WebMvcTest では Service をモックにして、Controller の責務だけを確認する。
    @MockitoBean
    private PostService postService;

    // いいね機能はServiceをモックにし、ControllerのHTTP処理だけを確認する。
    @MockitoBean
    private PostLikeService postLikeService;

    @MockitoBean
    private ClientHashService clientHashService;

    @Test
    @DisplayName("投稿一覧_0件の場合_空メッセージを表示しmodel.postsにListを積む")
    void 投稿一覧_0件の場合_空メッセージを表示しmodelPostsにListを積む() throws Exception {
        // Repository 由来の投稿が0件である状況を Service モックで作る。
        given(postService.search(null)).willReturn(Collections.emptyList());

        // GET /posts/ の結果として一覧ビュー、posts モデル、空メッセージが返ることを確認する。
        mockMvc.perform(get("/posts/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", instanceOf(List.class)))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsスラッシュにGETリクエストする")
    void 投稿一覧_更新ボタン_押すとpostsスラッシュにGETリクエストする() throws Exception {
        // 一覧画面を描画できるよう、投稿一覧は空として返す。
        given(postService.search(null)).willReturn(Collections.emptyList());

        // 検索フォームが GET /posts に向くフォームとして描画されることを確認する。
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<form class=\"search-form\" action=\"/posts\" method=\"get\"")))
                .andExpect(content().string(containsString("name=\"q\"")))
                .andExpect(content().string(containsString("<button type=\"submit\"")));
    }

    @Test
    @DisplayName("ダークモード_画面遷移先でもテーマトグルとlocalStorage制御を読み込む")
    void ダークモード_画面遷移先でもテーマトグルとlocalStorage制御を読み込む() throws Exception {
        // 一覧と詳細を描画できるよう、投稿を1件用意する。
        Post post = PostTestFactory.postWithId(42L, "alice", "テーマ引継ぎ確認");
        given(postService.search(null)).willReturn(List.of(post));
        given(postService.findById(42L)).willReturn(Optional.of(post));
        given(postLikeService.countLikes(42L)).willReturn(0L);

        // 一覧画面にテーマトグルと、localStorageを読む共通JSがあることを確認する。
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-theme-toggle")))
                .andExpect(content().string(containsString("src=\"/js/theme.js\"")));

        // 投稿詳細画面にも同じ仕組みがあるため、画面遷移後もブラウザ側でモードを復元できる。
        mockMvc.perform(get("/posts/42"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-theme-toggle")))
                .andExpect(content().string(containsString("src=\"/js/theme.js\"")));

        // 新規投稿画面にも同じ仕組みがあるため、投稿作成導線でもモードを引き継げる。
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-theme-toggle")))
                .andExpect(content().string(containsString("src=\"/js/theme.js\"")));
    }

    @Test
    @DisplayName("投稿一覧_投稿は投稿者内容投稿日の順に表示できている")
    void 投稿一覧_投稿は投稿者内容投稿日の順に表示できている() throws Exception {
        // 投稿1件を Service から返し、ビューが表示する投稿内容を固定する。
        Post post = PostTestFactory.postWithId(1L, "alice", "長い本文でも折り返して表示する");
        given(postService.search(null)).willReturn(List.of(post));

        // 一覧画面のHTMLを取得し、表示順の検証に使う。
        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("post__body")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 投稿者、本文、日時が画面上で期待順に並ぶことを確認する。
        assertThat(html).containsSubsequence("alice", "長い本文でも折り返して表示する", "2026-05-23 10:00");
    }

    @Test
    @DisplayName("投稿一覧_投稿者カラー_投稿者名左のアイコンに選択色を適用する")
    void 投稿一覧_投稿者カラー_投稿者名左のアイコンに選択色を適用する() throws Exception {
        // 投稿者カラーを持つ投稿を一覧に表示する。
        Post post = PostTestFactory.postWithIdAndColor(42L, "alice", "色付き投稿", "#ef4444");
        given(postService.search(null)).willReturn(List.of(post));

        // 投稿者名の左にある色アイコンへ、保存済みの色が反映されることを確認する。
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"post__author-color\"")))
                .andExpect(content().string(containsString("style=\"background-color: #ef4444\"")))
                .andExpect(content().string(containsString("alice")));
    }

    @Test
    @DisplayName("投稿一覧_投稿ブロック_詳細画面へのリンクになる")
    void 投稿一覧_投稿ブロック_詳細画面へのリンクになる() throws Exception {
        // 一覧に表示する投稿へIDを設定し、リンク先URLを検証できるようにする。
        Post post = PostTestFactory.postWithId(42L, "alice", "詳細へ遷移する本文");
        given(postService.search(null)).willReturn(List.of(post));

        // 投稿ブロックがクリック可能なリンクになり、投稿IDに対応する /posts/{id} を指すことを確認する。
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/posts/42\"")))
                .andExpect(content().string(containsString("詳細へ遷移する本文")));
    }

    @Test
    @DisplayName("投稿検索_qあり_Serviceで検索し入力値を画面に保持する")
    void 投稿検索_qあり_Serviceで検索し入力値を画面に保持する() throws Exception {
        // 検索キーワードに一致した投稿だけを Service モックから返す。
        Post post = PostTestFactory.postWithId(42L, "alice", "keywordを含む本文");
        given(postService.search("keyword")).willReturn(List.of(post));

        // GET /posts?q=keyword で検索結果を表示し、入力欄にも q が残ることを確認する。
        mockMvc.perform(get("/posts").param("q", "keyword"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("q", "keyword"))
                .andExpect(model().attribute("posts", List.of(post)))
                .andExpect(content().string(containsString("keywordを含む本文")))
                .andExpect(content().string(containsString("value=\"keyword\"")));
    }

    @Test
    @DisplayName("投稿詳細_存在するID_200OKで詳細画面を表示しmodel.postを積む")
    void 投稿詳細_存在するID_200OKで詳細画面を表示しmodelPostを積む() throws Exception {
        // 詳細表示対象の投稿を Service モックから返す。
        Post post = PostTestFactory.postWithIdAndColor(42L, "alice",
                "詳細画面に表示する本文", "#22c55e");
        given(postService.findById(42L)).willReturn(Optional.of(post));
        given(postLikeService.countLikes(42L)).willReturn(5L);

        // GET /posts/{id} で詳細ビューが表示され、model.post と投稿内容が返ることを確認する。
        mockMvc.perform(get("/posts/42"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(model().attribute("likeCount", 5L))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("style=\"background-color: #22c55e\"")))
                .andExpect(content().string(containsString("詳細画面に表示する本文")))
                .andExpect(content().string(containsString("2026-05-23 10:00")))
                .andExpect(content().string(containsString("いいね <span>5</span>")))
                .andExpect(content().string(containsString("action=\"/posts/42/likes\"")))
                .andExpect(content().string(containsString("action=\"/posts/42/delete\"")))
                .andExpect(content().string(containsString("class=\"delete-button\"")));
    }

    @Test
    @DisplayName("投稿詳細_存在しないID_404NotFoundを返す")
    void 投稿詳細_存在しないID_404NotFoundを返す() throws Exception {
        // 指定IDの投稿が存在しない状況を Service モックで作る。
        given(postService.findById(999L)).willReturn(Optional.empty());

        // 存在しない投稿IDでは詳細画面を表示せず、404 Not Found を返すことを確認する。
        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿詳細_削除フラグ1_Serviceが空を返す場合404NotFoundを返す")
    void 投稿詳細_削除フラグ1_Serviceが空を返す場合404NotFoundを返す() throws Exception {
        given(postService.findById(42L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/42"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿削除_POST_postsIdDelete_Serviceで論理削除して一覧へリダイレクトする")
    void 投稿削除_POST_postsIdDelete_Serviceで論理削除して一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts/42/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).delete(42L);
    }

    @Test
    @DisplayName("いいね_POST_postsIdLikes_トグル後に詳細画面へリダイレクトする")
    void いいね_POST_postsIdLikes_トグル後に詳細画面へリダイレクトする() throws Exception {
        // Likeボタン押下を再現し、Serviceでトグルしたあと詳細画面へ戻ることを確認する。
        given(clientHashService.hash("192.0.2.1", "JUnit")).willReturn("73c89905");

        mockMvc.perform(post("/posts/42/likes")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.1");
                            request.addHeader("User-Agent", "JUnit");
                            return request;
                        }))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/42"));

        verify(postLikeService).toggleLike(42L, "73c89905");
    }

    @Test
    @DisplayName("いいね_POST_存在しない投稿ID_404NotFoundを返す")
    void いいね_POST_存在しない投稿ID_404NotFoundを返す() throws Exception {
        // Serviceが投稿なしを通知した場合、Controllerは404として返す。
        doThrow(new PostNotFoundException(999L)).when(postLikeService).toggleLike(eq(999L), anyString());
        given(clientHashService.hash("192.0.2.1", "JUnit")).willReturn("73c89905");

        mockMvc.perform(post("/posts/999/likes")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.1");
                            request.addHeader("User-Agent", "JUnit");
                            return request;
                        }))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("投稿作成_GET_postsNew_postFormをmodelに積みフォーム画面を表示する")
    void 投稿作成_GET_postsNew_postFormをmodelに積みフォーム画面を表示する() throws Exception {
        // GET /posts/new で投稿フォームを開き、フォームバインド用の postForm が用意されることを確認する。
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)))
                .andExpect(content().string(containsString("<form action=\"/post\" method=\"post\"")));
    }

    @Test
    @DisplayName("投稿作成_投稿者未入力_エラーメッセージを表示し保存せず画面遷移しない")
    void 投稿作成_投稿者未入力_エラーメッセージを表示し保存せず画面遷移しない() throws Exception {
        // 投稿者だけ空で送信し、author のバリデーションエラーを発生させる。
        mockMvc.perform(post("/posts")
                        .param("author", "")
                        .param("body", "本文です"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者を入力してください")));

        // 入力エラー時は保存処理を呼ばないことを確認する。
        verify(postService, never()).create(any(Post.class));
    }

    @Test
    @DisplayName("投稿作成_本文未入力_エラーメッセージを表示し保存せず画面遷移しない")
    void 投稿作成_本文未入力_エラーメッセージを表示し保存せず画面遷移しない() throws Exception {
        // 本文だけ空で送信し、body のバリデーションエラーを発生させる。
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")));

        // 入力エラー時は保存処理を呼ばないことを確認する。
        verify(postService, never()).create(any(Post.class));
    }

    @Test
    @DisplayName("投稿作成_投稿者が空白_保存せず画面遷移しない")
    void 投稿作成_投稿者が空白_保存せず画面遷移しない() throws Exception {
        // 投稿者が空白だけの場合も未入力として扱われることを確認する。
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "本文です"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("投稿者を入力してください")));

        // 空白だけの投稿者では保存処理を呼ばないことを確認する。
        verify(postService, never()).create(any(Post.class));
    }

    @Test
    @DisplayName("投稿作成_本文が空白_保存せず画面遷移しない")
    void 投稿作成_本文が空白_保存せず画面遷移しない() throws Exception {
        // 本文が空白だけの場合も未入力として扱われることを確認する。
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")));

        // 空白だけの本文では保存処理を呼ばないことを確認する。
        verify(postService, never()).create(any(Post.class));
    }

    @Test
    @DisplayName("投稿作成_正常入力_Postに変換して保存し一覧へリダイレクトする")
    void 投稿作成_正常入力_Postに変換して保存し一覧へリダイレクトする() throws Exception {
        // 投稿者と本文がそろっている正常入力を送信する。
        mockMvc.perform(post("/post")
                        .param("author", "alice")
                        .param("authorColor", "#ef4444")
                        .param("body", "本文です"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        // Service に渡された Post を取り出し、フォーム入力がエンティティへ変換されたことを確認する。
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postService).create(captor.capture());
        assertThat(captor.getValue().getAuthor()).isEqualTo("alice");
        assertThat(captor.getValue().getAuthorColor()).isEqualTo("#ef4444");
        assertThat(captor.getValue().getBody()).isEqualTo("本文です");
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("投稿作成_postsへPOST_一覧へリダイレクトする")
    void 投稿作成_postsへPOST_一覧へリダイレクトする() throws Exception {
        // 既存の /posts へ POST しても投稿作成として処理できることを確認する。
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "本文です"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        // /posts への POST でも保存処理が呼ばれることを確認する。
        verify(postService).create(any(Post.class));
    }

    @Test
    @DisplayName("投稿作成_postsNewへPOST_エラーページにせず一覧へリダイレクトする")
    void 投稿作成_postsNewへPOST_エラーページにせず一覧へリダイレクトする() throws Exception {
        // ブラウザが /posts/new に POST しても 405 にせず、投稿作成として処理する。
        mockMvc.perform(post("/posts/new")
                        .param("author", "alice")
                        .param("body", "本文です"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        // /posts/new への POST でも保存処理が呼ばれることを確認する。
        verify(postService).create(any(Post.class));
    }
}
