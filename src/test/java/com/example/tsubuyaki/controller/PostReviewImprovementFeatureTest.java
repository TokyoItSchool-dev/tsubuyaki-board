package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.service.ClientHashGenerator;
import com.example.tsubuyaki.service.PostService;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostReviewImprovementFeatureTest {

    private static final String OWNER_IP = "203.0.113.10";
    private static final String OWNER_USER_AGENT = "TsubuyakiOwner/1.0";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    @DisplayName("レビュー改善_一覧検索フォーム投稿カード削除済み詳細を一括で改善する")
    void レビュー改善_一覧検索フォーム投稿カード削除済み詳細を一括で改善する() throws Exception {
        postLikeRepository.deleteAll();
        postRepository.deleteAll();

        Post first = savePost("review-user-1", "検索語を含む本文", "#dbeafe");
        Post second = savePost("review-user-2", "検索語を含む別本文", "#fee2e2");
        Post third = savePost("review-user-3", "通常本文", "#fef9c3");
        postLikeRepository.save(new PostLike(first, "client01", LocalDateTime.of(2026, 6, 1, 11, 0)));
        postLikeRepository.save(new PostLike(first, "client02", LocalDateTime.of(2026, 6, 1, 11, 1)));
        postLikeRepository.save(new PostLike(second, "client03", LocalDateTime.of(2026, 6, 1, 11, 2)));
        Post deleted = savePost("deleted-user", "deleted-body", "#dcfce7");
        deleted.markDeleted();
        postRepository.flush();
        postLikeRepository.flush();

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        assertThat(postService.latest())
                .extracting("likeCount")
                .contains(2L, 1L, 0L);
        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(2);

        statistics.clear();
        assertThat(postService.searchByBody("検索語"))
                .extracting("likeCount")
                .containsExactlyInAnyOrder(1L, 2L);
        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(2);

        String listHtml = mockMvc.perform(get("/posts").param("q", "検索語"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(content().string(containsString("maxlength=\"280\"")))
                .andExpect(content().string(containsString("name=\"q\" value=\"検索語\"")))
                .andExpect(content().string(containsString("post--bg-blue")))
                .andExpect(content().string(not(containsString("style=\"background-color:"))))
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(listHtml).containsSubsequence(
                "name=\"returnTo\" value=\"list\"",
                "name=\"q\" value=\"検索語\"");

        String encodedQuery = UriUtils.encodeQueryParam("検索語", StandardCharsets.UTF_8);
        mockMvc.perform(post("/posts/{id}/likes", first.getId())
                        .param("returnTo", "list")
                        .param("q", "検索語")
                        .with(client()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts?q=" + encodedQuery));
        mockMvc.perform(post("/posts/{id}/likes", first.getId())
                        .param("returnTo", "list")
                        .param("q", "   ")
                        .with(client()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        String longQuery = "あ".repeat(281);
        mockMvc.perform(get("/posts").param("q", longQuery))
                .andExpect(status().isOk())
                .andExpect(model().attribute("query", "あ".repeat(280)));

        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aria-describedby=\"author-help\"")))
                .andExpect(content().string(containsString("aria-describedby=\"body-help\"")))
                .andExpect(content().string(not(containsString("aria-describedby=\"author-help author-error\""))))
                .andExpect(content().string(not(containsString("aria-describedby=\"body-help body-error\""))));
        mockMvc.perform(post("/posts")
                        .param("author", "")
                        .param("body", "")
                        .param("backgroundColor", "#ffffff"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aria-describedby=\"author-help author-error\"")))
                .andExpect(content().string(containsString("aria-describedby=\"body-help body-error\"")))
                .andExpect(content().string(containsString("aria-invalid=\"true\"")))
                .andExpect(content().string(containsString("id=\"author-error\"")))
                .andExpect(content().string(containsString("id=\"body-error\"")));
        mockMvc.perform(get("/posts/{id}/edit", first.getId()).with(client()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aria-describedby=\"body-help\"")))
                .andExpect(content().string(not(containsString("aria-describedby=\"body-help body-error\""))));

        String detailHtml = mockMvc.perform(get("/posts/{id}", first.getId()).with(client()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("post--bg-blue")))
                .andExpect(content().string(not(containsString("style=\"background-color:"))))
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(detailHtml).contains("post post--detail");

        mockMvc.perform(get("/posts/{id}/delete-confirm", first.getId()).with(client()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("post--bg-blue")))
                .andExpect(content().string(not(containsString("style=\"background-color:"))));

        String css = new ClassPathResource("static/css/app.css").getContentAsString(StandardCharsets.UTF_8);
        assertThat(selectorBlock(css, ".post__like-button")).contains("min-height: 2.5rem;");
        assertThat(selectorBlock(css, ".post__actions")).contains("border-top:", "padding-top:");
        assertThat(selectorBlock(css, ".post__like-count")).contains("border-radius:", "background:");
        assertThat(selectorBlock(css, ".post--detail .post__body")).contains("line-height:");
        assertThat(selectorBlock(css, ".post__body")).contains(
                "white-space: pre-wrap;",
                "overflow-wrap: anywhere;",
                "word-break: break-word;",
                "color: var(--color-text-strong);");
        assertThat(css).contains("--color-text-strong: #111827;");

        mockMvc.perform(get("/posts/{id}", deleted.getId()).with(client()))
                .andExpect(status().isGone());

        assertThat(postService.latest()).hasSize(3);
    }

    private Post savePost(String author, String body, String backgroundColor) {
        return postRepository.save(new Post(
                author,
                body,
                LocalDateTime.of(2026, 6, 1, 10, 0),
                backgroundColor,
                ClientHashGenerator.hash(OWNER_IP, OWNER_USER_AGENT)));
    }

    private String selectorBlock(String css, String selector) {
        int start = css.indexOf(selector + " {");
        assertThat(start).as("CSS selector %s exists", selector).isNotNegative();
        int end = css.indexOf('}', start);
        assertThat(end).as("CSS selector %s closes", selector).isGreaterThan(start);
        return css.substring(start, end + 1);
    }

    private RequestPostProcessor client() {
        return request -> {
            request.setRemoteAddr(OWNER_IP);
            request.addHeader("User-Agent", OWNER_USER_AGENT);
            return request;
        };
    }
}
