package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostComment;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostCommentRepository;
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
    private final PostCommentRepository postCommentRepository;

    public PostService(PostRepository repository, PostLikeRepository postLikeRepository,
            PostCommentRepository postCommentRepository) {
        this.repository = repository;
        this.postLikeRepository = postLikeRepository;
        this.postCommentRepository = postCommentRepository;
    }

    public List<Post> latest() {
        return findLatestPosts();
    }

    public List<Post> findLatestPosts() {
        return repository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    public List<Post> searchPosts(String keyword) {
        return repository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc(keyword);
    }

    public long countActivePosts() {
        return repository.countByDeletedAtIsNull();
    }

    public Optional<Post> findPost(Long id) {
        return repository.findById(id);
    }

    public List<PostComment> findComments(Long postId) {
        return postCommentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }

    public long countComments(Long postId) {
        return postCommentRepository.countByPostId(postId);
    }

    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    public boolean isLiked(Long postId, String clientHash) {
        return postLikeRepository.existsByPostIdAndClientHash(postId, clientHash);
    }

    @Transactional
    public Post createPost(String author, String body) {
        return createPost(author, body, Post.DEFAULT_AVATAR_COLOR);
    }

    @Transactional
    public Post createPost(String author, String body, String avatarColor) {
        return repository.save(new Post(author, body, avatarColor, Instant.now()));
    }

    @Transactional
    public PostComment createComment(Long postId, String author, String body, String avatarColor) {
        return postCommentRepository.save(new PostComment(postId, author, body, avatarColor, Instant.now()));
    }

    @Transactional
    public void deleteComment(Long commentId) {
        postCommentRepository.deleteById(commentId);
    }

    @Transactional
    public void deletePost(Long id) {
        repository.findById(id).ifPresent(post -> post.markDeleted(Instant.now()));
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
