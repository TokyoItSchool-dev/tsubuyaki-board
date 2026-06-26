package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
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

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void list_whenNoPosts_showsEmptyMessage() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsにGETリクエストする")
    void list_hasRefreshButtonRequestingPosts() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/posts\"")))
                .andExpect(content().string(containsString("method=\"get\"")))
                .andExpect(content().string(containsString("<button type=\"submit\"")));
    }

    @Test
    @DisplayName("投稿一覧_投稿がある場合_投稿者内容投稿日の順に表示する")
    void list_whenPostsExist_showsAuthorBodyCreatedAtInOrder() throws Exception {
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.latest()).willReturn(List.of(post));

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("alice", "本文です", "2026-05-23 19:15");
        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("本文です"));
        assertThat(html.indexOf("本文です")).isLessThan(html.indexOf("2026-05-23 19:15"));
    }

    @Test
    @DisplayName("投稿一覧_投稿をクリック_posts_idへの詳細リンクを表示する")
    void list_whenPostsExist_showsDetailLink() throws Exception {
        Post post = postWithId(1L, "alice", "本文です", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/posts/1\"")));
    }

    @Test
    @DisplayName("投稿フォーム_GET_posts_new_postsFormを積んでposts/formを表示する")
    void newForm_addsPostFormAndRendersForm() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"));
    }

    @Test
    @DisplayName("投稿登録_正常入力_Serviceで保存して投稿一覧へリダイレクトする")
    void create_whenValid_savesAndRedirectsToPosts() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "投稿テスト"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).create("alice", "投稿テスト");
    }

    @Test
    @DisplayName("投稿詳細_GET_posts_id_posts_detailを表示する")
    void detail_whenPostExists_rendersDetailView() throws Exception {
        Post post = postWithId(1L, "alice", "詳細本文", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(postService.countLikes(1L)).willReturn(3L);

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(model().attribute("likeCount", 3L))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("詳細本文")))
                .andExpect(content().string(containsString("いいね")))
                .andExpect(content().string(containsString(">3<")))
                .andExpect(content().string(containsString("action=\"/posts/1/likes\"")))
                .andExpect(content().string(containsString("❤️")));
    }

    @Test
    @DisplayName("投稿詳細_GET_posts_id_存在しないidは404を返す")
    void detail_whenPostDoesNotExist_returnsNotFound() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("いいね_POST_posts_id_likes_clientHashでトグルして詳細へ戻る")
    void toggleLike_whenPostExists_togglesByClientHashAndRedirectsToDetail() throws Exception {
        Post post = postWithId(1L, "alice", "詳細本文", Instant.parse("2026-05-23T10:15:00Z"));
        given(postService.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(post("/posts/1/likes")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.10");
                            return request;
                        })
                        .header("User-Agent", "JUnit UA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).toggleLike(1L, sha256First8("203.0.113.10JUnit UA"));
    }

    @Test
    @DisplayName("いいね_POST_posts_id_likes_存在しないidは404を返す")
    void toggleLike_whenPostDoesNotExist_returnsNotFound() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(post("/posts/999/likes"))
                .andExpect(status().isNotFound());
    }

    private static Post postWithId(Long id, String author, String body, Instant createdAt) throws Exception {
        Post post = new Post(author, body, createdAt);
        Field idField = Post.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(post, id);
        return post;
    }

    private static String sha256First8(String source) throws NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(source.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (byte value : digest) {
            builder.append(String.format("%02x", value));
        }
        return builder.substring(0, 8);
    }
}
