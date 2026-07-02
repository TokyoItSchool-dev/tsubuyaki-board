package com.example.tsubuyaki.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

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
@Transactional
class PostDeleteFeatureTest {

    private static final String OWNER_IP = "203.0.113.10";
    private static final String OWNER_USER_AGENT = "TsubuyakiOwner/1.0";
    private static final String OTHER_IP = "203.0.113.20";
    private static final String OTHER_USER_AGENT = "TsubuyakiOther/1.0";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("投稿削除_clientHash一致時のみ確認画面経由で論理削除し一覧検索詳細から除外する")
    void 投稿削除_clientHash一致時のみ確認画面経由で論理削除し一覧検索詳細から除外する() throws Exception {
        assertThat(columnExists("POSTS", "CLIENT_HASH")).isTrue();
        assertThat(columnExists("POSTS", "DELETED_AT")).isTrue();

        mockMvc.perform(post("/posts")
                        .param("author", "delete-user")
                        .param("body", "delete-body")
                        .with(client(OWNER_IP, OWNER_USER_AGENT)))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        Long postId = jdbcTemplate.queryForObject(
                "select id from posts where author = ? and body = ?",
                Long.class,
                "delete-user",
                "delete-body");
        assertThat(postId).isNotNull();
        assertThat(clientHashOf(postId)).isEqualTo(clientHash(OWNER_IP, OWNER_USER_AGENT));
        assertThat(deletedAtOf(postId)).isZero();

        String deleteConfirmPath = "/posts/" + postId + "/delete-confirm";
        String deletePath = "/posts/" + postId + "/del";

        mockMvc.perform(get("/posts/{id}", postId).with(client(OWNER_IP, OWNER_USER_AGENT)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(content().string(containsString("delete-user")))
                .andExpect(content().string(containsString("delete-body")))
                .andExpect(content().string(containsString("href=\"" + deleteConfirmPath + "\"")))
                .andExpect(content().string(containsString("削除")));

        mockMvc.perform(get("/posts/{id}", postId).with(client(OTHER_IP, OTHER_USER_AGENT)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(content().string(not(containsString("href=\"" + deleteConfirmPath + "\""))));

        mockMvc.perform(get("/posts/{id}/delete-confirm", postId).with(client(OWNER_IP, OWNER_USER_AGENT)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/delete-confirm"))
                .andExpect(content().string(containsString("class=\"app-shell\"")))
                .andExpect(content().string(containsString("class=\"app-header\"")))
                .andExpect(content().string(containsString("社内つぶやきボード")))
                .andExpect(content().string(containsString("href=\"/posts\"")))
                .andExpect(content().string(containsString("href=\"/posts/new\"")))
                .andExpect(content().string(containsString("delete-user")))
                .andExpect(content().string(containsString("delete-body")))
                .andExpect(content().string(containsString("この投稿を削除します。よろしいですか？")))
                .andExpect(content().string(containsString("この操作は取り消せません。")))
                .andExpect(content().string(containsString("action=\"" + deletePath + "\"")))
                .andExpect(content().string(containsString("method=\"post\"")))
                .andExpect(content().string(containsString("class=\"button button--danger post-delete-form__button\"")))
                .andExpect(content().string(containsString("class=\"button button--ghost\"")))
                .andExpect(content().string(containsString("キャンセル")));

        mockMvc.perform(get("/posts/{id}/delete-confirm", postId).with(client(OTHER_IP, OTHER_USER_AGENT)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(content().string(containsString("この投稿は削除できません。")));

        mockMvc.perform(post("/posts/{id}/del", postId).with(client(OTHER_IP, OTHER_USER_AGENT)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(content().string(containsString("この投稿は削除できません。")));
        assertThat(deletedAtOf(postId)).isZero();

        mockMvc.perform(post("/posts/{id}/del", postId).with(client(OWNER_IP, OWNER_USER_AGENT)))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));
        assertThat(deletedAtOf(postId)).isEqualTo(1);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<p class=\"post__body\">delete-body</p>"))));

        mockMvc.perform(get("/posts").param("q", "delete-body"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<p class=\"post__body\">delete-body</p>"))))
                .andExpect(content().string(containsString("条件に一致する結果が見つかりませんでした。")));

        mockMvc.perform(get("/posts/{id}", postId).with(client(OWNER_IP, OWNER_USER_AGENT)))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(content().string(containsString("投稿が見つかりません。")))
                .andExpect(content().string(not(containsString("delete-body"))));
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_name = ? and column_name = ?",
                Integer.class,
                tableName,
                columnName);
        return count != null && count > 0;
    }

    private String clientHashOf(Long postId) {
        return jdbcTemplate.queryForObject(
                "select client_hash from posts where id = ?",
                String.class,
                postId);
    }

    private Integer deletedAtOf(Long postId) {
        return jdbcTemplate.queryForObject(
                "select deleted_at from posts where id = ?",
                Integer.class,
                postId);
    }

    private RequestPostProcessor client(String ip, String userAgent) {
        return request -> {
            request.setRemoteAddr(ip);
            request.addHeader("User-Agent", userAgent);
            return request;
        };
    }

    private String clientHash(String ip, String userAgent) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((ip + ":" + userAgent).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 4);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
