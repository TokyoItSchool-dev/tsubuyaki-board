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
    @DisplayName("投稿一覧_51件以上の投稿がある場合_新着50件だけを返す")
    void findTop50ByOrderByCreatedAtDesc_moreThan51_returnsLatest50() {
        Instant base = Instant.parse("2026-05-23T00:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= 51; i++) {
            posts.add(new Post("user" + i, "body" + i, base.plusSeconds(i)));
        }
        postRepository.saveAll(posts);

        List<Post> latestPosts = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(latestPosts).hasSize(50);
        assertThat(latestPosts.get(0).getBody()).isEqualTo("body51");
        assertThat(latestPosts.get(49).getBody()).isEqualTo("body2");
        assertThat(latestPosts).extracting(Post::getBody).doesNotContain("body1");
    }

    @Test
    @DisplayName("投稿検索_本文にキーワードを含む投稿だけを新着順で返す")
    void findByBodyContainingOrderByCreatedAtDesc_returnsMatchingPosts() {
        postRepository.saveAll(List.of(
                new Post("alice", "abcを含む古い投稿", Instant.parse("2026-05-23T01:00:00Z")),
                new Post("bob", "含まない投稿", Instant.parse("2026-05-23T02:00:00Z")),
                new Post("carol", "新しいabc投稿", Instant.parse("2026-05-23T03:00:00Z"))
        ));

        List<Post> posts = postRepository.findByBodyContainingOrderByCreatedAtDesc("abc");

        assertThat(posts)
                .extracting(Post::getBody)
                .containsExactly("新しいabc投稿", "abcを含む古い投稿");
    }

    @Test
    @DisplayName("投稿編集_保存済み投稿を更新すると変更後の値で取得できる")
    void save_whenExistingPostUpdated_persistsUpdatedValues() {
        Post post = postRepository.save(new Post(
                "alice", "更新前本文です", Instant.parse("2026-05-23T00:00:00Z")));

        post.update("bob", "更新後本文です");
        postRepository.saveAndFlush(post);

        assertThat(postRepository.findById(post.getId()))
                .get()
                .satisfies(updated -> {
                    assertThat(updated.getAuthor()).isEqualTo("bob");
                    assertThat(updated.getBody()).isEqualTo("更新後本文です");
                    assertThat(updated.getCreatedAt()).isEqualTo(Instant.parse("2026-05-23T00:00:00Z"));
                });
    }
}
