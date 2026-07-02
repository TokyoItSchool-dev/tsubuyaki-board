package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TagService {

    private static final Pattern TAG_PATTERN = Pattern.compile("#([\\p{L}\\p{N}_-]+)");

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional
    public void saveTagsFor(Post post) {
        List<Tag> tags = extractTags(post);
        if (!tags.isEmpty()) {
            tagRepository.saveAll(tags);
        }
    }

    List<Tag> extractTags(Post post) {
        Set<String> tagNames = new LinkedHashSet<>();
        Matcher matcher = TAG_PATTERN.matcher(Objects.toString(post.getBody(), ""));
        while (matcher.find()) {
            tagNames.add(matcher.group(1));
        }

        List<Tag> tags = new ArrayList<>();
        for (String tagName : tagNames) {
            tags.add(new Tag(post, tagName));
        }
        return tags;
    }
}
