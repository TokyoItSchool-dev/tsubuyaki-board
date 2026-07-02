package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class PostService {

    private static final String DEFAULT_AVATAR_COLOR = "gray";
    private static final Set<String> AVATAR_COLORS = Set.of("red", "blue", "green", "yellow", "gray");
    private static final Pattern TAG_PATTERN = Pattern.compile("#([A-Za-z0-9_]+)");

    private final PostRepository repository;
    private final PostLikeRepository postLikeRepository;
    private final TagRepository tagRepository;

    public PostService(PostRepository repository, PostLikeRepository postLikeRepository, TagRepository tagRepository) {
        this.repository = repository;
        this.postLikeRepository = postLikeRepository;
        this.tagRepository = tagRepository;
    }

    public List<Post> latest() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public List<Post> search(String q) {
        if (q == null || q.trim().isEmpty()) {
            return latest();
        }
        return repository.findTop50ByBodyContainingOrderByCreatedAtDesc(q.trim());
    }

    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    public List<Post> findByTag(String name) {
        String normalized = normalizeTagName(name);
        if (normalized.isEmpty()) {
            return List.of();
        }
        return repository.findTop50ByTagsNameOrderByCreatedAtDesc(normalized);
    }

    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    @Transactional
    public void toggleLike(Long postId, String clientHash) {
        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndClientHash(postId, clientHash);
        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            return;
        }

        Post post = repository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found: " + postId));
        postLikeRepository.save(new PostLike(post, clientHash, LocalDateTime.now()));
    }

    @Transactional
    public Post create(String author, String body) {
        return create(author, body, DEFAULT_AVATAR_COLOR);
    }

    @Transactional
    public Post create(String author, String body, String avatarColor) {
        String trimmedBody = body.trim();
        Post post = new Post(author.trim(), trimmedBody, LocalDateTime.now(), normalizeAvatarColor(avatarColor));
        extractTagNames(trimmedBody).forEach(name -> post.addTag(findOrCreateTag(name)));
        return repository.save(post);
    }

    private Tag findOrCreateTag(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> tagRepository.save(new Tag(name)));
    }

    private static Set<String> extractTagNames(String body) {
        Set<String> names = new LinkedHashSet<>();
        Matcher matcher = TAG_PATTERN.matcher(body);
        while (matcher.find()) {
            names.add(normalizeTagName(matcher.group(1)));
        }
        return names;
    }

    private static String normalizeTagName(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeAvatarColor(String avatarColor) {
        if (avatarColor == null || avatarColor.trim().isEmpty()) {
            return DEFAULT_AVATAR_COLOR;
        }
        String normalized = avatarColor.trim();
        return AVATAR_COLORS.contains(normalized) ? normalized : DEFAULT_AVATAR_COLOR;
    }
}
