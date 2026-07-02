package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class TagService {

    private static final Pattern TAG_PATTERN = Pattern.compile("#\\S+");
    private static final int MAX_TAG_LENGTH = 30;

    private final TagRepository repository;
    private final Clock clock;

    /**
     * タグサービスを生成する。
     *
     * @param repository タグリポジトリ
     * @param clock 現在日時取得用 Clock
     */
    public TagService(TagRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /**
     * 投稿本文からタグを抽出して保存する。
     *
     * @param postId 投稿 ID
     * @param body 本文
     */
    @Transactional
    public void createForPost(long postId, String body) {
        Instant createdAt = Instant.now(clock);
        // 同一本文中の重複タグは parseTags 側で除外済み。
        for (String tagName : parseTags(body)) {
            repository.save(new Tag(postId, tagName, createdAt));
        }
    }

    /**
     * 投稿 ID に紐づくタグ一覧を取得する。
     *
     * @param postId 投稿 ID
     * @return タグ一覧
     */
    public List<Tag> tagsByPostId(long postId) {
        return repository.findByPostId(postId);
    }

    /**
     * 複数投稿 ID に紐づくタグを投稿 ID ごとにまとめて取得する。
     *
     * @param postIds 投稿 ID 一覧
     * @return 投稿 ID をキーにしたタグ一覧
     */
    public Map<Long, List<Tag>> tagsByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<Tag>> tagsByPostId = new LinkedHashMap<>();
        // テンプレート側で投稿 ID からタグ一覧を引けるよう Map に詰め替える。
        for (Tag tag : repository.findByPostIdInOrderByCreatedAtAscIdAsc(postIds)) {
            tagsByPostId.computeIfAbsent(tag.getPostId(), ignored -> new ArrayList<>()).add(tag);
        }
        return tagsByPostId;
    }

    /**
     * パスで受け取ったタグ名から投稿 ID 一覧を取得する。
     *
     * @param name URL パスのタグ名
     * @return 該当タグを持つ投稿 ID 一覧
     */
    public List<Long> postIdsByPathName(String name) {
        if (name == null || name.isBlank()) {
            return List.of();
        }
        // URL では # を省略できるため、検索用には # 付きへ正規化する。
        String normalized = name.startsWith("#") ? name : "#" + name;
        return repository.findPostIdsByTagNameOrderByPostCreatedAtDesc(normalized);
    }

    /**
     * 本文を通常文字列とタグ文字列の表示セグメントに分割する。
     *
     * @param body 本文
     * @return 表示セグメント一覧
     */
    public List<TagTextSegment> bodySegments(String body) {
        if (body == null || body.isEmpty()) {
            return List.of();
        }
        List<TagTextSegment> segments = new ArrayList<>();
        Matcher matcher = TAG_PATTERN.matcher(body);
        int current = 0;
        while (matcher.find()) {
            // タグの手前に通常文字列があれば、通常セグメントとして保持する。
            if (matcher.start() > current) {
                segments.add(new TagTextSegment(body.substring(current, matcher.start()), false));
            }
            segments.add(new TagTextSegment(matcher.group(), true));
            current = matcher.end();
        }
        // 最後のタグ以降に残った通常文字列を保持する。
        if (current < body.length()) {
            segments.add(new TagTextSegment(body.substring(current), false));
        }
        if (segments.isEmpty()) {
            return List.of(new TagTextSegment(body, false));
        }
        return List.copyOf(segments);
    }

    /**
     * 本文から保存対象のタグ名を抽出する。
     *
     * @param body 本文
     * @return 重複を除いたタグ名一覧
     */
    private List<String> parseTags(String body) {
        if (body == null || body.isBlank()) {
            return List.of();
        }
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        Matcher matcher = TAG_PATTERN.matcher(body);
        while (matcher.find()) {
            String tag = matcher.group();
            // # だけの値は保存せず、DB 定義の長さに合わせて最大 30 文字に丸める。
            if (tag.length() > 1) {
                tags.add(tag.length() <= MAX_TAG_LENGTH ? tag : tag.substring(0, MAX_TAG_LENGTH));
            }
        }
        return List.copyOf(tags);
    }
}
