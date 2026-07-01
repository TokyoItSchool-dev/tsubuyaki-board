package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public PostLikeService(PostRepository postRepository, PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
    }

    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    @Transactional
    public void toggleLike(Long postId, String clientHash) {
        if (!postRepository.existsById(postId)) {
            throw new NoSuchElementException("post not found: " + postId);
        }

        postLikeRepository.findByPostIdAndClientHash(postId, clientHash)
                .ifPresentOrElse(
                        postLikeRepository::delete,
                        () -> postLikeRepository.save(new PostLike(postId, clientHash)));
    }
}
