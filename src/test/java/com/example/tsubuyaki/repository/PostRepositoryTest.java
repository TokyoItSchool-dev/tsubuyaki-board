package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
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
    @DisplayName("投稿一覧_投稿が55件あるとき_新着順で最大50件を返す")
    void 投稿一覧_投稿が55件あるとき_新着順で最大50件を返す() {
        LocalDateTime base = LocalDateTime.parse("2026-05-23T00:00:00");
        IntStream.rangeClosed(1, 55)
                .mapToObj(index -> new Post("user" + index, "body" + index, base.plusSeconds(index)))
                .forEach(postRepository::save);
        postRepository.flush();

        List<Post> posts = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        assertThat(posts).hasSize(50);
        assertThat(posts).extracting(Post::getBody)
                .containsExactly(IntStream.iterate(55, index -> index - 1)
                        .limit(50)
                        .mapToObj(index -> "body" + index)
                        .toArray(String[]::new));
        assertThat(posts).extracting(Post::getBody).doesNotContain("body5");
    }

    @Test
    @DisplayName("投稿検索_本文にキーワードを含む投稿だけを新着順で返す")
    void 投稿検索_本文にキーワードを含む投稿だけを新着順で返す() {
        postRepository.save(new Post("alice", "Springの古い話題", LocalDateTime.parse("2026-05-23T09:00:00")));
        postRepository.save(new Post("bob", "Javaの話題", LocalDateTime.parse("2026-05-23T10:00:00")));
        postRepository.save(new Post("carol", "Springの新しい話題", LocalDateTime.parse("2026-05-23T11:00:00")));
        postRepository.flush();

        List<Post> posts = postRepository.findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc("Spring");

        assertThat(posts).extracting(Post::getBody)
                .containsExactly("Springの新しい話題", "Springの古い話題");
    }

    @Test
    @DisplayName("投稿保存_アバター色を指定したとき_指定色を永続化する")
    void 投稿保存_アバター色を指定したとき_指定色を永続化する() {
        Post savedPost = postRepository.saveAndFlush(
                new Post("alice", "色つき投稿", "#3366cc", LocalDateTime.parse("2026-05-23T09:00:00")));

        Post foundPost = postRepository.findById(savedPost.getId()).orElseThrow();

        assertThat(foundPost.getAvatarColor()).isEqualTo("#3366cc");
    }
}
