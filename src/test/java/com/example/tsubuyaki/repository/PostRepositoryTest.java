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
    @DisplayName("投稿一覧_51件以上の投稿がある場合_新着50件だけを返す")
    void 投稿一覧_51件以上の投稿がある場合_新着50件だけを返す() {
        Instant baseTime = Instant.parse("2026-05-23T00:00:00Z");
        IntStream.rangeClosed(1, 51)
                .mapToObj(index -> new Post(
                        "user-" + index,
                        "投稿-" + index,
                        baseTime.plusSeconds(index)
                ))
                .forEach(postRepository::save);

        List<Post> posts = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(posts).hasSize(50);
        assertThat(posts).extracting(Post::getBody)
                .startsWith("投稿-51", "投稿-50", "投稿-49")
                .doesNotContain("投稿-1");
    }

    @Test
    @DisplayName("投稿検索_キーワードを指定した場合_本文をLIKE検索できる")
    void 投稿検索_キーワードを指定した場合_本文をLIKE検索できる() {
        Instant baseTime = Instant.parse("2026-05-23T00:00:00Z");
        postRepository.save(new Post("alice", "検索キーワードを含む投稿", baseTime.plusSeconds(1)));
        postRepository.save(new Post("bob", "一致しない投稿", baseTime.plusSeconds(2)));
        postRepository.save(new Post("carol", "別の検索キーワードを含む投稿", baseTime.plusSeconds(3)));

        List<Post> posts = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("検索キーワード");

        assertThat(posts).extracting(Post::getBody)
                .containsExactly("別の検索キーワードを含む投稿", "検索キーワードを含む投稿");
    }

    @Test
    @DisplayName("投稿登録_アバター色を選択した場合_選択した色を保存する")
    void 投稿登録_アバター色を選択した場合_選択した色を保存する() {
        Post savedPost = postRepository.saveAndFlush(new Post(
                "alice",
                "本文です",
                "purple",
                Instant.parse("2026-05-23T10:00:00Z")
        ));

        Post foundPost = postRepository.findById(savedPost.getId()).orElseThrow();

        assertThat(foundPost.getAvatarColor()).isEqualTo("purple");
    }
}
