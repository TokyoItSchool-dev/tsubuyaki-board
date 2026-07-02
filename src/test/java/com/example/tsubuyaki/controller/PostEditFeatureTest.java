package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.UpdatePostResult;
import com.example.tsubuyaki.web.dto.PostEditForm;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

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
class PostEditFeatureTest {

    private static final String OWNER_IP = "203.0.113.10";
    private static final String OWNER_USER_AGENT = "TsubuyakiOwner/1.0";
    private static final String OTHER_IP = "203.0.113.20";
    private static final String OTHER_USER_AGENT = "TsubuyakiOther/1.0";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("投稿編集_clientHash一致時のみ編集画面を表示し本文と背景色を更新する")
    void 投稿編集_clientHash一致時のみ編集画面を表示し本文と背景色を更新する() throws Exception {
        assertThat(columnExists("POSTS", "UPDATED_AT")).isTrue();
        assertThat(PostEditForm.class).isNotEqualTo(PostForm.class);
        assertThat(Arrays.asList(UpdatePostResult.values()))
                .containsExactly(UpdatePostResult.UPDATED, UpdatePostResult.NOT_FOUND, UpdatePostResult.FORBIDDEN);

        mockMvc.perform(post("/posts")
                        .param("author", "edit-user")
                        .param("body", "edit-before")
                        .param("backgroundColor", "#fee2e2")
                        .with(client(OWNER_IP, OWNER_USER_AGENT)))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        Long postId = jdbcTemplate.queryForObject(
                "select id from posts where author = ? and body = ?",
                Long.class,
                "edit-user",
                "edit-before");
        assertThat(postId).isNotNull();
        assertThat(updatedAtOf(postId)).isNull();

        String editPath = "/posts/" + postId + "/edit";

        mockMvc.perform(get("/posts/{id}", postId).with(client(OWNER_IP, OWNER_USER_AGENT)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(content().string(containsString("href=\"" + editPath + "\"")))
                .andExpect(content().string(containsString("編集")));

        mockMvc.perform(get("/posts/{id}", postId).with(client(OTHER_IP, OTHER_USER_AGENT)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(content().string(not(containsString("href=\"" + editPath + "\""))));

        mockMvc.perform(get("/posts/{id}/edit", postId).with(client(OWNER_IP, OWNER_USER_AGENT)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/edit"))
                .andExpect(model().attributeExists("post", "postEditForm"))
                .andExpect(content().string(containsString("class=\"app-shell\"")))
                .andExpect(content().string(containsString("class=\"app-header\"")))
                .andExpect(content().string(containsString("社内つぶやきボード")))
                .andExpect(content().string(containsString("href=\"/posts\"")))
                .andExpect(content().string(containsString("href=\"/posts/new\"")))
                .andExpect(content().string(containsString("edit-user")))
                .andExpect(content().string(containsString("edit-before")))
                .andExpect(content().string(containsString("本文は必須・280文字以内です。")))
                .andExpect(content().string(containsString("aria-describedby=\"body-help body-error\"")))
                .andExpect(content().string(containsString("class=\"button button--primary\"")))
                .andExpect(content().string(containsString("class=\"button button--ghost\"")))
                .andExpect(content().string(containsString("value=\"#fee2e2\"")))
                .andExpect(content().string(not(containsString("name=\"author\""))))
                .andExpect(content().string(containsString("action=\"" + editPath + "\"")))
                .andExpect(content().string(containsString("method=\"post\"")));

        mockMvc.perform(get("/posts/{id}/edit", postId).with(client(OTHER_IP, OTHER_USER_AGENT)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(content().string(containsString("この投稿は編集できません。")));

        mockMvc.perform(post("/posts/{id}/edit", postId)
                        .param("body", "edited-by-other")
                        .param("backgroundColor", "#dbeafe")
                        .with(client(OTHER_IP, OTHER_USER_AGENT)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/edit"))
                .andExpect(content().string(containsString("この投稿は編集できません。")));
        assertThat(bodyOf(postId)).isEqualTo("edit-before");
        assertThat(backgroundColorOf(postId)).isEqualTo("#fee2e2");
        assertThat(updatedAtOf(postId)).isNull();

        mockMvc.perform(post("/posts/{id}/edit", postId)
                        .param("body", " ")
                        .param("backgroundColor", "#dbeafe")
                        .with(client(OWNER_IP, OWNER_USER_AGENT)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/edit"))
                .andExpect(model().attributeHasFieldErrors("postEditForm", "body"))
                .andExpect(content().string(containsString("本文を入力してください")));

        mockMvc.perform(post("/posts/{id}/edit", postId)
                        .param("body", "x".repeat(281))
                        .param("backgroundColor", "#dbeafe")
                        .with(client(OWNER_IP, OWNER_USER_AGENT)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/edit"))
                .andExpect(model().attributeHasFieldErrors("postEditForm", "body"))
                .andExpect(content().string(containsString("本文は 280 文字以内で入力してください")));

        mockMvc.perform(post("/posts/{id}/edit", postId)
                        .param("body", "edit-after")
                        .param("backgroundColor", "#dbeafe")
                        .with(client(OWNER_IP, OWNER_USER_AGENT)))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/" + postId));

        assertThat(bodyOf(postId)).isEqualTo("edit-after");
        assertThat(backgroundColorOf(postId)).isEqualTo("#dbeafe");
        assertThat(updatedAtOf(postId)).isNotNull();

        mockMvc.perform(get("/posts/{id}", postId).with(client(OWNER_IP, OWNER_USER_AGENT)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("edit-after")))
                .andExpect(content().string(containsString("編集済み")))
                .andExpect(content().string(not(containsString("edit-before"))));

        mockMvc.perform(post("/posts/{id}/del", postId).with(client(OWNER_IP, OWNER_USER_AGENT)))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        mockMvc.perform(get("/posts/{id}/edit", postId).with(client(OWNER_IP, OWNER_USER_AGENT)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(content().string(containsString("投稿が見つかりません。")));
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_name = ? and column_name = ?",
                Integer.class,
                tableName,
                columnName);
        return count != null && count > 0;
    }

    private String bodyOf(Long postId) {
        return jdbcTemplate.queryForObject("select body from posts where id = ?", String.class, postId);
    }

    private String backgroundColorOf(Long postId) {
        return jdbcTemplate.queryForObject("select background_color from posts where id = ?", String.class, postId);
    }

    private Object updatedAtOf(Long postId) {
        return jdbcTemplate.queryForObject("select updated_at from posts where id = ?", Object.class, postId);
    }

    private RequestPostProcessor client(String ip, String userAgent) {
        return request -> {
            request.setRemoteAddr(ip);
            request.addHeader("User-Agent", userAgent);
            return request;
        };
    }
}
