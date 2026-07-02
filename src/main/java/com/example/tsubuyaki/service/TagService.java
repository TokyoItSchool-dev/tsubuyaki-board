package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.HashtagText;
import com.example.tsubuyaki.repository.TagEntity;
import com.example.tsubuyaki.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<String> extractTagNames(String body) {
        return HashtagText.extractTagNames(body);
    }

    @Transactional
    public List<TagEntity> resolveTags(String body) {
        return extractTagNames(body).stream()
                .map(this::findOrCreate)
                .toList();
    }

    private TagEntity findOrCreate(String name) {
        String normalized = Objects.requireNonNull(name, "name must not be null");
        return tagRepository.findByName(normalized)
                .orElseGet(() -> tagRepository.save(new TagEntity(normalized)));
    }
}
