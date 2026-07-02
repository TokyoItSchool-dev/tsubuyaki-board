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

    public List<Post> latestWithLikes(String clientHash) {
        List<Post> posts = latest();
        posts.forEach(post -> applyLikeState(post, clientHash));
        return posts;
    }

    public List<Post> searchWithLikes(String keyword, String clientHash) {
        List<Post> posts = repository.findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc(keyword);
        posts.forEach(post -> applyLikeState(post, clientHash));
        return posts;
    }

    public long countSearchResults(String keyword) {
        return repository.countByBodyContainingAndDeletedAtIsNull(keyword);
    }

    public Optional<Post> findById(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id);
    }

    public Optional<Post> findByIdWithLike(Long id, String clientHash) {
        return findById(id).map(post -> {
            applyLikeState(post, clientHash);
            return post;
        });
    }

    @Transactional
    public void create(String author, String body) {
        create(author, body, "#2563EB");
    }

    @Transactional
    public void create(String author, String body, String authorIconColor) {
        repository.save(new Post(author, body, authorIconColor, Instant.now()));
    }

    @Transactional
    public Optional<Post> delete(Long id) {
        Optional<Post> post = repository.findByIdAndDeletedAtIsNull(id);
        post.ifPresent(value -> value.delete(Instant.now()));
        return post;
    }

    @Transactional
    public Optional<Post> toggleLike(Long postId, String clientHash) {
        Optional<Post> post = repository.findByIdAndDeletedAtIsNull(postId);
        if (post.isEmpty()) {
            return Optional.empty();
        }

        Optional<PostLike> existingLike = likeRepository.findByPostIdAndClientHash(postId, clientHash);
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.orElseThrow());
        } else {
            likeRepository.save(new PostLike(post.orElseThrow(), clientHash, Instant.now()));
        }
        return post;
    }

    private void applyLikeState(Post post, String clientHash) {
        Long postId = post.getId();
        post.applyLikeState(
                likeRepository.countByPostId(postId),
                likeRepository.existsByPostIdAndClientHash(postId, clientHash));
    }
}
