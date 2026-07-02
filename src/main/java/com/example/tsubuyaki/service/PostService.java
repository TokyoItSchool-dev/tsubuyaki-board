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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<Post> searchByBody(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (!StringUtils.hasText(normalizedKeyword)) {
            return latest();
        }
        return repository.findTop50ByBodyContainingOrderByCreatedAtDesc(normalizedKeyword);
    }

    public String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return "";
        }
        return keyword.strip();
    }

    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    public long countLikes(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    public Map<Long, Long> countLikesByPostId(List<Post> posts) {
        return posts.stream()
                .filter(post -> post.getId() != null)
                .collect(Collectors.toMap(Post::getId, post -> likeRepository.countByPostId(post.getId())));
    }

    @Transactional
    public Post create(String author, String body) {
        return create(author, null, body);
    }

    @Transactional
    public Post create(String author, String avatarColor, String body) {
        return repository.save(new Post(author, normalizeAvatarColor(avatarColor), body, Instant.now()));
    }

    private String normalizeAvatarColor(String avatarColor) {
        if (!StringUtils.hasText(avatarColor)) {
            return null;
        }
        return avatarColor;
    }

    @Transactional
    public Optional<Boolean> toggleLike(Long postId, String clientHash) {
        Optional<Post> post = repository.findById(postId);
        if (post.isEmpty()) {
            return Optional.empty();
        }

        Optional<PostLike> liked = likeRepository.findByPostIdAndClientHash(postId, clientHash);
        if (liked.isPresent()) {
            likeRepository.delete(liked.get());
            return Optional.of(false);
        }

        likeRepository.save(new PostLike(post.get(), clientHash, Instant.now()));
        return Optional.of(true);
    }
}
