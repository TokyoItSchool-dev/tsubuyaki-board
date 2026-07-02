package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;
    private final PostLikeRepository postLikeRepository;
    private final Clock clock;

    @Autowired
    public PostService(PostRepository repository, PostLikeRepository postLikeRepository) {
        this(repository, postLikeRepository, Clock.systemUTC());
    }

    PostService(PostRepository repository, PostLikeRepository postLikeRepository, Clock clock) {
        this.repository = repository;
        this.postLikeRepository = postLikeRepository;
        this.clock = clock;
    }

    public List<Post> latest() {
        return findLatest50Posts();
    }

    public List<Post> findLatest50Posts() {
        List<Post> posts = repository.findTop50ByOrderByCreatedAtDesc();
        if (posts == null) {
            return List.of();
        }
        return posts;
    }

    public List<Post> searchPosts(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isEmpty()) {
            return findLatest50Posts();
        }

        List<Post> posts = repository.findTop50ByBodyContainingOrderByCreatedAtDesc(normalizedKeyword);
        if (posts == null) {
            return List.of();
        }
        return posts;
    }

    public Optional<Post> findDetailPost(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id);
    }

    public boolean hasLiked(Long postId, String clientHash) {
        return postLikeRepository.existsByPostIdAndClientHash(postId, clientHash);
    }

    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    @Transactional
    public void toggleLike(Long postId, String clientHash) {
        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndClientHash(postId, clientHash);
        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.orElseThrow());
            return;
        }

        Post post = repository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new NoSuchElementException("投稿が見つかりません: " + postId));
        postLikeRepository.save(new PostLike(post, clientHash, Instant.now(clock)));
    }

    @Transactional
    public void createPost(String author, String body) {
        createPost(author, "red", body);
    }

    @Transactional
    public void createPost(String author, String avatarColor, String body) {
        repository.save(new Post(author, avatarColor, body, Instant.now(clock)));
    }

    private static String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim();
    }
}
