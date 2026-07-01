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
    @DisplayName("投稿一覧_51件あるとき_最新50件を新着順で返す")
    void 投稿一覧_51件あるとき_最新50件を新着順で返す() {
        Instant baseTime = Instant.parse("2026-05-23T09:00:00Z");
        List<Post> posts = IntStream.rangeClosed(1, 51)
                .mapToObj(number -> new Post(
                        "user" + number,
                        "post-" + number,
                        baseTime.plusSeconds(number)))
                .toList();
        postRepository.saveAll(posts);

        List<Post> actual = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(actual).hasSize(50);
        assertThat(actual.getFirst().getBody()).isEqualTo("post-51");
        assertThat(actual.getLast().getBody()).isEqualTo("post-2");
        assertThat(actual).extracting(Post::getBody).doesNotContain("post-1");
    }
}
