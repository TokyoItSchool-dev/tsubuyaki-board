package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostTag;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.PostTagRepository;
import com.example.tsubuyaki.repository.PostApiRow;
import com.example.tsubuyaki.repository.TagRepository;
import com.example.tsubuyaki.service.dto.PostApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private PostTagRepository postTagRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_最新投稿取得_Repositoryの新着50件取得結果を返す")
    void latest_returnsRepositoryLatest50() {
        List<Post> posts = List.of(
                new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postRepository.findLatestIds(PageRequest.of(0, 50))).willReturn(List.of(42L));
        given(postRepository.findAllWithTagsByIdIn(List.of(42L))).willReturn(posts);

        List<Post> actual = postService.latest();

        assertThat(actual).isSameAs(posts);
        verify(postRepository).findLatestIds(PageRequest.of(0, 50));
        verify(postRepository).findAllWithTagsByIdIn(List.of(42L));
    }

    @Test
    @DisplayName("投稿検索_キーワードあり_Repositoryの本文部分一致検索結果を返す")
    void search_whenQueryHasText_returnsRepositorySearchResult() {
        List<Post> posts = List.of(
                new Post("alice", "abcを含む投稿", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postRepository.findByKeywordWithTags("abc")).willReturn(posts);

        List<Post> actual = postService.search("abc");

        assertThat(actual).isSameAs(posts);
        verify(postRepository).findByKeywordWithTags("abc");
        verify(postRepository, never()).findLatestIds(PageRequest.of(0, 50));
    }

    @Test
    @DisplayName("投稿検索_キーワード空_通常の新着50件を返す")
    void search_whenQueryIsEmpty_returnsLatest50() {
        List<Post> posts = List.of(
                new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postRepository.findLatestIds(PageRequest.of(0, 50))).willReturn(List.of(42L));
        given(postRepository.findAllWithTagsByIdIn(List.of(42L))).willReturn(posts);

        List<Post> actual = postService.search("");

        assertThat(actual).isSameAs(posts);
        verify(postRepository).findLatestIds(PageRequest.of(0, 50));
        verify(postRepository).findAllWithTagsByIdIn(List.of(42L));
        verify(postRepository, never()).findByKeywordWithTags(anyString());
    }

    @Test
    @DisplayName("投稿一覧_投稿なし_タグ付き取得を呼ばず空リストを返す")
    void latest_whenNoPostIds_returnsEmptyList() {
        given(postRepository.findLatestIds(PageRequest.of(0, 50))).willReturn(List.of());

        List<Post> actual = postService.latest();

        assertThat(actual).isEmpty();
        verify(postRepository).findLatestIds(PageRequest.of(0, 50));
        verify(postRepository, never()).findAllWithTagsByIdIn(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    @DisplayName("削除一覧_Repositoryの削除済み投稿一覧を返す")
    void deleted_returnsRepositoryDeletedPosts() {
        List<Post> posts = List.of(
                new Post("alice", "削除済み投稿", Instant.parse("2026-05-23T10:00:00Z"))
        );
        given(postRepository.findDeleted()).willReturn(posts);

        List<Post> actual = postService.deleted();

        assertThat(actual).isSameAs(posts);
        verify(postRepository).findDeleted();
    }

    @Test
    @DisplayName("投稿一覧API_最新50件のJOIN行をタグ配列といいね数に変換する")
    void latestForApi_groupsRowsIntoApiResponses() {
        Instant newer = Instant.parse("2026-05-23T03:00:00Z");
        Instant older = Instant.parse("2026-05-23T01:00:00Z");
        List<Long> ids = List.of(2L, 1L);
        given(postRepository.findLatestIds(PageRequest.of(0, 50))).willReturn(ids);
        given(postRepository.findApiRowsByIds(ids)).willReturn(List.of(
                new PostApiRow(2L, "bob", "#spring 新しい投稿", "green", newer, newer, "spring", 2L),
                new PostApiRow(2L, "bob", "#spring 新しい投稿", "green", newer, newer, "java", 2L),
                new PostApiRow(1L, "alice", "古い投稿", "blue", older, older, null, 0L)
        ));

        List<PostApiResponse> actual = postService.latestForApi();

        assertThat(actual).hasSize(2);
        assertThat(actual.get(0).id()).isEqualTo(2L);
        assertThat(actual.get(0).tags()).containsExactly("spring", "java");
        assertThat(actual.get(0).likesCount()).isEqualTo(2L);
        assertThat(actual.get(0).updatedAt()).isEqualTo(newer);
        assertThat(actual.get(1).id()).isEqualTo(1L);
        assertThat(actual.get(1).tags()).isEmpty();
        verify(postRepository).findLatestIds(PageRequest.of(0, 50));
        verify(postRepository).findApiRowsByIds(ids);
    }

    @Test
    @DisplayName("投稿一覧_いいね数_投稿idごとの件数Mapを返す")
    void countLikesByPostIds_returnsCountMap() {
        given(postRepository.findLikeCountsByPostIds(List.of(42L, 43L))).willReturn(List.of(
                new com.example.tsubuyaki.repository.PostLikeCount(42L, 3L),
                new com.example.tsubuyaki.repository.PostLikeCount(43L, 1L)
        ));

        Map<Long, Long> actual = postService.countLikesByPostIds(List.of(42L, 43L));

        assertThat(actual).containsEntry(42L, 3L);
        assertThat(actual).containsEntry(43L, 1L);
        verify(postRepository).findLikeCountsByPostIds(List.of(42L, 43L));
    }

    @DisplayName("投稿作成_入力正常_アバター色を含むPostに変換してRepositoryへ保存する")
    void create_whenValid_savesPostWithAvatarColor() {
        postService.create("alice", "本文です", "purple");

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post savedPost = captor.getValue();
        assertThat(savedPost.getAuthor()).isEqualTo("alice");
        assertThat(savedPost.getBody()).isEqualTo("本文です");
        assertThat(savedPost.getAvatarColor()).isEqualTo("purple");
        assertThat(savedPost.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("投稿詳細_id指定_RepositoryのfindById結果を返す")
    void findById_returnsRepositoryResult() {
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T01:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(42L)).willReturn(Optional.of(post));

        Optional<Post> actual = postService.findById(42L);

        assertThat(actual).containsSame(post);
        verify(postRepository).findByIdAndDeletedAtIsNull(42L);
    }

    @Test
    @DisplayName("タグ一覧_タグ名指定_PostTagRepositoryのタグ別投稿一覧を返す")
    void findByTagName_returnsRepositoryResult() {
        List<Post> posts = List.of(
                new Post("alice", "#java 本文です", Instant.parse("2026-05-23T01:00:00Z"))
        );
        given(postTagRepository.findPostsByTagName("java")).willReturn(posts);

        List<Post> actual = postService.findByTagName("java");

        assertThat(actual).isSameAs(posts);
        verify(postTagRepository).findPostsByTagName("java");
    }

    @Test
    @DisplayName("投稿編集_存在するid_アバター色を含む投稿を更新してRepositoryへ保存する")
    void update_whenPostExists_updatesAndSavesPostWithAvatarColor() {
        Post post = new Post("alice", "更新前本文です", Instant.parse("2026-05-23T01:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(42L)).willReturn(Optional.of(post));

        Optional<Post> actual = postService.update(42L, "bob", "更新後本文です", "green");

        assertThat(actual).containsSame(post);
        assertThat(post.getAuthor()).isEqualTo("bob");
        assertThat(post.getBody()).isEqualTo("更新後本文です");
        assertThat(post.getAvatarColor()).isEqualTo("green");
        verify(postRepository).findByIdAndDeletedAtIsNull(42L);
        verify(postRepository).save(post);
    }

    @Test
    @DisplayName("投稿作成_本文にタグあり_TagとPostTagを保存する")
    void create_whenBodyHasTags_savesTagsAndPostTags() {
        Tag java = new Tag("java");
        Tag spring = new Tag("spring-boot");
        given(tagRepository.findByName("java")).willReturn(Optional.of(java));
        given(tagRepository.findByName("spring-boot")).willReturn(Optional.empty());
        given(tagRepository.save(org.mockito.ArgumentMatchers.any(Tag.class))).willReturn(spring);

        postService.create("alice", "#java #spring-boot #java 本文です", "purple");

        ArgumentCaptor<PostTag> captor = ArgumentCaptor.forClass(PostTag.class);
        verify(postTagRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(postTag -> postTag.getTag().getName())
                .containsExactly("java", "spring-boot");
    }

    @Test
    @DisplayName("投稿編集_本文のタグ変更_既存PostTagを削除して新しいタグを保存する")
    void update_whenBodyTagsChanged_recreatesPostTags() {
        Post post = new Post("alice", "#java 更新前本文です", Instant.parse("2026-05-23T01:00:00Z"));
        ReflectionTestUtils.setField(post, "id", 42L);
        Tag spring = new Tag("spring");
        given(postRepository.findByIdAndDeletedAtIsNull(42L)).willReturn(Optional.of(post));
        given(tagRepository.findByName("spring")).willReturn(Optional.of(spring));

        Optional<Post> actual = postService.update(42L, "alice", "#spring 更新後本文です", "blue");

        assertThat(actual).containsSame(post);
        verify(postTagRepository).deleteByPostId(42L);
        ArgumentCaptor<PostTag> captor = ArgumentCaptor.forClass(PostTag.class);
        verify(postTagRepository).save(captor.capture());
        assertThat(captor.getValue().getPost()).isSameAs(post);
        assertThat(captor.getValue().getTag().getName()).isEqualTo("spring");
    }

    @Test
    @DisplayName("投稿編集_存在しないid_保存せず空を返す")
    void update_whenPostDoesNotExist_returnsEmpty() {
        given(postRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

        Optional<Post> actual = postService.update(999L, "bob", "更新後本文です");

        assertThat(actual).isEmpty();
        verify(postRepository).findByIdAndDeletedAtIsNull(999L);
        verify(postRepository, never()).save(org.mockito.ArgumentMatchers.any(Post.class));
    }

    @Test
    @DisplayName("いいね_投稿が存在する場合_likesCountを1増やして更新後件数を返す")
    void incrementLike_whenPostExists_incrementsLikesCountAndReturnsCount() {
        given(postRepository.incrementLikesCountById(42L)).willReturn(1);
        given(postRepository.findLikesCountById(42L)).willReturn(Optional.of(4L));

        Optional<Long> actual = postService.incrementLike(42L);

        assertThat(actual).contains(4L);
        verify(postRepository).incrementLikesCountById(42L);
        verify(postRepository).findLikesCountById(42L);
    }

    @Test
    @DisplayName("いいね_連続実行_トグルせず毎回likesCount更新を呼ぶ")
    void incrementLike_whenCalledRepeatedly_incrementsEveryTime() {
        given(postRepository.incrementLikesCountById(42L)).willReturn(1);
        given(postRepository.findLikesCountById(42L)).willReturn(Optional.of(1L), Optional.of(2L));

        Optional<Long> first = postService.incrementLike(42L);
        Optional<Long> second = postService.incrementLike(42L);

        assertThat(first).contains(1L);
        assertThat(second).contains(2L);
        verify(postRepository, org.mockito.Mockito.times(2)).incrementLikesCountById(42L);
    }

    @Test
    @DisplayName("いいね_存在しない投稿の場合_更新せず空を返す")
    void incrementLike_whenPostDoesNotExist_returnsEmpty() {
        given(postRepository.incrementLikesCountById(999L)).willReturn(0);

        Optional<Long> actual = postService.incrementLike(999L);

        assertThat(actual).isEmpty();
        verify(postRepository).incrementLikesCountById(999L);
        verify(postRepository, never()).findLikesCountById(999L);
    }

    @Test
    @DisplayName("いいね数_投稿id指定_Repositoryの件数を返す")
    void countLikes_returnsRepositoryCount() {
        given(postRepository.findLikesCountById(42L)).willReturn(Optional.of(3L));

        long actual = postService.countLikes(42L);

        assertThat(actual).isEqualTo(3L);
        verify(postRepository).findLikesCountById(42L);
    }

    @DisplayName("投稿削除_存在するid_deletedAtをセットして保存しtrueを返す")
    void delete_whenPostExists_setsDeletedAtAndReturnsTrue() {
        Post post = new Post("alice", "本文です", Instant.parse("2026-05-23T01:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(42L)).willReturn(Optional.of(post));

        boolean actual = postService.delete(42L);

        assertThat(actual).isTrue();
        assertThat(post.getDeletedAt()).isNotNull();
        verify(postRepository).findByIdAndDeletedAtIsNull(42L);
        verify(postRepository).save(post);
    }

    @Test
    @DisplayName("投稿削除_存在しないid_保存せずfalseを返す")
    void delete_whenPostDoesNotExist_returnsFalse() {
        given(postRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

        boolean actual = postService.delete(999L);

        assertThat(actual).isFalse();
        verify(postRepository).findByIdAndDeletedAtIsNull(999L);
        verify(postRepository, never()).save(org.mockito.ArgumentMatchers.any(Post.class));
    }
}
