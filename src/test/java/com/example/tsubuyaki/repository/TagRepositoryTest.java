package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class TagRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("タグ_保存済みの場合_投稿IDでタグ一覧を取得できる")
    void タグ_保存済みの場合_投稿IDでタグ一覧を取得できる() {
        Post post = postRepository.save(new Post("alice", "#Java のメモ", Instant.parse("2026-05-23T10:00:00Z")));
        tagRepository.save(new Tag(post.getId(), "#Java", Instant.parse("2026-05-23T10:01:00Z")));
        tagRepository.save(new Tag(post.getId(), "#研修", Instant.parse("2026-05-23T10:02:00Z")));

        var tags = tagRepository.findByPostIdInOrderByCreatedAtAscIdAsc(java.util.List.of(post.getId()));

        assertThat(tags).extracting(Tag::getTagName).containsExactly("#Java", "#研修");
    }

    @Test
    @DisplayName("タグ検索_タグ名指定_投稿ID一覧を新着順で取得できる")
    void タグ検索_タグ名指定_投稿ID一覧を新着順で取得できる() {
        Post older = postRepository.save(new Post("alice", "#Java 古い投稿", Instant.parse("2026-05-23T10:00:00Z")));
        Post newer = postRepository.save(new Post("bob", "#Java 新しい投稿", Instant.parse("2026-05-23T11:00:00Z")));
        tagRepository.save(new Tag(older.getId(), "#Java", Instant.parse("2026-05-23T10:01:00Z")));
        tagRepository.save(new Tag(newer.getId(), "#Java", Instant.parse("2026-05-23T11:01:00Z")));

        var postIds = tagRepository.findPostIdsByTagNameOrderByPostCreatedAtDesc("#Java");

        assertThat(postIds).containsExactly(newer.getId(), older.getId());
    }
}
