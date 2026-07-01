package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.User;
import com.example.tsubuyaki.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("本文検索_キーワードを含む投稿だけ_新着順で返す")
    void 本文検索_キーワードを含む投稿だけ_新着順で返す() {
        Post oldMatch = postRepository.save(new Post("alice", "xxx を含む古い投稿",
                Instant.parse("2026-05-23T09:00:00Z")));
        postRepository.save(new Post("bob", "一致しない投稿",
                Instant.parse("2026-05-23T10:00:00Z")));
        Post newMatch = postRepository.save(new Post("carol", "新しい xxx 投稿",
                Instant.parse("2026-05-23T11:00:00Z")));

        List<Post> posts = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("xxx");

        assertThat(posts).containsExactly(newMatch, oldMatch);
    }

    @Test
    @DisplayName("投稿保存_Userを指定したとき_userId経由でアバター色を参照できる")
    void 投稿保存_Userを指定したとき_userId経由でアバター色を参照できる() {
        User user = userRepository.save(new User("alice", "#ef4444"));
        Post saved = postRepository.save(new Post(user, "色付き投稿",
                Instant.parse("2026-05-23T10:00:00Z")));
        postRepository.flush();

        Post found = postRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getUser().getId()).isEqualTo(user.getId());
        assertThat(found.getAvatarColor()).isEqualTo("#ef4444");
    }
}
