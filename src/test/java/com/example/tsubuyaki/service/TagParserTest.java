package com.example.tsubuyaki.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TagParserTest {

    private final TagParser tagParser = new TagParser();

    @Test
    @DisplayName("タグ解析_本文中のハッシュタグを出現順で重複なく抽出する")
    void extractNames_returnsUniqueTagNamesInOrder() {
        List<String> names = tagParser.extractNames("今日は #java を学びます。#spring と #java の復習です");

        assertThat(names).containsExactly("java", "spring");
    }

    @Test
    @DisplayName("タグ解析_コメント用の本文でも解析器自体はタグを抽出できる")
    void extractNames_whenJapaneseTag_returnsNameWithoutHash() {
        List<String> names = tagParser.extractNames("研修メモ #設計 #TDD");

        assertThat(names).containsExactly("設計", "TDD");
    }
}
