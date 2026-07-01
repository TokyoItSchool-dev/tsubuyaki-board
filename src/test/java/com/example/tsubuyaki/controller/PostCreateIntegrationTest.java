package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class PostCreateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("投稿登録_有効な入力_保存して投稿一覧へリダイレクトする")
    void 投稿登録_有効な入力_保存して投稿一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "a")
                        .param("body", "b"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        assertThat(postRepository.findAll())
                .singleElement()
                .satisfies(post -> {
                    assertThat(post.getAuthor()).isEqualTo("a");
                    assertThat(post.getBody()).isEqualTo("b");
                    assertThat(post.getCreatedAt()).isNotNull();
                });
    }

    @ParameterizedTest(name = "{2}")
    @MethodSource("invalidForms")
    @DisplayName("投稿登録_入力エラー_フォームを再表示し保存しない")
    void 投稿登録_入力エラー_フォームを再表示し保存しない(String author, String body, String field) throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", author)
                        .param("body", body))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", field))
                .andExpect(model().attribute("postForm", hasProperty(field, notNullValue())));

        assertThat(postRepository.findAll()).isEmpty();
    }

    static Stream<Arguments> invalidForms() {
        return Stream.of(
                Arguments.of("", "valid body", "author"),
                Arguments.of("   ", "valid body", "author"),
                Arguments.of("a".repeat(31), "valid body", "author"),
                Arguments.of("valid author", "", "body"),
                Arguments.of("valid author", "   ", "body"),
                Arguments.of("valid author", "b".repeat(281), "body")
        );
    }
}
