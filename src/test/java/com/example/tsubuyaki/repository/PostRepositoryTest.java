package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
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
    @DisplayName("投稿一覧_51件以上の投稿がある場合_新着50件だけを新着順で取得する")
    void 投稿一覧_51件以上の投稿がある場合_新着50件だけを新着順で取得する() {
        Instant base = Instant.parse("2026-05-23T00:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 51; i++) {
            posts.add(new Post("user" + i, "body" + i, base.plusSeconds(i)));
        }
        postRepository.saveAll(posts);
        postRepository.flush();

        List<Post> latest = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(latest).hasSize(50);
        assertThat(latest).extracting(Post::getBody)
                .containsExactlyElementsOf(expectedBodiesFrom50To1());
    }

    private static List<String> expectedBodiesFrom50To1() {
        List<String> bodies = new ArrayList<>();
        for (int i = 50; i >= 1; i--) {
            bodies.add("body" + i);
        }
        return bodies;
    }
}
