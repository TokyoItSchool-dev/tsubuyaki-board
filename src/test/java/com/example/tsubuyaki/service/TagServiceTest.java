package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.TagRepository;
import com.example.tsubuyaki.testsupport.PostTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    @DisplayName("タグ保存_本文のハッシュタグ_TagRepositoryに保存する")
    @SuppressWarnings("unchecked")
    void タグ保存_本文のハッシュタグ_TagRepositoryに保存する() {
        Post post = PostTestFactory.post("alice", "#spring と #Java と #spring");

        tagService.saveTagsFor(post);

        ArgumentCaptor<List<Tag>> captor = ArgumentCaptor.forClass(List.class);
        verify(tagRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(Tag::getName)
                .containsExactly("spring", "Java");
        assertThat(captor.getValue()).extracting(Tag::getPost)
                .containsOnly(post);
    }

    @Test
    @DisplayName("タグ保存_タグなし_TagRepositoryに保存しない")
    void タグ保存_タグなし_TagRepositoryに保存しない() {
        Post post = PostTestFactory.post("alice", "通常の本文");

        tagService.saveTagsFor(post);

        verify(tagRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
    }
}
