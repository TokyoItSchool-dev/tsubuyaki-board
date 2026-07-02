package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;

    private final PostLikeRepository postLikeRepository;

    public PostService(PostRepository repository, PostLikeRepository postLikeRepository) {
        this.repository = repository;
        this.postLikeRepository = postLikeRepository;
    }

    public List<Post> latest() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public List<Post> search(String query) {
        if (query == null || query.isBlank()) {
            return latest();
        }
        return repository.findTop50ByBodyContainingIgnoreCaseOrderByCreatedAtDesc(query.trim());
    }

    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    public boolean likedBy(Long postId, String clientHash) {
        return postLikeRepository.existsByPostIdAndClientHash(postId, clientHash);
    }

    @Transactional
    public void create(String author, String body) {
        create(author, body, null);
    }

    @Transactional
    public void create(String author, String body, String avatarColor) {
        repository.save(new Post(author, body, normalizeAvatarColor(avatarColor), Instant.now()));
    }

    @Transactional
    public void toggleLike(Long postId, String clientHash) {
        if (!repository.existsById(postId)) {
            throw new NoSuchElementException("post not found: " + postId);
        }
        if (postLikeRepository.existsByPostIdAndClientHash(postId, clientHash)) {
            postLikeRepository.deleteByPostIdAndClientHash(postId, clientHash);
            return;
        }
        postLikeRepository.save(new PostLike(postId, clientHash));
    }

    private String normalizeAvatarColor(String avatarColor) {
        if (avatarColor == null || avatarColor.isBlank()) {
            return "gray";
        }
        return avatarColor;
    }
}
