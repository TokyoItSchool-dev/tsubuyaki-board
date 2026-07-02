package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class PostService {

    private static final Pattern TAG_PATTERN = Pattern.compile(
            "(?<![\\p{L}\\p{N}_-])#([\\p{L}\\p{N}_][\\p{L}\\p{N}_-]{0,29})");

    private final PostRepository repository;
    private final PostLikeRepository likeRepository;
    private final TagRepository tagRepository;

    public PostService(
            PostRepository repository,
            PostLikeRepository likeRepository,
            TagRepository tagRepository) {
        this.repository = repository;
        this.likeRepository = likeRepository;
        this.tagRepository = tagRepository;
    }

    public List<Post> latest() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public List<Post> search(String keyword) {
        String trimmedKeyword = nullToEmpty(keyword).trim();
        if (trimmedKeyword.isEmpty()) {
            return latest();
        }
        return repository.findTop50ByBodyContainingOrderByCreatedAtDesc(trimmedKeyword);
    }

    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    public List<Post> findByTagName(String tagName) {
        return repository.findTop50DistinctByTagsNameOrderByCreatedAtDesc(normalizeTagName(tagName));
    }

    public long countLikes(Long id) {
        return likeRepository.countByPostId(id);
    }

    @Transactional
    public Optional<Boolean> toggleLike(Long id, String clientHash) {
        Optional<Post> post = repository.findById(id);
        if (post.isEmpty()) {
            return Optional.empty();
        }

        if (likeRepository.existsByPostIdAndClientHash(id, clientHash)) {
            likeRepository.deleteByPostIdAndClientHash(id, clientHash);
            return Optional.of(false);
        }

        likeRepository.save(new PostLike(post.get(), clientHash, Instant.now()));
        return Optional.of(true);
    }

    @Transactional
    public Post create(String author, String body) {
        return create(author, body, Post.DEFAULT_AVATAR_COLOR);
    }

    @Transactional
    public Post create(String author, String body, String avatarColor) {
        Post post = new Post(author.trim(), body.trim(), avatarColor, Instant.now());
        extractTagNames(body).stream()
                .map(this::findOrCreateTag)
                .forEach(post::addTag);
        return repository.save(post);
    }

    private Tag findOrCreateTag(String tagName) {
        return tagRepository.findByName(tagName)
                .orElseGet(() -> tagRepository.save(new Tag(tagName)));
    }

    private static Set<String> extractTagNames(String body) {
        Set<String> tagNames = new LinkedHashSet<>();
        Matcher matcher = TAG_PATTERN.matcher(nullToEmpty(body));
        while (matcher.find()) {
            tagNames.add(normalizeTagName(matcher.group(1)));
        }
        return tagNames;
    }

    private static String normalizeTagName(String tagName) {
        return nullToEmpty(tagName).toLowerCase(Locale.ROOT);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
