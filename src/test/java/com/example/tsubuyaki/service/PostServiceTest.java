package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

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
    @DisplayName("投稿一覧_Repository例外_例外を上位へ伝搬する")
    void latest_repositoryThrows_propagatesException() {
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc())
                .willThrow(new IllegalStateException("db error"));

        assertThatThrownBy(() -> postService.latest())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("db error");
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
    @DisplayName("投稿検索_本文部分一致_全角半角を区別せず検索する")
    void searchPage_keyword_matchesBodyIgnoringFullHalfWidth() {
        Post matchingPost = new Post("alice", "ＡＢＣを含む本文", Instant.parse("2026-05-23T10:00:00Z"));
        Post otherPost = new Post("bob", "別の本文", Instant.parse("2026-05-23T09:00:00Z"));
        given(postRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(List.of(matchingPost, otherPost));

        Page<Post> result = postService.searchPage("abc", 0);

        assertThat(result.getContent()).containsExactly(matchingPost);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(postRepository).findAllByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿作成_正常値_Entityに詰め替えてDB保存する")
    void create_validValues_savesPostEntity() {
        byte[] avatarImageData = "image".getBytes(java.nio.charset.StandardCharsets.UTF_8);

        postService.create("alice", "hello", "#2563eb", "image/png", avatarImageData);

        verify(postRepository).save(argThat(post ->
                "alice".equals(post.getAuthor())
                        && "hello".equals(post.getBody())
                        && "#2563eb".equals(post.getAvatarColor())
                        && "image/png".equals(post.getAvatarImageContentType())
                        && java.util.Arrays.equals(avatarImageData, post.getAvatarImageData())
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
    @DisplayName("ごみ箱_一覧へ戻す_削除日時を消して通常一覧へ戻す")
    void restoreFromTrash_deletedPost_clearsDeletedAt() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        post.setDeletedAt(Instant.parse("2026-05-23T11:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNotNull(10L)).willReturn(java.util.Optional.of(post));

        assertThat(postService.restoreFromTrash(10L)).contains(post);
        assertThat(post.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("ごみ箱_空にする_削除済み投稿数を返して完全削除する")
    void emptyTrash_deletedPosts_deletesAllDeletedPosts() {
        given(postRepository.countByDeletedAtIsNotNull()).willReturn(2L);

        assertThat(postService.emptyTrash()).isEqualTo(2L);
        verify(postRepository).deleteAllByDeletedAtIsNotNull();
    }

    @Test
    @DisplayName("ごみ箱_空の状態で空にする_削除を実行せず0件を返す")
    void emptyTrash_alreadyEmpty_doesNotDelete() {
        given(postRepository.countByDeletedAtIsNotNull()).willReturn(0L);

        assertThat(postService.emptyTrash()).isZero();
        verify(postRepository, never()).deleteAllByDeletedAtIsNotNull();
    }

    @Test
    @DisplayName("投稿編集_存在する投稿_投稿者と本文を更新する")
    void update_existingPost_updatesAuthorAndBody() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(10L)).willReturn(java.util.Optional.of(post));

        postService.update(10L, "bob", "updated", "#dc2626", null, null);

        assertThat(post.getAuthor()).isEqualTo("bob");
        assertThat(post.getBody()).isEqualTo("updated");
        assertThat(post.getAvatarColor()).isEqualTo("#dc2626");
        assertThat(post.getAvatarImageContentType()).isNull();
        assertThat(post.getAvatarImageData()).isNull();
    }

    @Test
    @DisplayName("投稿編集_画像アバター指定_カラーを消して画像に更新する")
    void update_imageAvatar_updatesAvatarImage() {
        byte[] avatarImageData = "image".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        post.setAvatarColor("#2563eb");
        given(postRepository.findByIdAndDeletedAtIsNull(10L)).willReturn(java.util.Optional.of(post));

        postService.update(10L, "bob", "updated", null, "image/png", avatarImageData);

        assertThat(post.getAvatarColor()).isNull();
        assertThat(post.getAvatarImageContentType()).isEqualTo("image/png");
        assertThat(post.getAvatarImageData()).isEqualTo(avatarImageData);
    }

    @Test
    @DisplayName("いいね_未登録clientHash_いいねを保存する")
    void toggleLike_newClientHash_savesLike() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(10L)).willReturn(java.util.Optional.of(post));
        given(postLikeRepository.findByPostAndClientHash(post, "abcd1234")).willReturn(java.util.Optional.empty());

        postService.toggleLike(10L, "abcd1234");

        verify(postLikeRepository).save(argThat(like ->
                like.getPost().equals(post)
                        && "abcd1234".equals(like.getClientHash())
                        && like.getCreatedAt() != null));
    }

    @Test
    @DisplayName("いいね_同じclientHashで再クリック_いいねを解除する")
    void toggleLike_sameClientHash_deletesLike() {
        Post post = new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"));
        PostLike like = new PostLike(post, "abcd1234", Instant.parse("2026-05-23T11:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(10L)).willReturn(java.util.Optional.of(post));
        given(postLikeRepository.findByPostAndClientHash(post, "abcd1234")).willReturn(java.util.Optional.of(like));

        postService.toggleLike(10L, "abcd1234");

        verify(postLikeRepository).delete(like);
        verify(postLikeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("いいね数_投稿ID指定_Repositoryから件数を取得する")
    void likeCount_requestsRepositoryCount() {
        given(postLikeRepository.countByPostId(10L)).willReturn(2L);

        assertThat(postService.likeCount(10L)).isEqualTo(2L);
        verify(postLikeRepository).countByPostId(10L);
    }
}
