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

    public TagService(TagRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public void createForPost(long postId, String body) {
        Instant createdAt = Instant.now(clock);
        for (String tagName : parseTags(body)) {
            repository.save(new Tag(postId, tagName, createdAt));
        }
    }

    public List<Tag> tagsByPostId(long postId) {
        return repository.findByPostId(postId);
    }

    public Map<Long, List<Tag>> tagsByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<Tag>> tagsByPostId = new LinkedHashMap<>();
        for (Tag tag : repository.findByPostIdInOrderByCreatedAtAscIdAsc(postIds)) {
            tagsByPostId.computeIfAbsent(tag.getPostId(), ignored -> new ArrayList<>()).add(tag);
        }
        return tagsByPostId;
    }

    public List<Long> postIdsByPathName(String name) {
        if (name == null || name.isBlank()) {
            return List.of();
        }
        String normalized = name.startsWith("#") ? name : "#" + name;
        return repository.findPostIdsByTagNameOrderByPostCreatedAtDesc(normalized);
    }

    public List<TagTextSegment> bodySegments(String body) {
        if (body == null || body.isEmpty()) {
            return List.of();
        }
        List<TagTextSegment> segments = new ArrayList<>();
        Matcher matcher = TAG_PATTERN.matcher(body);
        int current = 0;
        while (matcher.find()) {
            if (matcher.start() > current) {
                segments.add(new TagTextSegment(body.substring(current, matcher.start()), false));
            }
            segments.add(new TagTextSegment(matcher.group(), true));
            current = matcher.end();
        }
        if (current < body.length()) {
            segments.add(new TagTextSegment(body.substring(current), false));
        }
        if (segments.isEmpty()) {
            return List.of(new TagTextSegment(body, false));
        }
        return List.copyOf(segments);
    }

    private List<String> parseTags(String body) {
        if (body == null || body.isBlank()) {
            return List.of();
        }
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        Matcher matcher = TAG_PATTERN.matcher(body);
        while (matcher.find()) {
            String tag = matcher.group();
            if (tag.length() > 1) {
                tags.add(tag.length() <= MAX_TAG_LENGTH ? tag : tag.substring(0, MAX_TAG_LENGTH));
            }
        }
        return List.copyOf(tags);
    }
}
