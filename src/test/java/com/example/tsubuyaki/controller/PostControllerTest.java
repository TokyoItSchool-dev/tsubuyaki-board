package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.ClientHashService;
import com.example.tsubuyaki.service.PostLikeService;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.sameInstance;
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

    @MockitoBean
    private PostLikeService postLikeService;

    @MockitoBean
    private ClientHashService clientHashService;

    @Test
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void 投稿一覧_0件の場合_まだ投稿はありませんを表示する() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attribute("q", ""))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_q未指定_通常一覧をビューに渡す")
    void 投稿一覧_q未指定_通常一覧をビューに渡す() throws Exception {
        List<Post> posts = List.of(new Post("alice", "通常一覧です",
                LocalDateTime.parse("2026-05-23T10:00:00")));
        given(postService.latest()).willReturn(posts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", sameInstance(posts)))
                .andExpect(model().attribute("q", ""));
    }

    @Test
    @DisplayName("投稿一覧_q空文字_通常一覧をビューに渡す")
    void 投稿一覧_q空文字_通常一覧をビューに渡す() throws Exception {
        List<Post> posts = List.of(new Post("alice", "通常一覧です",
                LocalDateTime.parse("2026-05-23T10:00:00")));
        given(postService.latest()).willReturn(posts);

        mockMvc.perform(get("/posts").param("q", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", sameInstance(posts)))
                .andExpect(model().attribute("q", ""));
    }

    @Test
    @DisplayName("投稿検索_q指定_検索結果をビューに渡す")
    void 投稿検索_q指定_検索結果をビューに渡す() throws Exception {
        List<Post> posts = List.of(new Post("alice", "検索対象です",
                LocalDateTime.parse("2026-05-23T10:00:00")));
        given(postService.search("検索")).willReturn(posts);

        mockMvc.perform(get("/posts").param("q", "検索"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", sameInstance(posts)))
                .andExpect(model().attribute("q", "検索"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"q\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("value=\"検索\"")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsスラッシュへGETする")
    void 投稿一覧_更新ボタン_押すとpostsスラッシュへGETする() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("更新");
        assertThat(html).contains("method=\"get\"");
        assertThat(html).contains("action=\"/posts/\"");

        mockMvc.perform(get("/posts/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"));
    }

    @Test
    @DisplayName("投稿一覧_投稿あり_投稿者内容投稿日の順に表示する")
    void 投稿一覧_投稿あり_投稿者内容投稿日の順に表示する() throws Exception {
        given(postService.latest()).willReturn(List.of(
                new Post("alice", "本文です", LocalDateTime.parse("2026-05-23T10:00:00"))));

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("本文です"));
        assertThat(html.indexOf("本文です")).isLessThan(html.indexOf("2026-05-23"));
    }

    @Test
    @DisplayName("投稿一覧_投稿あり_詳細ボタンから投稿詳細へ移動できる")
    void 投稿一覧_投稿あり_詳細ボタンから投稿詳細へ移動できる() throws Exception {
        Post post = new Post("alice", "本文です", LocalDateTime.parse("2026-05-23T10:00:00"));
        ReflectionTestUtils.setField(post, "id", 1L);
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("詳細")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/posts/1\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("method=\"get\"")));
    }

    @Test
    @DisplayName("新規投稿フォーム_GET_posts_new_postFormをビューに渡す")
    void 新規投稿フォーム_GET_posts_new_postFormをビューに渡す() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.instanceOf(PostForm.class)))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文 (280 文字まで)")));
    }

    @Test
    @DisplayName("投稿詳細_存在するid_該当Postをビューに渡す")
    void 投稿詳細_存在するid_該当Postをビューに渡す() throws Exception {
        Post post = new Post("alice", "詳細本文です", LocalDateTime.parse("2026-05-23T10:00:00"));
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(postLikeService.countLikes(1L)).willReturn(3L);

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", sameInstance(post)))
                .andExpect(model().attribute("likeCount", 3L))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("いいね 3")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("like")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("action=\"/posts/1/likes\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("method=\"post\"")));
    }

    @Test
    @DisplayName("投稿詳細_存在しないid_404を返す")
    void 投稿詳細_存在しないid_404を返す() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("いいねトグル_POST_posts_id_likes_clientHashを渡して詳細へ戻る")
    void いいねトグル_POST_posts_id_likes_clientHashを渡して詳細へ戻る() throws Exception {
        String ipAddress = "203.0.113.10";
        String userAgent = "JUnit";
        given(clientHashService.generate(ipAddress, userAgent)).willReturn("abc12345");

        mockMvc.perform(post("/posts/1/likes")
                        .header("User-Agent", userAgent)
                        .with(request -> {
                            request.setRemoteAddr(ipAddress);
                            return request;
                        }))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        verify(clientHashService).generate(ipAddress, userAgent);
        verify(postLikeService).toggleLike(1L, "abc12345");
    }
}
