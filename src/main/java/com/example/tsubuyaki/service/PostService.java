package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.domain.User;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.UserRepository;
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
    private final UserRepository userRepository;

    public PostService(PostRepository repository, PostLikeRepository postLikeRepository,
            UserRepository userRepository) {
        this.repository = repository;
        this.postLikeRepository = postLikeRepository;
        this.userRepository = userRepository;
    }

    public List<Post> findLatest50() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public List<Post> searchByBody(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findLatest50();
        }
        return repository.findTop50ByBodyContainingOrderByCreatedAtDesc(keyword);
    }

    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    @Transactional
    public Post create(String author, String body) {
        return create(author, body, "");
    }

    @Transactional
    public Post create(String author, String body, String avatarColor) {
        User user = userRepository.findByName(author)
                .map(existing -> {
                    existing.updateAvatarColor(avatarColor);
                    return existing;
                })
                .orElseGet(() -> userRepository.save(new User(author, avatarColor)));
        return repository.save(new Post(user, body, Instant.now()));
    }

    @Transactional
    public void toggleLike(Long postId, String clientHash) {
        if (postLikeRepository.existsByPostIdAndClientHash(postId, clientHash)) {
            postLikeRepository.deleteByPostIdAndClientHash(postId, clientHash);
            return;
        }

        postLikeRepository.save(new PostLike(repository.getReferenceById(postId), clientHash, Instant.now()));
    }
}
