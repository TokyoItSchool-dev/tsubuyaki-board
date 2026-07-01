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
    @DisplayName("Repository_51件以上の投稿がある場合_新着50件だけを新着順で返す")
    void Repository_51件以上の投稿がある場合_新着50件だけを新着順で返す() {
        Instant base = Instant.parse("2026-05-23T10:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= 51; i++) {
            posts.add(new Post("user" + i, "投稿" + i, base.plusSeconds(i)));
        }
        postRepository.saveAll(posts);

        List<Post> latest = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(latest).hasSize(50);
        assertThat(latest).extracting(Post::getBody).first().isEqualTo("投稿51");
        assertThat(latest).extracting(Post::getBody).last().isEqualTo("投稿2");
        assertThat(latest).extracting(Post::getBody).doesNotContain("投稿1");
        assertThat(latest).extracting(Post::getCreatedAt).isSortedAccordingTo((left, right) -> right.compareTo(left));
    }
}
