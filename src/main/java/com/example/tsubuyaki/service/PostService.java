package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostBackgroundColor;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.web.dto.PostView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private static final int NOT_DELETED = 0;

    private final PostRepository repository;
    private final PostLikeRepository likeRepository;

    public PostService(PostRepository repository, PostLikeRepository likeRepository) {
        this.repository = repository;
        this.likeRepository = likeRepository;
    }

    public List<PostView> latest() {
        return toViews(repository.findTop50ByDeletedAtOrderByCreatedAtDesc(NOT_DELETED));
    }

    public List<PostView> searchByBody(String keyword) {
        return toViews(repository.findTop50ByBodyContainingAndDeletedAtOrderByCreatedAtDesc(keyword, NOT_DELETED));
    }

    @Transactional
    public void create(String author, String body) {
        create(author, body, PostBackgroundColor.DEFAULT);
    }

    @Transactional
    public void create(String author, String body, String backgroundColor) {
        create(author, body, backgroundColor, null);
    }

    @Transactional
    public void create(String author, String body, String backgroundColor, String clientHash) {
        repository.saveAndFlush(new Post(author, body, LocalDateTime.now(), backgroundColor, clientHash));
    }

    public Optional<PostView> findById(Long id) {
        return findById(id, null);
    }

    public Optional<PostView> findById(Long id, String clientHash) {
        return repository.findByIdAndDeletedAt(id, NOT_DELETED)
                .map(post -> toView(post, clientHash));
    }

    public boolean existsDeletedById(Long id) {
        return repository.existsByIdAndDeletedAt(id, 1);
    }

    @Transactional
    public boolean toggleLike(Long postId, String clientHash) {
        Optional<Post> post = repository.findByIdAndDeletedAt(postId, NOT_DELETED);
        if (post.isEmpty()) {
            return false;
        }

        Optional<PostLike> existingLike = likeRepository.findByPostIdAndClientHash(postId, clientHash);
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            likeRepository.flush();
        } else {
            likeRepository.saveAndFlush(new PostLike(post.get(), clientHash, LocalDateTime.now()));
        }
        return true;
    }

    @Transactional
    public boolean delete(Long postId, String clientHash) {
        Optional<Post> post = repository.findByIdAndDeletedAt(postId, NOT_DELETED);
        if (post.isEmpty() || !post.get().canDelete(clientHash)) {
            return false;
        }
        post.get().markDeleted();
        repository.flush();
        return true;
    }

    private PostView toView(Post post) {
        return toView(post, null);
    }

    private PostView toView(Post post, String clientHash) {
        return new PostView(
                post.getId(),
                post.getAuthor(),
                post.getBody(),
                post.getCreatedAt(),
                likeRepository.countByPostId(post.getId()),
                post.getBackgroundColor(),
                post.canDelete(clientHash));
    }

    private List<PostView> toViews(List<Post> posts) {
        return posts.stream()
                .map(this::toView)
                .toList();
    }
}
