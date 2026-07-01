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
    void findTop50ByOrderByCreatedAtDesc_returnsLatest50NewestFirst() {
        Instant base = Instant.parse("2026-05-23T09:00:00Z");
        IntStream.rangeClosed(0, 50)
                .mapToObj(index -> new Post("user" + index, "body" + index, base.plusSeconds(index)))
                .forEach(postRepository::save);

        List<Post> posts = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(posts)
                .hasSize(50)
                .extracting(Post::getBody)
                .startsWith("body50", "body49", "body48")
                .doesNotContain("body0");
    }
}
