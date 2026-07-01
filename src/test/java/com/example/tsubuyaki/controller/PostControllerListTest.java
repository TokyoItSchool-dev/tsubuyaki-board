package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostControllerListTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void list_whenNoPosts_showsEmptyMessage() throws Exception {
        given(postService.search(null)).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", Collections.emptyList()))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_押すとpostsスラッシュへGETリクエストする")
    void list_refreshButton_requestsPostsSlash() throws Exception {
        given(postService.search(null)).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/posts/\"")))
                .andExpect(content().string(containsString("method=\"get\"")))
                .andExpect(content().string(containsString("<button type=\"submit\"")))
                .andExpect(content().string(containsString("更新")));
    }

    @Test
    @DisplayName("投稿一覧_投稿は投稿者内容投稿日の順に表示する")
    void list_rendersAuthorBodyCreatedAtInOrder() throws Exception {
        given(postService.search(null)).willReturn(List.of(
                new Post("alice", "朝の共有です", Instant.parse("2026-05-23T10:00:00Z"))
        ));

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("alice", "朝の共有です", "2026-05-23 19:00");
        assertThat(html.indexOf("alice")).isLessThan(html.indexOf("朝の共有です"));
        assertThat(html.indexOf("朝の共有です")).isLessThan(html.indexOf("2026-05-23 19:00"));
    }

    @Test
    @DisplayName("投稿検索_GET_posts_q指定_絞り込み結果をmodelに設定し入力値を保持する")
    void list_whenQueryProvided_setsFilteredPostsAndKeepsQuery() throws Exception {
        List<Post> posts = List.of(
                new Post("alice", "検索対象の投稿", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postService.search("検索対象")).willReturn(posts);
        given(postService.hasSearchQuery("検索対象")).willReturn(true);

        mockMvc.perform(get("/posts").param("q", "検索対象"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", posts))
                .andExpect(model().attribute("q", "検索対象"))
                .andExpect(model().attribute("searched", true))
                .andExpect(content().string(containsString("name=\"q\"")))
                .andExpect(content().string(containsString("value=\"検索対象\"")))
                .andExpect(content().string(containsString("検索対象の投稿")));
    }

    @Test
    @DisplayName("投稿検索_GET_posts_q指定で0件_該当する投稿はありませんを表示する")
    void list_whenQueryProvidedAndNoPosts_showsSearchEmptyMessage() throws Exception {
        given(postService.search("検索対象")).willReturn(Collections.emptyList());
        given(postService.hasSearchQuery("検索対象")).willReturn(true);

        mockMvc.perform(get("/posts").param("q", "検索対象"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", Collections.emptyList()))
                .andExpect(content().string(containsString("該当する投稿はありません")));
    }
}
