package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Repository_投稿一覧_新着順で最大50件を返す")
    void findTop50ByOrderByCreatedAtDesc_returnsLatest50InDescendingOrder() {
        Instant base = Instant.parse("2026-06-26T09:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int index = 0; index < 51; index++) {
            posts.add(new Post("user" + index, "body" + index, base.plusSeconds(index)));
        }
        postRepository.saveAll(posts);

        List<Post> latest = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(latest).hasSize(50);
        assertThat(latest).extracting(Post::getBody).containsExactly(
                "body50", "body49", "body48", "body47", "body46", "body45", "body44", "body43", "body42",
                "body41", "body40", "body39", "body38", "body37", "body36", "body35", "body34", "body33",
                "body32", "body31", "body30", "body29", "body28", "body27", "body26", "body25", "body24",
                "body23", "body22", "body21", "body20", "body19", "body18", "body17", "body16", "body15",
                "body14", "body13", "body12", "body11", "body10", "body9", "body8", "body7", "body6",
                "body5", "body4", "body3", "body2", "body1");
    }

    @Test
    @DisplayName("Repository_投稿検索_本文を前後あいまい検索し新着順で返す")
    void findTop50ByBodyContainingOrderByCreatedAtDesc_returnsMatchingPostsInDescendingOrder() {
        Instant base = Instant.parse("2026-06-26T09:00:00Z");
        postRepository.save(new Post("alice", "朝会メモ", base));
        postRepository.save(new Post("bob", "週次の朝会で共有", base.plusSeconds(1)));
        postRepository.save(new Post("carol", "ランチ予定", base.plusSeconds(2)));

        List<Post> results = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("朝会");

        assertThat(results).extracting(Post::getBody).containsExactly("週次の朝会で共有", "朝会メモ");
    }

    @Test
    @DisplayName("Repository_投稿者色取得_色付き投稿があれば同じ投稿者の最新色付き投稿を返す")
    void findFirstByAuthorAndAvatarColorIsNotNullOrderByCreatedAtDesc_returnsLatestColoredPostByAuthor() {
        Instant base = Instant.parse("2026-06-26T09:00:00Z");
        postRepository.save(new Post("alice", "old red", base, "red"));
        postRepository.save(new Post("bob", "blue", base.plusSeconds(1), "blue"));
        postRepository.save(new Post("alice", "no color", base.plusSeconds(2)));

        Optional<Post> result = postRepository.findFirstByAuthorAndAvatarColorIsNotNullOrderByCreatedAtDesc("alice");

        assertThat(result).isPresent();
        assertThat(result.get().getBody()).isEqualTo("old red");
        assertThat(result.get().getAvatarColor()).isEqualTo("red");
    }

    @Test
    @DisplayName("Repository_投稿者色更新_同じ投稿者の投稿だけ指定色に更新する")
    void updateAvatarColorByAuthor_updatesOnlyPostsByAuthor() {
        Instant base = Instant.parse("2026-06-26T09:00:00Z");
        postRepository.save(new Post("alice", "first", base, "red"));
        postRepository.save(new Post("alice", "second", base.plusSeconds(1)));
        postRepository.save(new Post("bob", "other", base.plusSeconds(2), "yellow"));

        int updatedCount = postRepository.updateAvatarColorByAuthor("alice", "blue");

        entityManager.flush();
        entityManager.clear();
        List<Post> posts = postRepository.findAll();
        assertThat(updatedCount).isEqualTo(2);
        assertThat(posts).filteredOn(post -> "alice".equals(post.getAuthor()))
                .extracting(Post::getAvatarColor)
                .containsOnly("blue");
        assertThat(posts).filteredOn(post -> "bob".equals(post.getAuthor()))
                .extracting(Post::getAvatarColor)
                .containsExactly("yellow");
    }
}
