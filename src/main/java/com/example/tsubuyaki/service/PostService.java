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
        return findLatestPosts();
    }

    public List<Post> findLatestPosts() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public List<Post> searchPosts(String keyword) {
        return repository.findTop50ByBodyContainingOrderByCreatedAtDesc(keyword);
    }

    public Optional<Post> findPost(Long id) {
        return repository.findById(id);
    }

    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    @Transactional
    public Post createPost(String author, String body) {
        return repository.save(new Post(author, body, Instant.now()));
    }

    @Transactional
    public void toggleLike(Long postId, String clientHash) {
        if (postLikeRepository.existsByPostIdAndClientHash(postId, clientHash)) {
            postLikeRepository.deleteByPostIdAndClientHash(postId, clientHash);
            return;
        }

        postLikeRepository.save(new PostLike(postId, clientHash, Instant.now()));
    }
}
