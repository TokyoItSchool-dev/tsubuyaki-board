package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class PostDetailIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @BeforeEach
    void setUp() {
        // 外部キー制約を守るため、投稿より先にいいねデータを削除する。
        postLikeRepository.deleteAll();
        // 詳細取得テストの前提を固定するため、各テスト開始時に投稿データを空にする。
        postRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // 後続テストへいいねデータが残らないよう、投稿より先に削除する。
        postLikeRepository.deleteAll();
        // 後続テストへ投稿データが残らないよう、テスト終了時にも投稿データを削除する。
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("投稿詳細_存在するidのとき_詳細画面と投稿Modelを返す")
    void 投稿詳細_存在するidのとき_詳細画面と投稿Modelを返す() throws Exception {
        // 詳細画面で表示する対象投稿をDBへ保存し、採番されたidでアクセスする。
        Post savedPost = postRepository.save(new Post(
                "alice",
                "M4の投稿詳細を実装します",
                LocalDateTime.parse("2026-06-01T09:30:00")));
        // 詳細画面に表示するいいね数を検証するため、対象投稿にいいねを1件保存する。
        postLikeRepository.save(new PostLike(
                savedPost,
                "abcd1234",
                LocalDateTime.parse("2026-06-01T10:00:00")));

        mockMvc.perform(get("/posts/{id}", savedPost.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("likeCount", 1L))
                .andExpect(model().attribute("post", allOf(
                        instanceOf(Post.class),
                        hasProperty("id", is(savedPost.getId())),
                        hasProperty("author", is("alice")),
                        hasProperty("body", is("M4の投稿詳細を実装します")),
                        hasProperty("createdAt", is(LocalDateTime.parse("2026-06-01T09:30:00")))
                )))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("M4の投稿詳細を実装します")))
                .andExpect(content().string(containsString("2026-06-01 09:30")))
                .andExpect(content().string(containsString("いいね")))
                .andExpect(content().string(containsString("action=\"/posts/" + savedPost.getId() + "/likes\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("Like")))
                .andExpect(content().string(containsString("href=\"/posts\"")));
    }

    @Test
    @DisplayName("投稿詳細_存在しないidのとき_404を返す")
    void 投稿詳細_存在しないidのとき_404を返す() throws Exception {
        // DBに存在しないidを指定した場合、詳細画面ではなく404を返すことを確認する。
        mockMvc.perform(get("/posts/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
