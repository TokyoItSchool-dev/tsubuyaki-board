package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostLikeRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Test
    @DisplayName("いいねRepository_投稿idとclientHash_保存済みいいねを検索できる")
    void いいねRepository_投稿idとclientHash_保存済みいいねを検索できる() {
        // いいねの外部キーとして利用する投稿をDBへ保存する。
        Post savedPost = postRepository.save(new Post(
                "alice",
                "Repositoryテストの投稿",
                LocalDateTime.parse("2026-06-01T09:00:00")));
        // 検索対象になるclientHashを持ついいねをDBへ保存する。
        postLikeRepository.save(new PostLike(
                savedPost,
                "abcd1234",
                LocalDateTime.parse("2026-06-01T10:00:00")));

        // 投稿idとclientHashで、保存済みのいいねを取得できることを検証する。
        assertThat(postLikeRepository.findByPostIdAndClientHash(savedPost.getId(), "abcd1234")).isPresent();
        // 投稿idに紐づくいいね件数が1件として数えられることを検証する。
        assertThat(postLikeRepository.countByPostId(savedPost.getId())).isEqualTo(1);
        // 保存済みclientHashの存在確認がtrueになることを検証する。
        assertThat(postLikeRepository.existsByPostIdAndClientHash(savedPost.getId(), "abcd1234")).isTrue();
        // 未保存clientHashの存在確認がfalseになることを検証する。
        assertThat(postLikeRepository.existsByPostIdAndClientHash(savedPost.getId(), "zzzz9999")).isFalse();
    }

    @Test
    @DisplayName("投稿Repository_ロック付きid検索_対象投稿を取得できる")
    void 投稿Repository_ロック付きid検索_対象投稿を取得できる() {
        // いいねトグルの直列化でロック取得する対象投稿をDBへ保存する。
        Post savedPost = postRepository.save(new Post(
                "alice",
                "ロック付き検索の投稿",
                LocalDateTime.parse("2026-06-01T09:00:00")));

        // 悲観ロック付き検索でも、通常のid検索と同じ投稿を取得できることを検証する。
        assertThat(postRepository.findByIdForUpdate(savedPost.getId()))
                .isPresent()
                .get()
                .extracting(Post::getBody)
                .isEqualTo("ロック付き検索の投稿");
    }
}
