package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("Repository_投稿一覧_最新50件を新着順で返す")
    void 投稿一覧_投稿が55件あるとき_最新50件を新着順で返す() {
        Instant base = Instant.parse("2026-06-26T09:00:00Z");
        List<Post> posts = IntStream.rangeClosed(1, 55)
                .mapToObj(number -> new Post("user" + number, "body" + number, base.plusSeconds(number)))
                .toList();
        postRepository.saveAll(posts);

        List<Post> latestPosts = postRepository.findTop50ByOrderByCreatedAtDesc();

        List<String> expectedBodies = IntStream.iterate(55, number -> number - 1)
                .limit(50)
                .mapToObj(number -> "body" + number)
                .toList();
        assertThat(latestPosts)
                .hasSize(50)
                .extracting(Post::getBody)
                .containsExactlyElementsOf(expectedBodies);
    }

    @Test
    @DisplayName("Repository_投稿登録_投稿を保存できる")
    void 投稿登録_投稿を保存できる() {
        Post post = new Post("alice", "M3 の投稿", "#2563eb", Instant.parse("2026-06-26T10:00:00Z"));

        Post savedPost = postRepository.saveAndFlush(post);

        assertThat(savedPost.getId()).isNotNull();
        assertThat(postRepository.findById(savedPost.getId()))
                .get()
                .extracting(Post::getAuthor, Post::getBody, Post::getAvatarColor, Post::getCreatedAt)
                .containsExactly("alice", "M3 の投稿", "#2563eb", Instant.parse("2026-06-26T10:00:00Z"));
    }

    @Test
    @DisplayName("Repository_投稿詳細_id指定で投稿を取得できる")
    void 投稿詳細_id指定で投稿を取得できる() {
        Post savedPost = postRepository.saveAndFlush(
                new Post("alice", "M4 の詳細投稿", Instant.parse("2026-06-26T11:00:00Z")));

        assertThat(postRepository.findById(savedPost.getId()))
                .get()
                .extracting(Post::getAuthor, Post::getBody, Post::getCreatedAt)
                .containsExactly("alice", "M4 の詳細投稿", Instant.parse("2026-06-26T11:00:00Z"));
    }

    @Test
    @DisplayName("Repository_キーワード検索_body部分一致を新着順で返す")
    void キーワード検索_body部分一致を新着順で返す() {
        postRepository.save(new Post("alice", "朝会で検索機能を相談", Instant.parse("2026-06-26T09:00:00Z")));
        postRepository.save(new Post("bob", "ランチの話題", Instant.parse("2026-06-26T10:00:00Z")));
        postRepository.save(new Post("carol", "検索フォームを実装", Instant.parse("2026-06-26T11:00:00Z")));
        postRepository.flush();

        List<Post> posts = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("検索");

        assertThat(posts)
                .extracting(Post::getBody)
                .containsExactly("検索フォームを実装", "朝会で検索機能を相談");
    }
}
