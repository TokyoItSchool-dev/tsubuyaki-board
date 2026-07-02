package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostTag;
import com.example.tsubuyaki.domain.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

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

    @Autowired
    private PostTagRepository postTagRepository;

    @Test
    @DisplayName("タグ保存_nameでTagを取得できる")
    void タグ保存_nameでTagを取得できる() {
        tagRepository.save(new Tag("研修"));

        assertThat(tagRepository.findByName("研修"))
                .isPresent()
                .get()
                .extracting(Tag::getName)
                .isEqualTo("研修");
    }

    @Test
    @DisplayName("タグ保存_同一name重複_DB制約で失敗する")
    void タグ保存_同一name重複_Db制約で失敗する() {
        tagRepository.saveAndFlush(new Tag("研修"));

        assertThatThrownBy(() -> tagRepository.saveAndFlush(new Tag("研修")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("投稿タグ保存_投稿に複数タグを関連付けられる")
    void 投稿タグ保存_投稿に複数タグを関連付けられる() {
        Post post = postRepository.save(new Post("alice", "hello #研修 #spring", Instant.parse("2026-05-23T10:00:00Z")));
        Tag training = tagRepository.save(new Tag("研修"));
        Tag spring = tagRepository.save(new Tag("spring"));

        postTagRepository.save(new PostTag(post, training));
        postTagRepository.saveAndFlush(new PostTag(post, spring));

        Post found = postRepository.findById(post.getId()).orElseThrow();
        assertThat(found.getTags()).extracting(Tag::getName)
                .containsExactlyInAnyOrder("研修", "spring");
    }

    @Test
    @DisplayName("投稿タグ保存_同一投稿同一タグ重複_DB制約で失敗する")
    void 投稿タグ保存_同一投稿同一タグ重複_Db制約で失敗する() {
        Post post = postRepository.save(new Post("alice", "hello #研修", Instant.parse("2026-05-23T10:00:00Z")));
        Tag tag = tagRepository.save(new Tag("研修"));
        postTagRepository.saveAndFlush(new PostTag(post, tag));

        assertThatThrownBy(() -> postTagRepository.saveAndFlush(new PostTag(post, tag)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
