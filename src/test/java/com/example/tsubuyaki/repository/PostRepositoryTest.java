package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("投稿一覧_51件以上ある場合_新着50件だけを新着順で返す")
    void 投稿一覧_51件以上ある場合_新着50件だけを新着順で返す() {
        Instant base = Instant.parse("2026-05-23T00:00:00Z");
        IntStream.rangeClosed(0, 50)
                .mapToObj(index -> new Post("user" + index, "body" + index, base.plusSeconds(index)))
                .forEach(postRepository::save);

        var posts = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(posts).hasSize(50);
        assertThat(posts.get(0).getAuthor()).isEqualTo("user50");
        assertThat(posts.get(49).getAuthor()).isEqualTo("user1");
        assertThat(posts).extracting(Post::getAuthor).doesNotContain("user0");
    }

    @Test
    @DisplayName("投稿検索_本文にキーワードを含む投稿_新着順で返す")
    void 投稿検索_本文にキーワードを含む投稿_新着順で返す() {
        postRepository.save(new Post("alice", "AI研修のメモ", Instant.parse("2026-05-23T10:00:00Z")));
        postRepository.save(new Post("bob", "雑談です", Instant.parse("2026-05-23T11:00:00Z")));
        postRepository.save(new Post("carol", "今日のAI活用例", Instant.parse("2026-05-23T12:00:00Z")));

        var posts = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("AI");

        assertThat(posts).extracting(Post::getAuthor).containsExactly("carol", "alice");
    }
}
