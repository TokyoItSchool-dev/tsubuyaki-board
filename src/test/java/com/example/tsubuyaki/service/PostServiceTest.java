package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @InjectMocks
    private PostService postService;

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
        Post savedPost = new Post("alice", "本文です", java.time.LocalDateTime.parse("2026-05-23T10:00:00"),
                "blue");
        given(postRepository.save(org.mockito.ArgumentMatchers.any(Post.class))).willReturn(savedPost);
        given(tagParser.extractTags("本文です")).willReturn(List.of());

        postService.create("alice", "本文です", "blue");

        verify(postRepository).save(argThat((Post post) ->
                "alice".equals(post.getAuthor())
                        && "本文です".equals(post.getBody())
                        && "blue".equals(post.getAvatarColor())
                        && post.getCreatedAt() != null));
        verify(tagRepository).saveAll(List.of());
    }

    @Test
    @DisplayName("投稿作成_create_本文タグをTagとして保存する")
    void 投稿作成_create_本文タグをTagとして保存する() {
        Post savedPost = new Post("alice", "本文です #Java #SpringBoot",
                java.time.LocalDateTime.parse("2026-05-23T10:00:00"));
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
        given(tagRepository.findPostsByNameContainingOrderByCreatedAtDesc("ava")).willReturn(List.of());

        postService.searchByTag("ava");

        verify(tagRepository).findPostsByNameContainingOrderByCreatedAtDesc("ava");
        verifyNoInteractions(tagParser);
    }

    @Test
    @DisplayName("投稿削除_delete_投稿のdeletedAtを設定する")
    void 投稿削除_delete_投稿のdeletedAtを設定する() {
        Post post = new Post("alice", "削除対象です", java.time.LocalDateTime.parse("2026-05-23T10:00:00"));
        given(postRepository.findById(1L)).willReturn(java.util.Optional.of(post));

        postService.delete(1L);

        assertThat(post.getDeletedAt()).isNotNull();
    }
}
