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
        return repository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    public List<Post> searchByBody(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return latest();
        }
        return repository.findTop50ByDeletedAtIsNullAndBodyContainingIgnoreCaseOrderByCreatedAtDesc(keyword.trim());
    }

    @Transactional
    public Post create(String author, String body) {
        return create(author, body, Post.DEFAULT_AVATAR_COLOR);
    }

    @Transactional
    public Post create(String author, String body, String avatarColor) {
        return repository.save(new Post(author, body, avatarColor, Instant.now()));
    }

    public Optional<Post> findById(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id);
    }

    @Transactional
    public boolean delete(Long id) {
        Optional<Post> post = repository.findByIdAndDeletedAtIsNull(id);
        if (post.isEmpty()) {
            return false;
        }
        post.get().delete(Instant.now());
        return true;
    }

    @Transactional
    public void toggleLike(Long postId, String clientHash) {
        Optional<PostLike> existing = likeRepository.findByPostIdAndClientHash(postId, clientHash);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            return;
        }
        Post post = repository.findByIdAndDeletedAtIsNull(postId).orElseThrow();
        likeRepository.save(new PostLike(post, clientHash));
    }

    public long countLikes(Long postId) {
        return likeRepository.countByPostId(postId);
    }
}
