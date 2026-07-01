package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class PostListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
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
    @DisplayName("新規投稿フォーム_GET_posts_newはpostFormを積んでフォームビューを返す")
    void newForm_addsPostFormAndRendersFormView() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"));
    }
}
