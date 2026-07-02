package com.example.tsubuyaki.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TagExtractorTest {

    private final TagExtractor tagExtractor = new TagExtractor();

    @Test
    @DisplayName("タグ抽出_本文中のタグ_空白または記号までをタグ名として抽出する")
    void extract_whenBodyContainsTags_returnsTagNames() {
        assertThat(tagExtractor.extract("今日は #java と #spring-boot, #AI。"))
                .containsExactly("java", "spring-boot", "AI");
    }

    @Test
    @DisplayName("タグ抽出_同一タグが複数回_1件として扱う")
    void extract_whenDuplicatedTags_returnsUniqueNames() {
        assertThat(tagExtractor.extract("#java #java #spring #java"))
                .containsExactly("java", "spring");
    }
}
