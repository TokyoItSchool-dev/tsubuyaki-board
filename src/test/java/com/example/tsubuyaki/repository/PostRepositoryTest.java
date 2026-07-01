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
}
