package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class TagRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("タグ検索_本文中のタグを保存したとき_関連投稿を新着順で返す")
    void findPostsByTagName_本文中のタグを保存したとき_関連投稿を新着順で返す() {
        LocalDateTime base = LocalDateTime.parse("2026-06-30T00:00:00");
        Tag java = new Tag("java");
        Post older = new Post("alice", "研修メモ #java", base.plusSeconds(1));
        older.addTag(java);
        Post newer = new Post("bob", "実装メモ #java #spring", base.plusSeconds(2));
        newer.addTag(java);
        postRepository.save(older);
        postRepository.save(newer);

        List<Post> posts = tagRepository.findPostsByNameOrderByPostCreatedAtDesc("java");

        assertThat(posts).extracting(Post::getBody)
                .containsExactly("実装メモ #java #spring", "研修メモ #java");
    }

    @Test
    @DisplayName("タグ保存_同じ名前を保存したとき_一意制約違反になる")
    void save_同じ名前を保存したとき_一意制約違反になる() {
        tagRepository.saveAndFlush(new Tag("java", LocalDateTime.parse("2026-06-30T00:00:00")));

        assertThatThrownBy(() -> tagRepository.saveAndFlush(
                new Tag("java", LocalDateTime.parse("2026-06-30T00:00:01"))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
