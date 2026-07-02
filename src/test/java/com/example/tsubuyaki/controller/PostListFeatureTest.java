package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

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
@Transactional
class PostListFeatureTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("投稿一覧_GET_posts_空表示と新着50件と更新ボタンと表示順を満たす")
    void 投稿一覧_GET_posts_空表示と新着50件と更新ボタンと表示順を満たす() throws Exception {
        postRepository.deleteAll();

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(content().string(containsString("まだ投稿はありません")));

        LocalDateTime base = LocalDateTime.of(2026, 5, 23, 18, 0);
        for (int i = 0; i < 51; i++) {
            postRepository.save(new Post("user" + i, "body" + i, base.plusMinutes(i)));
        }
        postRepository.flush();

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(content().string(containsString("action=\"/posts\"")))
                .andExpect(content().string(containsString("method=\"get\"")))
                .andExpect(content().string(containsString("更新")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long latestPostId = postRepository.findTop50ByOrderByCreatedAtDesc().getFirst().getId();

        assertThat(html).contains("user50", "body50", "2026-05-23 18:50");
        assertThat(html).doesNotContain("user0", "body0");
        assertThat(html).contains("href=\"/posts/" + latestPostId + "\"");
        assertThat(html).containsSubsequence("user50", "body50", "2026-05-23 18:50");
    }

    @Test
    @DisplayName("投稿検索_GET_posts_q_検索フォームと空検索と0件表示と新着50件制限を満たす")
    void 投稿検索_GET_posts_q_検索フォームと空検索と0件表示と新着50件制限を満たす() throws Exception {
        postRepository.deleteAll();
        LocalDateTime base = LocalDateTime.of(2026, 5, 24, 9, 0);
        for (int i = 0; i < 51; i++) {
            postRepository.save(new Post("match-user" + i, "検索対象 body" + i, base.plusMinutes(i)));
        }
        postRepository.save(new Post("other-user", "関係ない本文", base.plusHours(2)));
        postRepository.flush();

        String listHtml = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attribute("query", ""))
                .andExpect(content().string(containsString("class=\"post-search\"")))
                .andExpect(content().string(containsString("action=\"/posts\"")))
                .andExpect(content().string(containsString("method=\"get\"")))
                .andExpect(content().string(containsString("name=\"q\"")))
                .andExpect(content().string(containsString("検索")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listHtml).containsSubsequence(
                "post-list__refresh",
                "更新",
                "post-search",
                "name=\"q\"",
                "検索",
                "post");

        mockMvc.perform(get("/posts").param("q", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("query", ""))
                .andExpect(model().attribute("searched", false))
                .andExpect(content().string(not(containsString("検索:"))))
                .andExpect(content().string(containsString("other-user")));

        String searchHtml = mockMvc.perform(get("/posts").param("q", "検索対象"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attribute("query", "検索対象"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(searchHtml).contains("match-user50", "body50", "2026-05-24 09:50");
        assertThat(searchHtml).contains("検索:", "検索対象", "href=\"/posts\"", "検索を解除");
        assertThat(searchHtml).doesNotContain("match-user0", "body0", "other-user", "関係ない本文");
        assertThat(searchHtml).containsSubsequence("match-user50", "match-user49", "match-user48");

        mockMvc.perform(get("/posts").param("q", "該当なし"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("query", "該当なし"))
                .andExpect(content().string(containsString("条件に一致する結果が見つかりませんでした。")));
    }

    @Test
    @DisplayName("投稿一覧_モダンUI_中央タイムラインと投稿導線と投稿カードを読みやすく表示する")
    void 投稿一覧_モダンUI_中央タイムラインと投稿導線と投稿カードを読みやすく表示する() throws Exception {
        postRepository.deleteAll();

        String emptyHtml = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(emptyHtml).contains("class=\"empty-state\"", "まだ投稿はありません。");

        postRepository.save(new Post(
                "modern-user",
                "モダンUIの本文",
                LocalDateTime.of(2026, 5, 26, 8, 45)));
        postRepository.flush();

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains(
                "href=\"/css/app.css\"",
                "class=\"app-shell\"",
                "class=\"app-header\"",
                "class=\"timeline-layout\"",
                "class=\"composer-card\"",
                "class=\"timeline-feed\"",
                "class=\"post post-card post--bg-default\"");
        assertThat(html).contains("詳細を見る");
        assertThat(html).contains("class=\"post__detail-link button button--ghost\"");
        assertThat(html).containsSubsequence("composer-card", "新規投稿", "timeline-feed", "modern-user", "モダンUIの本文");

        String css = new ClassPathResource("static/css/app.css").getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).contains(
                "--surface:",
                "--shadow-card:",
                ".app-shell {",
                "max-width: 720px;",
                ".app-header {",
                ".timeline-layout {",
                ".composer-card {",
                ".timeline-feed {",
                ".post-card {",
                "border-radius: 8px;",
                ".search-status {",
                ".button--danger {",
                ".button--ghost {",
                "@media (max-width: 640px)",
                "min(100% - 32px, 720px)",
                ":focus-visible");
    }

    @Test
    @DisplayName("投稿一覧_編集済み表示_updatedAtがある投稿だけ更新日時を表示する")
    void 投稿一覧_編集済み表示_updatedAtがある投稿だけ更新日時を表示する() throws Exception {
        postRepository.deleteAll();
        Post originalPost = postRepository.save(new Post(
                "original-user",
                "original-body",
                LocalDateTime.of(2026, 5, 27, 10, 0)));
        Post editedPost = postRepository.save(new Post(
                "edited-user",
                "edited-body",
                LocalDateTime.of(2026, 5, 27, 11, 0)));
        editedPost.updateBodyAndBackgroundColor(
                "edited-body",
                editedPost.getBackgroundColor(),
                LocalDateTime.of(2026, 5, 27, 12, 30));
        postRepository.flush();

        String html = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(html).contains("edited-user", "編集済み", "2026-05-27 12:30");
        assertThat(html).containsSubsequence("original-user", "original-body", "2026-05-27 10:00");
        int originalStart = html.indexOf("original-user");
        int originalEnd = html.indexOf("</article>", originalStart);
        assertThat(html.substring(originalStart, originalEnd))
                .doesNotContain("編集済み");
        assertThat(originalPost.getUpdatedAt()).isNull();
    }
}
