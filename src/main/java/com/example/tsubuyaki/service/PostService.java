package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public PostService(PostRepository postRepository, PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
    }

    public List<Post> latest() {
        return postRepository.findTop50ByOrderByCreatedAtDesc();
    }

    public List<Post> search(String query) {
        if (!StringUtils.hasText(query)) {
            return latest();
        }

        return postRepository.findByBodyContainingOrderByCreatedAtDesc(query);
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    @Transactional
    public void create(String author, String body) {
        postRepository.save(new Post(author, body, Instant.now()));
    }

    @Transactional
    public Optional<Post> update(Long id, String author, String body) {
        return postRepository.findById(id)
                .map(post -> {
                    post.update(author, body);
                    postRepository.save(post);
                    return post;
                });
    }

    @Transactional
    public Optional<Boolean> toggleLike(Long postId, String clientHash) {
        return postRepository.findById(postId)
                .map(post -> toggleLike(postId, post, clientHash));
    }

    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    public boolean likedBy(Long postId, String clientHash) {
        return postLikeRepository.existsByPostIdAndClientHash(postId, clientHash);
    }

    private boolean toggleLike(Long postId, Post post, String clientHash) {
        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndClientHash(postId, clientHash);
        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            return false;
        }

        postLikeRepository.save(new PostLike(post, clientHash, Instant.now()));
        return true;
    }
}
