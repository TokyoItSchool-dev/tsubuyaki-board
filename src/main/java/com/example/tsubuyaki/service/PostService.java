package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class PostService {

    private static final String DEFAULT_AVATAR_COLOR = "gray";
    private static final Set<String> AVATAR_COLORS = Set.of("red", "blue", "green", "yellow", "gray");

    private final PostRepository repository;
    private final PostLikeRepository postLikeRepository;

    public PostService(PostRepository repository, PostLikeRepository postLikeRepository) {
        this.repository = repository;
        this.postLikeRepository = postLikeRepository;
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
        Post post = new Post(author.trim(), body.trim(), LocalDateTime.now(), normalizeAvatarColor(avatarColor));
        return repository.save(post);
    }

    private static String normalizeAvatarColor(String avatarColor) {
        if (avatarColor == null || avatarColor.trim().isEmpty()) {
            return DEFAULT_AVATAR_COLOR;
        }
        String normalized = avatarColor.trim();
        return AVATAR_COLORS.contains(normalized) ? normalized : DEFAULT_AVATAR_COLOR;
    }
}
