package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Comment;
import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.CommentRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class PostService {

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#([\\p{L}\\p{N}_-]{1,50})");

    private final PostRepository repository;

    private final TagRepository tagRepository;

    private final CommentRepository commentRepository;

    private final Map<Long, Set<String>> likedClientHashesByPostId = new ConcurrentHashMap<>();

    public PostService(PostRepository repository, TagRepository tagRepository, CommentRepository commentRepository) {
        this.repository = repository;
        this.tagRepository = tagRepository;
        this.commentRepository = commentRepository;
    }

    public List<Post> findLatest50() {
        return initializeTags(repository.findTop50ByOrderByCreatedAtDesc());
    }

    public List<Post> findOldest50() {
        return initializeTags(repository.findTop50ByDeletedAtIsNullOrderByCreatedAtAsc());
    }

    public List<Post> searchByBodyContaining(String keyword) {
        return initializeTags(repository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc(keyword));
    }

    public List<Post> searchByBodyContainingOldest(String keyword) {
        return initializeTags(repository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtAsc(keyword));
    }

    public Optional<Post> findById(Long id) {
        return initializeTags(repository.findByIdAndDeletedAtIsNull(id));
    }

    public List<Post> findByTagName(String name) {
        return initializeTags(tagRepository.findPostsByNameOrderByPostCreatedAtDesc(normalizeTagName(name)));
    }

    public List<Comment> findCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    public long countLikes(Long postId) {
        return likedClientHashesByPostId.getOrDefault(postId, Collections.emptySet()).size();
    }

    public void toggleLike(Long postId, String clientHash) {
        likedClientHashesByPostId.compute(postId, (id, clientHashes) -> {
            Set<String> updatedClientHashes = clientHashes;
            if (updatedClientHashes == null) {
                updatedClientHashes = ConcurrentHashMap.newKeySet();
            }
            if (!updatedClientHashes.add(clientHash)) {
                updatedClientHashes.remove(clientHash);
            }
            return updatedClientHashes.isEmpty() ? null : updatedClientHashes;
        });
    }

    @Transactional
    public void create(String author, String body) {
        create(author, body, null);
    }

    @Transactional
    public void create(String author, String body, String avatarColor) {
        create(author, body, avatarColor, "");
    }

    @Transactional
    public void create(String author, String body, String avatarColor, String tagNames) {
        Post post = new Post(author, body, LocalDateTime.now(), avatarColor);
        extractTagNames(body, tagNames).stream()
                .map(this::findOrCreateTag)
                .forEach(post::addTag);
        repository.save(post);
    }

    @Transactional
    public void delete(Long id) {
        repository.findByIdAndDeletedAtIsNull(id)
                .ifPresent(post -> post.markDeleted(LocalDateTime.now()));
    }

    @Transactional
    public void createComment(Long postId, String body) {
        if (body == null || body.isBlank()) {
            return;
        }
        repository.findByIdAndDeletedAtIsNull(postId)
                .ifPresent(post -> commentRepository.save(new Comment(post, body, LocalDateTime.now())));
    }

    private Tag findOrCreateTag(String name) {
        return tagRepository.findByName(name).orElseGet(() -> new Tag(name));
    }

    private static List<Post> initializeTags(List<Post> posts) {
        posts.forEach(PostService::initializeTags);
        return posts;
    }

    private static Optional<Post> initializeTags(Optional<Post> post) {
        post.ifPresent(PostService::initializeTags);
        return post;
    }

    private static void initializeTags(Post post) {
        if (post.hasTags()) {
            return;
        }
    }

    private static Set<String> extractTagNames(String body, String tagNames) {
        Set<String> extractedTagNames = new LinkedHashSet<>();
        addBodyTagNames(body, extractedTagNames);
        addEnteredTagNames(tagNames, extractedTagNames);
        return extractedTagNames;
    }

    private static void addBodyTagNames(String body, Set<String> tagNames) {
        if (body == null || body.isBlank()) {
            return;
        }
        Matcher matcher = HASHTAG_PATTERN.matcher(body);
        while (matcher.find()) {
            tagNames.add(normalizeTagName(matcher.group(1)));
        }
    }

    private static void addEnteredTagNames(String enteredTagNames, Set<String> tagNames) {
        if (enteredTagNames == null || enteredTagNames.isBlank()) {
            return;
        }
        Matcher matcher = HASHTAG_PATTERN.matcher(toHashtagText(enteredTagNames));
        while (matcher.find()) {
            tagNames.add(normalizeTagName(matcher.group(1)));
        }
    }

    private static String toHashtagText(String tagNames) {
        StringBuilder builder = new StringBuilder();
        for (String token : tagNames.split("[\\s,]+")) {
            if (!token.isBlank()) {
                builder.append(' ');
                if (!token.startsWith("#")) {
                    builder.append('#');
                }
                builder.append(token);
            }
        }
        return builder.toString();
    }

    private static String normalizeTagName(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }
}
