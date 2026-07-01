package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class PostService {

    private static final Pattern AVATAR_COLOR_PATTERN = Pattern.compile("^#[0-9a-fA-F]{6}$");

    private final PostRepository repository;
    private final PostLikeRepository postLikeRepository;

    public PostService(PostRepository repository, PostLikeRepository postLikeRepository) {
        this.repository = repository;
        this.postLikeRepository = postLikeRepository;
    }

    public List<Post> latest() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public List<Post> list(String q) {
        if (q == null || q.trim().isEmpty()) {
            return latest();
        }
        return repository.findTop50ByBodyContainingOrderByCreatedAtDesc(q);
    }

    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Post create(String author, String body) {
        return create(author, body, Post.DEFAULT_AVATAR_COLOR);
    }

    @Transactional
    public Post create(String author, String body, String avatarColor) {
        return repository.save(new Post(author, body, normalizeAvatarColor(avatarColor), Instant.now()));
    }

    @Transactional
    public long toggleLike(Long postId, String clientHash) {
        Post post = repository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        postLikeRepository.findByPostIdAndClientHash(postId, clientHash)
                .ifPresentOrElse(postLikeRepository::delete,
                        () -> postLikeRepository.save(new PostLike(post, clientHash)));

        return postLikeRepository.countByPostId(postId);
    }

    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    private String normalizeAvatarColor(String avatarColor) {
        if (avatarColor == null || avatarColor.trim().isEmpty()) {
            return Post.DEFAULT_AVATAR_COLOR;
        }
        String trimmed = avatarColor.trim();
        if (!AVATAR_COLOR_PATTERN.matcher(trimmed).matches()) {
            return Post.DEFAULT_AVATAR_COLOR;
        }
        return trimmed;
    }
}
