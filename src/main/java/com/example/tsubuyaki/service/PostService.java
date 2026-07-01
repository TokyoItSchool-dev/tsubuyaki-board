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

    public List<Post> search(String query) {
        if (!hasSearchQuery(query)) {
            return latest();
        }
        return repository.findTop50ByBodyContainingOrderByCreatedAtDesc(query);
    }

    public boolean hasSearchQuery(String query) {
        return query != null && !query.isBlank();
    }

    @Transactional
    public Post create(String author, String body) {
        return create(author, body, null);
    }

    @Transactional
    public Post create(String author, String body, String avatarColor) {
        return repository.save(new Post(author, body, Instant.now(), avatarColor));
    }

    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public boolean toggleLike(Long postId, String clientHash) {
        Optional<Post> post = repository.findById(postId);
        if (post.isEmpty()) {
            return false;
        }

        Optional<PostLike> like = likeRepository.findByPostIdAndClientHash(postId, clientHash);
        if (like.isPresent()) {
            likeRepository.delete(like.get());
        } else {
            likeRepository.save(new PostLike(post.get(), clientHash));
        }
        return true;
    }

    public long countLikes(Long postId) {
        return likeRepository.countByPostId(postId);
    }
}
