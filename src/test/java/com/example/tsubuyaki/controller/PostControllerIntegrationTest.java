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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class PostControllerIntegrationTest {

    // Spring Boot 全体を起動した状態で HTTP リクエストを疑似実行する。
    @Autowired
    private MockMvc mockMvc;

    // 実際の Repository を使い、保存有無をDB件数で確認する。
    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        // 各テストを空の投稿テーブルから始める。
        postRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // 後続テストへ投稿データを残さないようにする。
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("投稿作成_入力エラー_WhitelabelErrorPageへ飛ばない")
    void 投稿作成_入力エラー_WhitelabelErrorPageへ飛ばない() throws Exception {
        // 投稿者未入力で送信し、エラー画面ではなくフォームに戻ることを確認する。
        mockMvc.perform(post("/posts")
                        .param("author", "")
                        .param("body", "本文です"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(content().string(not(containsString("Whitelabel Error Page"))));
    }

    @Test
    @DisplayName("投稿作成_投稿者が空白_INSERT処理を行わず画面遷移しない")
    void 投稿作成_投稿者が空白_INSERT処理を行わず画面遷移しない() throws Exception {
        // 投稿者が空白だけの入力を送信する。
        mockMvc.perform(post("/posts")
                        .param("author", "   ")
                        .param("body", "本文です"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"));

        // バリデーションエラー時は INSERT されず、投稿件数が0件のままであることを確認する。
        assertThat(postRepository.count()).isZero();
    }

    @Test
    @DisplayName("投稿作成_本文が空白_INSERT処理を行わず画面遷移しない")
    void 投稿作成_本文が空白_INSERT処理を行わず画面遷移しない() throws Exception {
        // 本文が空白だけの入力を送信する。
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"));

        // バリデーションエラー時は INSERT されず、投稿件数が0件のままであることを確認する。
        assertThat(postRepository.count()).isZero();
    }

    @Test
    @DisplayName("投稿作成_正常入力_投稿された内容が投稿一覧画面で表示される")
    void 投稿作成_正常入力_投稿された内容が投稿一覧画面で表示される() throws Exception {
        // 正常な投稿を送信し、保存後に一覧へリダイレクトされることを確認する。
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "一覧に表示される本文"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        // 保存済み投稿を GET /posts で取得し、一覧画面に表示されることを確認する。
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("一覧に表示される本文")))
                .andExpect(content().string(not(containsString("Whitelabel Error Page"))));
    }
    @Test
    @DisplayName("投稿削除_詳細画面から削除すると一覧へ戻り削除済み詳細は404になる")
    void 投稿削除_詳細画面から削除すると一覧へ戻り削除済み詳細は404になる() throws Exception {
        Post post = postRepository.save(new Post("alice", "削除対象の本文",
                LocalDateTime.of(2026, 5, 23, 10, 0)));

        mockMvc.perform(post("/posts/{id}/delete", post.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        Post deletedPost = postRepository.findById(post.getId()).orElseThrow();
        assertThat(deletedPost.getDeletedAt()).isEqualTo(Post.DELETED);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("削除対象の本文"))));

        mockMvc.perform(get("/posts/{id}", post.getId()))
                .andExpect(status().isNotFound());
    }
}
