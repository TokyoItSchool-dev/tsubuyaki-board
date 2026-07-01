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
    @DisplayName("投稿一覧_51件以上_新着50件だけを返す")
    void 投稿一覧_51件以上_新着50件だけを返す() {
        postRepository.deleteAll();
        postRepository.saveAll(postsWithSequentialCreatedAt(51));

        List<Post> latestPosts = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(latestPosts).hasSize(50);
        assertThat(latestPosts).extracting(Post::getBody)
                .startsWith("body50", "body49", "body48")
                .doesNotContain("body0");
    }

    private List<Post> postsWithSequentialCreatedAt(int count) {
        Instant base = Instant.parse("2026-05-23T00:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            posts.add(new Post("user" + i, "body" + i, base.plusSeconds(i)));
        }
        return posts;
    }
}
