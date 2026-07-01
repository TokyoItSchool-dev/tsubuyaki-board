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
            posts.add(new Post((long) index, "user" + index, "body" + index, baseTime.plusSeconds(index)));
        }
        postRepository.saveAll(posts);

        List<Post> latestPosts = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(latestPosts).hasSize(50);
        assertThat(latestPosts.get(0).getAuthor()).isEqualTo("user51");
        assertThat(latestPosts.get(49).getAuthor()).isEqualTo("user2");
        assertThat(latestPosts).extracting(Post::getAuthor).doesNotContain("user1");
    }

    @Test
    @DisplayName("投稿登録_最大長の投稿者本文投稿日を保存したとき_IDと各項目をDBに登録できる")
    void save_whenAuthorAndBodyAreMaxLength_registersIdAuthorBodyAndCreatedAt() {
        String author = "a".repeat(30);
        String body = "b".repeat(280);
        Instant createdAt = Instant.parse("2026-05-23T10:00:00Z");
        postRepository.saveAndFlush(new Post(10L, "existing", "body", createdAt.minusSeconds(1)));

        Post saved = postRepository.saveAndFlush(new Post(11L, author, body, createdAt));

        assertThat(saved.getId()).isEqualTo(11L);
        assertThat(postRepository.findById(saved.getId()))
                .get()
                .satisfies(post -> {
                    assertThat(post.getId()).isEqualTo(11L);
                    assertThat(post.getAuthor()).isEqualTo(author);
                    assertThat(post.getBody()).isEqualTo(body);
                    assertThat(post.getCreatedAt()).isEqualTo(createdAt);
                });
    }

    @Test
    @DisplayName("投稿登録_既存投稿があるとき_最大IDを返す")
    void findMaxId_whenPostsExist_returnsMaxId() {
        Instant createdAt = Instant.parse("2026-05-23T10:00:00Z");
        postRepository.saveAndFlush(new Post(10L, "alice", "hello", createdAt));
        postRepository.saveAndFlush(new Post(15L, "bob", "hi", createdAt.plusSeconds(1)));

        Long maxId = postRepository.findMaxId();

        assertThat(maxId).isEqualTo(15L);
    }

    @Test
    @DisplayName("投稿登録_既存投稿がないとき_最大IDは0を返す")
    void findMaxId_whenNoPosts_returnsZero() {
        Long maxId = postRepository.findMaxId();

        assertThat(maxId).isZero();
    }
}
