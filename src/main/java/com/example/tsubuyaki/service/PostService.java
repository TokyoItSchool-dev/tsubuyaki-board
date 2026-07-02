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
        return repository.findTop50ByBodyContainingOrderByCreatedAtDesc(query.trim());
    }

    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    public boolean isLiked(Long postId, String clientHash) {
        return postLikeRepository.existsByPostIdAndClientHash(postId, clientHash);
    }

    @Transactional
    public void create(String author, String body) {
        repository.save(new Post(author, body, Instant.now()));
    }

    @Transactional
    public void toggleLike(Long postId, String clientHash) {
        Optional<PostLike> existing = postLikeRepository.findByPostIdAndClientHash(postId, clientHash);
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            return;
        }

        Post post = repository.findById(postId).orElseThrow();
        postLikeRepository.save(new PostLike(post, clientHash, Instant.now()));
    }
}
