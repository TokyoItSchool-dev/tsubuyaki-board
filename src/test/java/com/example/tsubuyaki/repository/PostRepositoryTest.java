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
    @DisplayName("投稿一覧_51件以上ある場合_新着50件だけをcreated_at降順で返す")
    void 投稿一覧_51件以上ある場合_新着50件だけをcreatedAt降順で返す() {
        Instant base = Instant.parse("2026-06-26T00:00:00Z");
        IntStream.rangeClosed(1, 51)
                .mapToObj(index -> new Post("user" + index, "body" + index, base.plusSeconds(index)))
                .forEach(postRepository::save);
        postRepository.flush();

        List<Post> posts = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(posts).hasSize(50);
        assertThat(posts.get(0).getAuthor()).isEqualTo("user51");
        assertThat(posts.get(49).getAuthor()).isEqualTo("user2");
        assertThat(posts).isSortedAccordingTo(
                (left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()));
    }

    @Test
    @DisplayName("キーワード検索_本文にキーワードを含む投稿のみcreatedAt降順で返す")
    void キーワード検索_本文にキーワードを含む投稿のみcreatedAt降順で返す() {
        postRepository.save(new Post("alice", "Spring Boot のメモ", Instant.parse("2026-07-01T05:00:00Z")));
        postRepository.save(new Post("bob", "Oracle DB のメモ", Instant.parse("2026-07-01T06:00:00Z")));
        postRepository.save(new Post("carol", "Spring Data JPA のメモ", Instant.parse("2026-07-01T07:00:00Z")));
        postRepository.flush();

        List<Post> posts = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("Spring");

        assertThat(posts).extracting(Post::getAuthor)
                .containsExactly("carol", "alice");
        assertThat(posts).extracting(Post::getBody)
                .doesNotContain("Oracle DB のメモ");
    }

    @Test
    @DisplayName("投稿者名フィールド拡張_avatarColorを保存して取得できる")
    void 投稿者名フィールド拡張_avatarColorを保存して取得できる() {
        Post saved = postRepository.saveAndFlush(
                new Post("alice", "色つき投稿", Instant.parse("2026-07-01T05:00:00Z"), "red"));

        Post actual = postRepository.findById(saved.getId()).orElseThrow();

        assertThat(actual.getAvatarColor()).isEqualTo("red");
    }
}
