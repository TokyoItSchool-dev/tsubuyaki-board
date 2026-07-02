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

    @Test
    @DisplayName("投稿検索_本文にキーワードを含む投稿_新着順で返す")
    void 投稿検索_本文にキーワードを含む投稿_新着順で返す() {
        postRepository.deleteAll();
        postRepository.save(new Post("alice", "朝会の共有です", Instant.parse("2026-05-23T10:00:00Z")));
        postRepository.save(new Post("bob", "ランチの話です", Instant.parse("2026-05-23T11:00:00Z")));
        postRepository.save(new Post("carol", "夕会の共有です", Instant.parse("2026-05-23T12:00:00Z")));

        List<Post> posts = postRepository.findTop50ByBodyContainingIgnoreCaseOrderByCreatedAtDesc("共有");

        assertThat(posts).extracting(Post::getBody)
                .containsExactly("夕会の共有です", "朝会の共有です");
    }

    @Test
    @DisplayName("投稿検索_本文に含まない投稿_返さない")
    void 投稿検索_本文に含まない投稿_返さない() {
        postRepository.deleteAll();
        postRepository.save(new Post("alice", "朝会の共有です", Instant.parse("2026-05-23T10:00:00Z")));

        List<Post> posts = postRepository.findTop50ByBodyContainingIgnoreCaseOrderByCreatedAtDesc("障害対応");

        assertThat(posts).isEmpty();
    }

    @Test
    @DisplayName("投稿保存_アバター色あり_保存後に同じ色を取得できる")
    void 投稿保存_アバター色あり_保存後に同じ色を取得できる() {
        postRepository.deleteAll();
        Post saved = postRepository.save(new Post("alice", "色付き投稿です", "orange",
                Instant.parse("2026-05-23T10:00:00Z")));

        Post post = postRepository.findById(saved.getId()).orElseThrow();

        assertThat(post.getAvatarColor()).isEqualTo("orange");
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
