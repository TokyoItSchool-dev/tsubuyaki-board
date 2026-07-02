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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public Optional<PostView> findEditableById(Long id, String clientHash) {
        return findById(id, clientHash)
                .filter(PostView::isCanEdit);
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

    @Transactional
    public UpdatePostResult update(Long postId, String body, String backgroundColor, String clientHash) {
        Optional<Post> post = repository.findByIdAndDeletedAt(postId, NOT_DELETED);
        if (post.isEmpty()) {
            return UpdatePostResult.NOT_FOUND;
        }
        if (!post.get().canModify(clientHash)) {
            return UpdatePostResult.FORBIDDEN;
        }
        post.get().updateBodyAndBackgroundColor(body, backgroundColor, LocalDateTime.now());
        repository.flush();
        return UpdatePostResult.UPDATED;
    }

    private PostView toView(Post post, String clientHash) {
        return toView(post, clientHash, likeRepository.countByPostId(post.getId()));
    }

    private PostView toView(Post post, String clientHash, long likeCount) {
        boolean canModify = post.canModify(clientHash);
        return PostView.builder(post.getId(), post.getAuthor(), post.getBody(), post.getCreatedAt())
                .likeCount(likeCount)
                .backgroundColor(post.getBackgroundColor())
                .canDelete(canModify)
                .canEdit(canModify)
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private List<PostView> toViews(List<Post> posts) {
        Map<Long, Long> likeCounts = likeCountsByPostId(posts);
        return posts.stream()
                .map(post -> toView(post, null, likeCounts.getOrDefault(post.getId(), 0L)))
                .toList();
    }

    private Map<Long, Long> likeCountsByPostId(List<Post> posts) {
        if (posts.isEmpty()) {
            return Map.of();
        }
        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .toList();
        return likeRepository.countByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(
                        PostLikeRepository.PostLikeCount::getPostId,
                        PostLikeRepository.PostLikeCount::getLikeCount));
    }
}
