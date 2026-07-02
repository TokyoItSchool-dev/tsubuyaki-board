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
    @DisplayName("投稿一覧_投稿が51件あるとき_最新50件を新着順で返す")
    void findTop50ByOrderByCreatedAtDesc_投稿が51件あるとき_最新50件を新着順で返す() {
        Instant baseTime = Instant.parse("2026-05-23T00:00:00Z");
        List<Post> posts = IntStream.rangeClosed(0, 50)
                .mapToObj(index -> new Post(
                        "user" + index,
                        "body" + index,
                        baseTime.plusSeconds(index)))
                .toList();
        postRepository.saveAll(posts);

        List<Post> actual = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(actual).hasSize(50);
        assertThat(actual).extracting(Post::getBody)
                .containsExactly(IntStream.iterate(50, index -> index - 1)
                        .limit(50)
                        .mapToObj(index -> "body" + index)
                        .toArray(String[]::new));
    }
}
