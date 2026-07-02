package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagParser tagParser;

    private PostService postService;

    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2026-05-23T01:23:45Z"),
            ZoneId.of("Asia/Tokyo"));

    @BeforeEach
    void setUp() {
        postService = new PostService(postRepository, tagRepository, tagParser, fixedClock);
    }

    @Test
    @DisplayName("投稿一覧_latest_Repositoryから新着50件を取得する")
    void 投稿一覧_latest_Repositoryから新着50件を取得する() {
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(Collections.emptyList());

        postService.latest();

        verify(postRepository).findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿検索_search_本文キーワードでRepositoryを検索する")
    void 投稿検索_search_本文キーワードでRepositoryを検索する() {
        given(postRepository.findByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("検索"))
                .willReturn(Collections.emptyList());

        postService.search("検索");

        verify(postRepository).findByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("検索");
    }

    @Test
    @DisplayName("投稿作成_create_avatarColorをPostに保存する")
    void 投稿作成_create_avatarColorをPostに保存する() {
        Post savedPost = new Post("alice", "本文です", LocalDateTime.parse("2026-05-23T10:23:45"),
                "blue");
        given(postRepository.save(org.mockito.ArgumentMatchers.any(Post.class))).willReturn(savedPost);
        given(tagParser.extractTags("本文です")).willReturn(List.of());

        postService.create("alice", "本文です", "blue");

        verify(postRepository).save(argThat((Post post) ->
                "alice".equals(post.getAuthor())
                        && "本文です".equals(post.getBody())
                        && "blue".equals(post.getAvatarColor())
                        && LocalDateTime.parse("2026-05-23T10:23:45").equals(post.getCreatedAt())));
        verify(tagRepository).saveAll(List.of());
    }

    @Test
    @DisplayName("投稿作成_create_本文タグをTagとして保存する")
    void 投稿作成_create_本文タグをTagとして保存する() {
        Post savedPost = new Post("alice", "本文です #Java #SpringBoot",
                LocalDateTime.parse("2026-05-23T10:00:00"));
        given(postRepository.save(org.mockito.ArgumentMatchers.any(Post.class))).willReturn(savedPost);
        given(tagParser.extractTags("本文です #Java #SpringBoot")).willReturn(List.of("Java", "SpringBoot"));

        postService.create("alice", "本文です #Java #SpringBoot");

        verify(tagRepository).saveAll(argThat(tags -> {
            List<com.example.tsubuyaki.domain.Tag> tagList = (List<com.example.tsubuyaki.domain.Tag>) tags;
            return tagList.size() == 2
                    && "Java".equals(tagList.get(0).getName())
                    && savedPost.equals(tagList.get(0).getPost())
                    && "SpringBoot".equals(tagList.get(1).getName())
                    && savedPost.equals(tagList.get(1).getPost());
        }));
    }

    @Test
    @DisplayName("タグ一覧_findByTag_TagRepositoryから投稿を取得する")
    void タグ一覧_findByTag_TagRepositoryから投稿を取得する() {
        given(tagRepository.findPostsByNameOrderByCreatedAtDesc("Java")).willReturn(List.of());

        postService.findByTag("Java");

        verify(tagRepository).findPostsByNameOrderByCreatedAtDesc("Java");
        verifyNoInteractions(tagParser);
    }

    @Test
    @DisplayName("タグ検索_searchByTag_TagRepositoryからタグ名部分一致の投稿を取得する")
    void タグ検索_searchByTag_TagRepositoryからタグ名部分一致の投稿を取得する() {
        given(tagRepository.findPostsByNameLikeOrderByCreatedAtDesc("%ava%")).willReturn(List.of());

        postService.searchByTag("ava");

        verify(tagRepository).findPostsByNameLikeOrderByCreatedAtDesc("%ava%");
        verifyNoInteractions(tagParser);
    }

    @Test
    @DisplayName("タグ検索_searchByTag_LIKEワイルドカードをエスケープして検索する")
    void タグ検索_searchByTag_LIKEワイルドカードをエスケープして検索する() {
        given(tagRepository.findPostsByNameLikeOrderByCreatedAtDesc("%100\\%\\_Java\\\\%"))
                .willReturn(List.of());

        postService.searchByTag("100%_Java\\");

        verify(tagRepository).findPostsByNameLikeOrderByCreatedAtDesc("%100\\%\\_Java\\\\%");
        verifyNoInteractions(tagParser);
    }

    @Test
    @DisplayName("投稿削除_delete_投稿のdeletedAtを設定する")
    void 投稿削除_delete_投稿のdeletedAtを設定する() {
        Post post = new Post("alice", "削除対象です", LocalDateTime.parse("2026-05-23T10:00:00"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        postService.delete(1L);

        assertThat(post.getDeletedAt()).isEqualTo(LocalDateTime.parse("2026-05-23T10:23:45"));
    }

    @Test
    @DisplayName("投稿削除_delete_削除済み投稿は404扱いにする")
    void 投稿削除_delete_削除済み投稿は404扱いにする() {
        Post post = new Post("alice", "削除済みです", LocalDateTime.parse("2026-05-23T10:00:00"));
        post.markDeleted(LocalDateTime.parse("2026-05-23T10:10:00"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.delete(1L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
