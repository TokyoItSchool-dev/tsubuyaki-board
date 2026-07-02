package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
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

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Test
    @DisplayName("投稿一覧_51件以上の投稿がある場合_新着50件だけを新着順で取得する")
    void 投稿一覧_51件以上の投稿がある場合_新着50件だけを新着順で取得する() {
        Instant base = Instant.parse("2026-05-23T00:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 51; i++) {
            posts.add(new Post("user" + i, "body" + i, "#2563EB", base.plusSeconds(i)));
        }
        postRepository.saveAll(posts);
        postRepository.flush();

        List<Post> latest = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        assertThat(latest).hasSize(50);
        assertThat(latest).extracting(Post::getBody)
                .containsExactlyElementsOf(expectedBodiesFrom50To1());
    }

    @Test
    @DisplayName("投稿検索_本文にキーワードを含む投稿だけを最大50件新着順で取得する")
    void 投稿検索_本文にキーワードを含む投稿だけを最大50件新着順で取得する() {
        Instant base = Instant.parse("2026-05-23T00:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 55; i++) {
            posts.add(new Post("user" + i, "検索対象 body" + i, "#2563EB", base.plusSeconds(i)));
        }
        posts.add(new Post("bob", "対象外 body", "#2563EB", base.plusSeconds(100)));
        postRepository.saveAll(posts);
        postRepository.flush();

        List<Post> found = postRepository.findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc("検索対象");
        long count = postRepository.countByBodyContainingAndDeletedAtIsNull("検索対象");

        assertThat(found).hasSize(50);
        assertThat(count).isEqualTo(55);
        assertThat(found).allSatisfy(post -> assertThat(post.getBody()).contains("検索対象"));
        assertThat(found).extracting(Post::getBody)
                .containsExactlyElementsOf(expectedSearchBodiesFrom54To5());
    }

    @Test
    @DisplayName("投稿一覧_論理削除済み投稿_新着一覧に表示しない")
    void 投稿一覧_論理削除済み投稿_新着一覧に表示しない() {
        Post visible = new Post("alice", "表示する本文", "#2563EB", Instant.parse("2026-05-23T00:00:00Z"));
        Post deleted = new Post("bob", "削除済み本文", "#2563EB", Instant.parse("2026-05-23T00:01:00Z"));
        deleted.delete(Instant.parse("2026-05-23T00:02:00Z"));
        postRepository.saveAll(List.of(visible, deleted));
        postRepository.flush();

        List<Post> latest = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        assertThat(latest).extracting(Post::getBody)
                .contains("表示する本文")
                .doesNotContain("削除済み本文");
    }

    @Test
    @DisplayName("投稿検索_論理削除済み投稿_検索結果と件数に含めない")
    void 投稿検索_論理削除済み投稿_検索結果と件数に含めない() {
        Post visible = new Post("alice", "検索対象 表示", "#2563EB", Instant.parse("2026-05-23T00:00:00Z"));
        Post deleted = new Post("bob", "検索対象 削除済み", "#2563EB", Instant.parse("2026-05-23T00:01:00Z"));
        deleted.delete(Instant.parse("2026-05-23T00:02:00Z"));
        postRepository.saveAll(List.of(visible, deleted));
        postRepository.flush();

        List<Post> found = postRepository.findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc("検索対象");
        long count = postRepository.countByBodyContainingAndDeletedAtIsNull("検索対象");

        assertThat(count).isEqualTo(1);
        assertThat(found).extracting(Post::getBody)
                .containsExactly("検索対象 表示");
    }

    @Test
    @DisplayName("投稿詳細_論理削除済み投稿_id検索で取得できない")
    void 投稿詳細_論理削除済み投稿_id検索で取得できない() {
        Post deleted = new Post("bob", "削除済み本文", "#2563EB", Instant.parse("2026-05-23T00:01:00Z"));
        deleted.delete(Instant.parse("2026-05-23T00:02:00Z"));
        Post saved = postRepository.saveAndFlush(deleted);

        assertThat(postRepository.findByIdAndDeletedAtIsNull(saved.getId())).isEmpty();
        assertThat(postRepository.findById(saved.getId()).orElseThrow().getDeletedAt())
                .isEqualTo(Instant.parse("2026-05-23T00:02:00Z"));
    }

    @Test
    @DisplayName("いいね_投稿idとclientHash_件数と存在有無を取得できる")
    void いいね_投稿IdとclientHash_件数と存在有無を取得できる() {
        Post post = postRepository.save(new Post("alice", "本文", "#2563EB",
                Instant.parse("2026-05-23T00:00:00Z")));
        postLikeRepository.save(new PostLike(post, "abcdef12", Instant.parse("2026-05-23T00:01:00Z")));
        postLikeRepository.save(new PostLike(post, "12345678", Instant.parse("2026-05-23T00:02:00Z")));
        postLikeRepository.flush();

        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(2);
        assertThat(postLikeRepository.existsByPostIdAndClientHash(post.getId(), "abcdef12")).isTrue();
        assertThat(postLikeRepository.existsByPostIdAndClientHash(post.getId(), "87654321")).isFalse();
    }

    @Test
    @DisplayName("いいね_投稿idとclientHash_同一clientHashのいいねを取得できる")
    void いいね_投稿IdとClientHash_同一clientHashのいいねを取得できる() {
        Post post = postRepository.save(new Post("alice", "本文", "#2563EB",
                Instant.parse("2026-05-23T00:00:00Z")));
        PostLike like = postLikeRepository.save(new PostLike(post, "abcdef12", Instant.parse("2026-05-23T00:01:00Z")));
        postLikeRepository.flush();

        assertThat(postLikeRepository.findByPostIdAndClientHash(post.getId(), "abcdef12")).contains(like);
        assertThat(postLikeRepository.findByPostIdAndClientHash(post.getId(), "87654321")).isEmpty();
    }

    @Test
    @DisplayName("投稿作成_選択したアイコン色を投稿データとして保存できる")
    void 投稿作成_選択したアイコン色を投稿データとして保存できる() {
        Post saved = postRepository.save(new Post("alice", "本文", "#F97316",
                Instant.parse("2026-05-23T00:00:00Z")));
        postRepository.flush();

        Post found = postRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getAuthorIconColor()).isEqualTo("#F97316");
    }

    private static List<String> expectedBodiesFrom50To1() {
        List<String> bodies = new ArrayList<>();
        for (int i = 50; i >= 1; i--) {
            bodies.add("body" + i);
        }
        return bodies;
    }

    private static List<String> expectedSearchBodiesFrom54To5() {
        List<String> bodies = new ArrayList<>();
        for (int i = 54; i >= 5; i--) {
            bodies.add("検索対象 body" + i);
        }
        return bodies;
    }
}
