package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.domain.PostReply;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.PostReplyRepository;
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
    private final PostReplyRepository replyRepository;

    public PostService(PostRepository repository, PostLikeRepository likeRepository, PostReplyRepository replyRepository) {
        this.repository = repository;
        this.likeRepository = likeRepository;
        this.replyRepository = replyRepository;
    }

    public List<Post> latest() {
        return repository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    public List<Post> search(String query) {
        if (!hasSearchQuery(query)) {
            return latest();
        }
        return repository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc(query);
    }

    public boolean hasSearchQuery(String query) {
        return query != null && !query.isBlank();
    }

    @Transactional
    public Post create(String author, String body) {
        return create(author, body, null);
    }

    @Transactional
    public Post create(String author, String body, String avatarColor) {
        return repository.save(new Post(author, body, Instant.now(), avatarColor));
    }

    public Optional<Post> findById(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id);
    }

    @Transactional
    public boolean toggleLike(Long postId, String clientHash) {
        Optional<Post> post = repository.findByIdAndDeletedAtIsNull(postId);
        if (post.isEmpty()) {
            return false;
        }

        Optional<PostLike> like = likeRepository.findByPostIdAndClientHash(postId, clientHash);
        if (like.isPresent()) {
            likeRepository.delete(like.get());
        } else {
            likeRepository.save(new PostLike(post.get(), clientHash));
        }
        return true;
    }

    @Transactional
    public boolean delete(Long postId) {
        Optional<Post> post = repository.findByIdAndDeletedAtIsNull(postId);
        if (post.isEmpty()) {
            return false;
        }
        post.get().markDeleted(Instant.now());
        return true;
    }

    public long countLikes(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    public List<PostReply> repliesForPost(Long postId) {
        return replyRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    @Transactional
    public boolean createReply(Long postId, String author, String body, String avatarColor) {
        Optional<Post> post = repository.findByIdAndDeletedAtIsNull(postId);
        if (post.isEmpty()) {
            return false;
        }
        replyRepository.save(new PostReply(post.get(), author, body, avatarColor));
        return true;
    }

    @Transactional
    public boolean deleteReply(Long postId, Long replyId) {
        Optional<PostReply> reply = replyRepository.findByIdAndPostId(replyId, postId);
        if (reply.isEmpty()) {
            return false;
        }
        replyRepository.delete(reply.get());
        return true;
    }

    public long countReplies(Long postId) {
        return replyRepository.countByPostId(postId);
    }
}
