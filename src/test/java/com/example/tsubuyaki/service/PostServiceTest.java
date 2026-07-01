package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_51件以上の投稿がある場合_新着50件しか返さない")
    void latest_overFiftyPosts_returnsOnlyLatestFifty() {
        List<Post> latestFifty = IntStream.rangeClosed(1, 50)
                .mapToObj(index -> new Post("user" + index, "body" + index,
                        Instant.parse("2026-05-23T10:00:00Z").plusSeconds(index)))
                .toList();
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(latestFifty);

        List<Post> result = postService.latest();

        assertThat(result).hasSize(50);
        assertThat(result).isSameAs(latestFifty);
        verify(postRepository).findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿一覧_ページ指定_指定ページを50件単位でRepositoryへ問い合わせる")
    void latestPage_requestsRepositoryByFiftyPostsPage() {
        Page<Post> page = new PageImpl<>(List.of(), PageRequest.of(2, 50), 102);
        given(postRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc(PageRequest.of(2, 50))).willReturn(page);

        Page<Post> result = postService.latestPage(2);

        assertThat(result).isSameAs(page);
        verify(postRepository).findAllByDeletedAtIsNullOrderByCreatedAtDesc(PageRequest.of(2, 50));
    }

    @Test
    @DisplayName("投稿作成_正常値_Entityに詰め替えてDB保存する")
    void create_validValues_savesPostEntity() {
        postService.create("alice", "hello");

        verify(postRepository).save(argThat(post ->
                "alice".equals(post.getAuthor())
                        && "hello".equals(post.getBody())
                        && post.getCreatedAt() != null));
    }

    @Test
    @DisplayName("投稿詳細_ID指定_削除されていない投稿だけ取得する")
    void findVisibleById_requestsNotDeletedPost() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(10L)).willReturn(java.util.Optional.of(post));

        assertThat(postService.findVisibleById(10L)).contains(post);
        verify(postRepository).findByIdAndDeletedAtIsNull(10L);
    }

    @Test
    @DisplayName("投稿削除_存在する投稿_DBから消さず削除日時を入れてごみ箱へ移す")
    void moveToTrash_existingPost_setsDeletedAtWithoutPhysicalDelete() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(10L)).willReturn(java.util.Optional.of(post));

        postService.moveToTrash(10L);

        assertThat(post.getDeletedAt()).isNotNull();
        verify(postRepository, never()).delete(post);
    }

    @Test
    @DisplayName("ごみ箱_一覧_削除済み投稿を削除日時の新しい順で取得する")
    void trashedPosts_requestsDeletedPosts() {
        List<Post> posts = List.of(new Post("alice", "deleted", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findAllByDeletedAtIsNotNullOrderByDeletedAtDesc()).willReturn(posts);

        assertThat(postService.trashedPosts()).isSameAs(posts);
        verify(postRepository).findAllByDeletedAtIsNotNullOrderByDeletedAtDesc();
    }

    @Test
    @DisplayName("投稿編集_存在する投稿_投稿者と本文を更新する")
    void update_existingPost_updatesAuthorAndBody() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(10L)).willReturn(java.util.Optional.of(post));

        postService.update(10L, "bob", "updated");

        assertThat(post.getAuthor()).isEqualTo("bob");
        assertThat(post.getBody()).isEqualTo("updated");
    }
}
