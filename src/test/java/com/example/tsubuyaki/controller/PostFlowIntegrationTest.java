package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PostFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @Test
    @DisplayName("投稿作成_登録成功後_投稿一覧にデータが反映される")
    void 投稿作成_登録成功後_投稿一覧にデータが反映される() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "integration-user")
                        .param("body", "統合テストから登録した本文です")
                        .param("avatarColor", "yellow"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("integration-user")))
                .andExpect(content().string(containsString("post__avatar post__avatar--yellow")))
                .andExpect(content().string(containsString("統合テストから登録した本文です")));
    }

    @Test
    @DisplayName("投稿作成_avatarColor未選択_既定色で一覧と詳細に表示される")
    void 投稿作成_avatarColor未選択_既定色で一覧と詳細に表示される() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "no-color-user")
                        .param("body", "色未選択の投稿です")
                        .param("avatarColor", ""))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no-color-user")))
                .andExpect(content().string(containsString("post__avatar post__avatar--gray")));

        Long postId = postRepository.findAll().get(0).getId();
        mockMvc.perform(get("/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no-color-user")))
                .andExpect(content().string(containsString("post__avatar post__avatar--gray")));
    }

    @Test
    @DisplayName("投稿作成_ハッシュタグあり_タグを重複せず保存しタグ別一覧へ遷移できる")
    void 投稿作成_ハッシュタグあり_タグを重複せず保存しタグ別一覧へ遷移できる() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "tag-user")
                        .param("body", "#Java #java #spring_3 を試す")
                        .param("avatarColor", "blue"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        assertThat(tagRepository.findAll())
                .extracting(tag -> tag.getName())
                .containsExactlyInAnyOrder("java", "spring_3");

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/tags/java\"")))
                .andExpect(content().string(containsString("href=\"/tags/spring_3\"")));

        mockMvc.perform(get("/tags/java"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("tag-user")))
                .andExpect(content().string(containsString("#Java #java #spring_3 を試す")));
    }
}
