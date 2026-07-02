package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;
    private final PostLikeRepository likeRepository;

    public PostService(PostRepository repository, PostLikeRepository likeRepository) {
        this.repository = repository;
        this.likeRepository = likeRepository;
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
        return repository.save(post);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
