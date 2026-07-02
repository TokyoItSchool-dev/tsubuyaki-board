package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.domain.Reply;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.ReplyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private ReplyRepository replyRepository;

    private PostService postService;

    @Test
    @DisplayName("投稿作成_投稿者名と本文を受け取ったとき_現在時刻付きの投稿を保存する")
    void 投稿作成_投稿者名と本文を受け取ったとき_現在時刻付きの投稿を保存する() {
        Instant fixedInstant = Instant.parse("2026-05-23T10:15:30Z");
        postService = new PostService(postRepository, postLikeRepository, Clock.fixed(fixedInstant, ZoneOffset.UTC));

        postService.createPost("alice", "はじめての投稿");

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().getAuthor()).isEqualTo("alice");
        assertThat(postCaptor.getValue().getBody()).isEqualTo("はじめての投稿");
        assertThat(postCaptor.getValue().getCreatedAt()).isEqualTo(fixedInstant);
    }

    @Test
    @DisplayName("投稿一覧_投稿があるとき_Repositoryの最新50件を返す")
    void 投稿一覧_投稿があるとき_Repositoryの最新50件を返す() {
        postService = new PostService(postRepository, postLikeRepository);
        List<Post> latestPosts = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-05-23T10:00:00Z")),
                new Post("bob", "古い投稿", Instant.parse("2026-05-23T09:00:00Z")));
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(latestPosts);

        List<Post> actual = postService.findLatest50Posts();

        assertThat(actual).isEqualTo(latestPosts);
    }

    @Test
    @DisplayName("投稿一覧_Repositoryがnullを返したとき_空リストを返す")
    void 投稿一覧_Repositoryがnullを返したとき_空リストを返す() {
        postService = new PostService(postRepository, postLikeRepository);
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(null);

        List<Post> actual = postService.findLatest50Posts();

        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("投稿検索_検索語を受け取ったとき_Repositoryの本文部分一致結果を返す")
    void 投稿検索_検索語を受け取ったとき_Repositoryの本文部分一致結果を返す() {
        postService = new PostService(postRepository, postLikeRepository);
        List<Post> searchResults = List.of(
                new Post("alice", "リモート勤務のお知らせ", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("リモート"))
                .willReturn(searchResults);

        List<Post> actual = postService.searchPosts(" リモート ");

        assertThat(actual).isEqualTo(searchResults);
    }

    @Test
    @DisplayName("投稿検索_検索語が空白のとき_最新50件を返す")
    void 投稿検索_検索語が空白のとき_最新50件を返す() {
        postService = new PostService(postRepository, postLikeRepository);
        List<Post> latestPosts = List.of(
                new Post("alice", "最新投稿", Instant.parse("2026-05-23T10:00:00Z")));
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(latestPosts);

        List<Post> actual = postService.searchPosts("   ");

        assertThat(actual).isEqualTo(latestPosts);
    }

    @Test
    @DisplayName("投稿詳細_投稿が削除されていないとき_Repositoryの投稿を返す")
    void 投稿詳細_投稿が削除されていないとき_Repositoryの投稿を返す() {
        postService = new PostService(postRepository, postLikeRepository);
        Post post = new Post("alice", "詳細を表示する投稿", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));

        Optional<Post> actual = postService.findDetailPost(1L);

        assertThat(actual).contains(post);
    }

    @Test
    @DisplayName("投稿詳細_投稿が存在しないまたは削除済みのとき_空を返す")
    void 投稿詳細_投稿が存在しないまたは削除済みのとき_空を返す() {
        postService = new PostService(postRepository, postLikeRepository);
        given(postRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

        Optional<Post> actual = postService.findDetailPost(999L);

        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("いいね状態_同一clientHashのいいねが存在するとき_trueを返す")
    void いいね状態_同一clientHashのいいねが存在するとき_trueを返す() {
        postService = new PostService(postRepository, postLikeRepository);
        given(postLikeRepository.existsByPostIdAndClientHash(1L, "a1b2c3d4")).willReturn(true);

        boolean actual = postService.hasLiked(1L, "a1b2c3d4");

        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("いいね数_投稿IDを受け取ったとき_Repositoryの件数を返す")
    void いいね数_投稿IDを受け取ったとき_Repositoryの件数を返す() {
        postService = new PostService(postRepository, postLikeRepository);
        given(postLikeRepository.countByPostId(1L)).willReturn(2L);

        long actual = postService.countLikes(1L);

        assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("いいねトグル_未いいねのとき_いいねを保存する")
    void いいねトグル_未いいねのとき_いいねを保存する() {
        Instant fixedInstant = Instant.parse("2026-05-23T10:15:30Z");
        postService = new PostService(postRepository, postLikeRepository, Clock.fixed(fixedInstant, ZoneOffset.UTC));
        Post post = new Post("alice", "いいね対象の投稿", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "a1b2c3d4")).willReturn(Optional.empty());

        postService.toggleLike(1L, "a1b2c3d4");

        ArgumentCaptor<PostLike> postLikeCaptor = ArgumentCaptor.forClass(PostLike.class);
        verify(postLikeRepository).save(postLikeCaptor.capture());
        assertThat(postLikeCaptor.getValue().getPost()).isEqualTo(post);
        assertThat(postLikeCaptor.getValue().getClientHash()).isEqualTo("a1b2c3d4");
        assertThat(postLikeCaptor.getValue().getCreatedAt()).isEqualTo(fixedInstant);
    }

    @Test
    @DisplayName("グット取り消し_既にグット済みのとき_既存のいいねを削除する")
    void グット取り消し_既にグット済みのとき_既存のいいねを削除する() {
        postService = new PostService(postRepository, postLikeRepository);
        Post post = new Post("alice", "いいね解除対象の投稿", Instant.parse("2026-05-23T10:00:00Z"));
        PostLike postLike = new PostLike(post, "a1b2c3d4", Instant.parse("2026-05-23T10:01:00Z"));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "a1b2c3d4")).willReturn(Optional.of(postLike));

        postService.toggleLike(1L, "a1b2c3d4");

        verify(postLikeRepository).delete(postLike);
        verify(postLikeRepository, never()).save(org.mockito.ArgumentMatchers.any(PostLike.class));
    }

    @Test
    @DisplayName("投稿削除_投稿が存在するとき_現在時刻で論理削除する")
    void 投稿削除_投稿が存在するとき_現在時刻で論理削除する() {
        Instant fixedInstant = Instant.parse("2026-05-23T10:15:30Z");
        postService = new PostService(postRepository, postLikeRepository, Clock.fixed(fixedInstant, ZoneOffset.UTC));
        Post post = new Post("alice", "削除対象の投稿", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));

        postService.deletePost(1L);

        assertThat(post.getDeletedAt()).isEqualTo(fixedInstant);
    }

    @Test
    @DisplayName("返信作成_投稿IDと本文を受け取ったとき_現在時刻付きの返信を保存する")
    void 返信作成_投稿IDと本文を受け取ったとき_現在時刻付きの返信を保存する() {
        Instant fixedInstant = Instant.parse("2026-05-23T10:15:30Z");
        postService = new PostService(
                postRepository,
                postLikeRepository,
                replyRepository,
                Clock.fixed(fixedInstant, ZoneOffset.UTC));
        Post post = new Post("alice", "返信対象の投稿", Instant.parse("2026-05-23T10:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));

        postService.createReply(1L, null, "bob", "返信本文");

        ArgumentCaptor<Reply> replyCaptor = ArgumentCaptor.forClass(Reply.class);
        verify(replyRepository).save(replyCaptor.capture());
        assertThat(replyCaptor.getValue().getPost()).isEqualTo(post);
        assertThat(replyCaptor.getValue().getParent()).isNull();
        assertThat(replyCaptor.getValue().getAuthor()).isEqualTo("bob");
        assertThat(replyCaptor.getValue().getBody()).isEqualTo("返信本文");
        assertThat(replyCaptor.getValue().getCreatedAt()).isEqualTo(fixedInstant);
    }

    @Test
    @DisplayName("返信作成_親返信IDがあるとき_親返信に紐づけて保存する")
    void 返信作成_親返信IDがあるとき_親返信に紐づけて保存する() {
        postService = new PostService(postRepository, postLikeRepository, replyRepository);
        Post post = new Post("alice", "返信対象の投稿", Instant.parse("2026-05-23T10:00:00Z"));
        Reply parent = new Reply(post, null, "bob", "親返信", Instant.parse("2026-05-23T10:01:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));
        given(replyRepository.findByIdAndPostId(10L, 1L)).willReturn(Optional.of(parent));

        postService.createReply(1L, 10L, "carol", "子返信");

        ArgumentCaptor<Reply> replyCaptor = ArgumentCaptor.forClass(Reply.class);
        verify(replyRepository).save(replyCaptor.capture());
        assertThat(replyCaptor.getValue().getParent()).isEqualTo(parent);
    }

    @Test
    @DisplayName("返信ツリー_親子返信があるとき_親の直下に子返信を深さ付きで返す")
    void 返信ツリー_親子返信があるとき_親の直下に子返信を深さ付きで返す() throws Exception {
        postService = new PostService(postRepository, postLikeRepository, replyRepository);
        Post post = new Post("alice", "返信対象の投稿", Instant.parse("2026-05-23T10:00:00Z"));
        Reply root = new Reply(post, null, "bob", "親返信", Instant.parse("2026-05-23T10:01:00Z"));
        setId(root, 10L);
        Reply otherRoot = new Reply(post, null, "dave", "別の親返信", Instant.parse("2026-05-23T10:02:00Z"));
        setId(otherRoot, 11L);
        Reply child = new Reply(post, root, "carol", "子返信", Instant.parse("2026-05-23T10:03:00Z"));
        setId(child, 12L);
        given(replyRepository.findByPostIdOrderByCreatedAtAscIdAsc(1L))
                .willReturn(List.of(root, otherRoot, child));

        List<ReplyThreadItem> actual = postService.findReplyThread(1L);

        assertThat(actual).extracting(item -> item.reply().getBody())
                .containsExactly("親返信", "子返信", "別の親返信");
        assertThat(actual).extracting(ReplyThreadItem::depth)
                .containsExactly(0, 1, 0);
    }

    @Test
    @DisplayName("返信既読_未読の返信を指定したとき_既読時刻を設定する")
    void 返信既読_未読の返信を指定したとき_既読時刻を設定する() {
        Instant fixedInstant = Instant.parse("2026-05-23T10:15:30Z");
        postService = new PostService(
                postRepository,
                postLikeRepository,
                replyRepository,
                Clock.fixed(fixedInstant, ZoneOffset.UTC));
        Post post = new Post("alice", "返信対象の投稿", Instant.parse("2026-05-23T10:00:00Z"));
        Reply reply = new Reply(post, null, "bob", "返信本文", Instant.parse("2026-05-23T10:01:00Z"));
        given(replyRepository.findByIdAndPostId(10L, 1L)).willReturn(Optional.of(reply));

        postService.toggleReplyRead(1L, 10L);

        assertThat(reply.isRead()).isTrue();
        assertThat(reply.getReadAt()).isEqualTo(fixedInstant);
    }

    private static void setId(Object target, Long id) throws Exception {
        Field idField = target.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(target, id);
    }
}
