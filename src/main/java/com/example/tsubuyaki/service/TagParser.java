/*
 * 投稿本文からハッシュタグ表記を抽出する小さな解析コンポーネント。
 */
package com.example.tsubuyaki.service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TagParser {

    private static final Pattern TAG_PATTERN = Pattern.compile("#([\\p{L}\\p{N}_]+)");

    /**
     * 本文中の {@code #tag} 形式のタグ名を抽出する。
     *
     * <p>タグ名として文字、数字、アンダースコアを許可する。同じタグが本文中に
     * 複数回出てきても、1 投稿内では 1 件だけ保存できるように重複を取り除き、
     * 初出順を保って返す。</p>
     *
     * @param body 投稿本文
     * @return 本文から抽出したタグ名一覧
     */
    public List<String> extractTags(String body) {
        if (body == null || body.isBlank()) {
            return List.of();
        }

        LinkedHashSet<String> tags = new LinkedHashSet<>();
        Matcher matcher = TAG_PATTERN.matcher(body);
        while (matcher.find()) {
            tags.add(matcher.group(1));
        }
        return List.copyOf(tags);
    }
}
