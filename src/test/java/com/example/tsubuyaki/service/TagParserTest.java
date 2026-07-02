package com.example.tsubuyaki.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TagParserTest {

    private final TagParser tagParser = new TagParser();

    @Test
    @DisplayName("タグ抽出_本文に複数タグ_出現順に重複なく抽出する")
    void タグ抽出_本文に複数タグ_出現順に重複なく抽出する() {
        assertThat(tagParser.extractTags("今日は #Java と #SpringBoot を確認。#Java は復習。"))
                .containsExactly("Java", "SpringBoot");
    }
}
