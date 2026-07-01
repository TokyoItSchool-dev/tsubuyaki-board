package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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

    @Test
    @DisplayName("投稿一覧_DB空のとき_空メッセージを画面に表示する")
    void 投稿一覧_DB空のとき_空メッセージを画面に表示する() throws Exception {
        List<Post> posts = Collections.emptyList();
        given(postService.findLatestPosts()).willReturn(posts);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", posts))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタンがあり_GET_postsスラッシュへリクエストできる")
    void 投稿一覧_更新ボタンがあり_GET_postsスラッシュへリクエストできる() throws Exception {
        given(postService.findLatestPosts()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/posts/\"")))
                .andExpect(content().string(containsString("method=\"get\"")))
                .andExpect(content().string(containsString("<button type=\"submit\"")))
                .andExpect(content().string(containsString("更新")));

        mockMvc.perform(get("/posts/"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"));
    }

    @Test
    @DisplayName("投稿一覧_投稿者_内容_投稿日の順に表示する")
    void 投稿一覧_投稿者_内容_投稿日の順に表示する() throws Exception {
        List<Post> latestPosts = List.of(
                new Post("alice", "朝会のメモを共有します", LocalDateTime.parse("2026-05-23T10:00:00")));
        given(postService.findLatestPosts()).willReturn(latestPosts);

        MvcResult result = mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", latestPosts))
                .andReturn();

        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        int authorIndex = html.indexOf("alice");
        int bodyIndex = html.indexOf("朝会のメモを共有します");
        int createdAtIndex = html.indexOf("2026-05-23 10:00");

        assertThat(authorIndex).isNotNegative();
        assertThat(bodyIndex).isGreaterThan(authorIndex);
        assertThat(createdAtIndex).isGreaterThan(bodyIndex);
    }

    @Test
    @DisplayName("投稿フォーム_GET_posts_new_入力フォームをビューに渡す")
    void 投稿フォーム_GET_posts_new_入力フォームをビューに渡す() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"))
                .andExpect(model().attribute("postForm", org.hamcrest.Matchers.instanceOf(PostForm.class)));
    }

    @Test
    @DisplayName("投稿登録_正常な入力_投稿を保存してpostsへリダイレクトする")
    void 投稿登録_正常な入力_投稿を保存してpostsへリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "今日の学びを共有します"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"));

        then(postService).should().createPost("alice", "今日の学びを共有します");
    }

    @ParameterizedTest
    @CsvSource({
            "'', 投稿者名を入力してください",
            "'   ', 投稿者名を入力してください",
            "1234567890123456789012345678901, 投稿者名は 30 文字以内で入力してください"
    })
    @DisplayName("投稿登録_author不正_バリデーションエラーでフォームを再表示する")
    void 投稿登録_author不正_バリデーションエラーでフォームを再表示する(String author, String expectedMessage) throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", author)
                        .param("body", "入力済み本文"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
                .andExpect(content().string(containsString("入力済み本文")))
                .andExpect(content().string(containsString(expectedMessage)));
    }

    @ParameterizedTest
    @MethodSource("不正な本文")
    @DisplayName("投稿登録_body不正_バリデーションエラーでフォームを再表示する")
    void 投稿登録_body不正_バリデーションエラーでフォームを再表示する(String body, String expectedMessage) throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "入力済み投稿者")
                        .param("body", body))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasFieldErrors("postForm", "body"))
                .andExpect(content().string(containsString("入力済み投稿者")))
                .andExpect(content().string(containsString(expectedMessage)));
    }

    private static Stream<Arguments> 不正な本文() {
        return Stream.of(
                Arguments.of("", "本文を入力してください"),
                Arguments.of("   ", "本文を入力してください"),
                Arguments.of("x".repeat(281), "本文は 280 文字以内で入力してください"));
    }

    @Test
    @DisplayName("投稿詳細_正常なid_該当投稿の詳細を表示する")
    void 投稿詳細_正常なid_該当投稿の詳細を表示する() throws Exception {
        Post post = new Post("alice", "詳細で表示する本文", LocalDateTime.parse("2026-05-23T10:00:00"));
        given(postService.findPostById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(content().string(containsString("alice")))
                .andExpect(content().string(containsString("詳細で表示する本文")))
                .andExpect(content().string(containsString("2026-05-23 10:00")));
    }

    @Test
    @DisplayName("投稿詳細_存在しないid_404で存在しないページメッセージを表示する")
    void 投稿詳細_存在しないid_404で存在しないページメッセージを表示する() throws Exception {
        given(postService.findPostById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().string(containsString("お探しのページは存在しません")));
    }
}
