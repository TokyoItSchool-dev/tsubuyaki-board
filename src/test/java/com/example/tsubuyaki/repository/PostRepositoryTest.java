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
    @DisplayName("投稿一覧_51件以上_新着50件だけを新着順で返す")
    void 投稿一覧_51件以上_新着50件だけを新着順で返す() {
        Instant base = Instant.parse("2026-05-23T00:00:00Z");
        IntStream.rangeClosed(1, 51)
                .mapToObj(index -> new Post("user" + index, "body" + index, base.plusSeconds(index)))
                .forEach(postRepository::save);

        List<Post> posts = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(posts).hasSize(50);
        assertThat(posts).extracting(Post::getBody)
                .startsWith("body51", "body50", "body49")
                .doesNotContain("body1");
    }
}
