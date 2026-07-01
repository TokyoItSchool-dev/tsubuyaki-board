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
    @DisplayName("投稿一覧_51件以上あるとき_新着50件だけを返す")
    void findTop50ByOrderByCreatedAtDesc_whenMoreThan50Posts_returnsLatest50() {
        Instant baseTime = Instant.parse("2026-05-23T10:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int index = 1; index <= 51; index++) {
            posts.add(new Post("user" + index, "body" + index, baseTime.plusSeconds(index)));
        }
        postRepository.saveAll(posts);

        List<Post> latestPosts = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(latestPosts).hasSize(50);
        assertThat(latestPosts.get(0).getAuthor()).isEqualTo("user51");
        assertThat(latestPosts.get(49).getAuthor()).isEqualTo("user2");
        assertThat(latestPosts).extracting(Post::getAuthor).doesNotContain("user1");
    }
}
