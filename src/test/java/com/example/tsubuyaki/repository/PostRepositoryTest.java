package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("投稿一覧_51件以上の投稿_新着50件だけをcreated_at降順で返す")
    void 投稿一覧_51件以上の投稿_新着50件だけをCreatedAt降順で返す() {
        LocalDateTime base = LocalDateTime.parse("2026-06-26T00:00:00");
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= 51; i++) {
            posts.add(new Post("user-" + i, "body-" + i, base.plusSeconds(i)));
        }
        postRepository.saveAll(posts);

        List<Post> latestPosts = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(latestPosts).hasSize(50);
        assertThat(latestPosts).extracting(Post::getAuthor)
                .startsWith("user-51")
                .endsWith("user-2")
                .doesNotContain("user-1");
    }

    @Test
    @DisplayName("投稿検索_bodyにキーワードを含む投稿だけをcreated_at降順で返す")
    void 投稿検索_bodyにキーワードを含む投稿だけをCreatedAt降順で返す() {
        postRepository.saveAll(List.of(
                new Post("alice", "週次の共有です", LocalDateTime.parse("2026-06-26T09:00:00")),
                new Post("bob", "検索対象の共有です", LocalDateTime.parse("2026-06-26T11:00:00")),
                new Post("carol", "検索対象の古い共有です", LocalDateTime.parse("2026-06-26T10:00:00"))));

        List<Post> searchResults = postRepository.findByBodyContainingOrderByCreatedAtDesc("検索対象");

        assertThat(searchResults).extracting(Post::getAuthor)
                .containsExactly("bob", "carol");
    }

    @Test
    @DisplayName("投稿検索_SQLインジェクション文字列_検索条件を文字列値として扱う")
    void 投稿検索_SQLインジェクション文字列_検索条件を文字列値として扱う() {
        postRepository.saveAll(List.of(
                new Post("alice", "通常の投稿です", LocalDateTime.parse("2026-06-26T09:00:00")),
                new Post("bob", "別の投稿です", LocalDateTime.parse("2026-06-26T10:00:00"))));

        List<Post> searchResults = postRepository.findByBodyContainingOrderByCreatedAtDesc("' OR '1'='1");

        assertThat(searchResults).isEmpty();
    }
}
