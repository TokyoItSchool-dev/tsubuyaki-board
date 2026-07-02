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
        if (!StringUtils.hasText(keyword)) {
            return latest();
        }
        return repository.findTop50ByBodyContainingOrderByCreatedAtDesc(keyword.trim());
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
        return repository.findById(id);
    }

    public long likeCount(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    @Transactional
    public void toggleLike(Long postId, String clientHash) {
        likeRepository.findByPostIdAndClientHash(postId, clientHash)
                .ifPresentOrElse(likeRepository::delete,
                        () -> likeRepository.save(new PostLike(postId, clientHash, Instant.now())));
    }
}
