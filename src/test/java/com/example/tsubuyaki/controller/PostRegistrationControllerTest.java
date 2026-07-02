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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class PostRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("投稿登録_正しい入力_POST_postsで保存しpostsへリダイレクトする")
    void 投稿登録_正しい入力_POST_postsで保存しpostsへリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "本文です")
                        .param("avatarColor", "blue"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        List<Post> posts = postRepository.findAll();
        assertThat(posts).hasSize(1);
        assertThat(posts.getFirst().getAuthor()).isEqualTo("alice");
        assertThat(posts.getFirst().getBody()).isEqualTo("本文です");
        assertThat(posts.getFirst().getAvatarColor()).isEqualTo("blue");
        assertThat(posts.getFirst().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("投稿登録_不正な入力_POST_postsでフォームを再表示しエラーメッセージを表示する")
    void 投稿登録_不正な入力_POST_postsでフォームを再表示しエラーメッセージを表示する() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", " ")
                        .param("body", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("投稿者名を入力してください")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("本文を入力してください")));

        assertThat(postRepository.findAll()).isEmpty();
    }
}
