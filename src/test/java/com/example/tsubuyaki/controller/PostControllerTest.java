package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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

    @Test
    @DisplayName("投稿一覧_0件の場合_空メッセージを表示しmodel.postsにListを積む")
    void 投稿一覧_0件の場合_空メッセージを表示しmodelPostsにListを積む() throws Exception {
        // Repository 由来の投稿が0件である状況を Service モックで作る。
        given(postService.latest()).willReturn(Collections.emptyList());

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
        given(postService.latest()).willReturn(Collections.emptyList());

        // 更新ボタンが GET /posts/ に向くフォームとして描画されることを確認する。
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<form class=\"toolbar\" action=\"/posts/\" method=\"get\"")))
                .andExpect(content().string(containsString("<button type=\"submit\"")));
    }

    @Test
    @DisplayName("投稿一覧_投稿は投稿者内容投稿日の順に表示できている")
    void 投稿一覧_投稿は投稿者内容投稿日の順に表示できている() throws Exception {
        // 投稿1件を Service から返し、ビューが表示する投稿内容を固定する。
        given(postService.latest()).willReturn(List.of(
                new Post("alice", "長い本文でも折り返して表示する", Instant.parse("2026-05-23T10:00:00Z"))));

        // 一覧画面のHTMLを取得し、表示順の検証に使う。
        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("post__body")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 投稿者、本文、日時が画面上で期待順に並ぶことを確認する。
        assertThat(html).containsSubsequence("alice", "長い本文でも折り返して表示する", "2026-05-23 19:00");
    }

    @Test
    @DisplayName("投稿作成_GET_postsNew_postFormをmodelに積みフォーム画面を表示する")
    void 投稿作成_GET_postsNew_postFormをmodelに積みフォーム画面を表示する() throws Exception {
        // GET /posts/new で投稿フォームを開き、フォームバインド用の postForm が用意されることを確認する。
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)))
                .andExpect(content().string(containsString("<form action=\"/posts\" method=\"post\"")));
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
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "本文です"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        // Service に渡された Post を取り出し、フォーム入力がエンティティへ変換されたことを確認する。
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postService).create(captor.capture());
        assertThat(captor.getValue().getAuthor()).isEqualTo("alice");
        assertThat(captor.getValue().getBody()).isEqualTo("本文です");
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
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
