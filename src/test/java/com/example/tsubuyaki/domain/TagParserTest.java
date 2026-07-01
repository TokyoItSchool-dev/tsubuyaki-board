package com.example.tsubuyaki.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TagParserTest {

    @Test
    @DisplayName("タグ抽出_半角全角スペースで区切られたとき_別タグとして返す")
    void タグ抽出_半角全角スペースで区切られたとき_別タグとして返す() {
        List<String> tags = TagParser.extractNames("#java #spring　#研修");

        assertThat(tags).containsExactly("java", "spring", "研修");
    }

    @Test
    @DisplayName("タグ抽出_改行で区切られたとき_別タグとして返す")
    void タグ抽出_改行で区切られたとき_別タグとして返す() {
        List<String> tags = TagParser.extractNames("#java\n#spring\r\n#研修");

        assertThat(tags).containsExactly("java", "spring", "研修");
    }

    @Test
    @DisplayName("タグ抽出_全角シャープを含むとき_半角シャープだけ認識する")
    void タグ抽出_全角シャープを含むとき_半角シャープだけ認識する() {
        List<String> tags = TagParser.extractNames("＃ng #ok");

        assertThat(tags).containsExactly("ok");
    }

    @Test
    @DisplayName("タグ抽出_同じタグが複数あるとき_重複を除いて返す")
    void タグ抽出_同じタグが複数あるとき_重複を除いて返す() {
        List<String> tags = TagParser.extractNames("#java #java #spring");

        assertThat(tags).containsExactly("java", "spring");
    }

    @Test
    @DisplayName("タグ抽出_本文がnullまたは空タグのとき_空配列を返す")
    void タグ抽出_本文がnullまたは空タグのとき_空配列を返す() {
        assertThat(TagParser.extractNames(null)).isEmpty();
        assertThat(TagParser.extractNames("# #　#")).isEmpty();
    }
}
