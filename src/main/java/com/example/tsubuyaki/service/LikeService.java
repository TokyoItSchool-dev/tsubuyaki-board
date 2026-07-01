package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class LikeService {

    private final PostLikeRepository postLikeRepository;

    private final Clock clock;

    public LikeService(PostLikeRepository postLikeRepository, Clock clock) {
        this.postLikeRepository = postLikeRepository;
        this.clock = clock;
    }

    public long countByPostId(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    @Transactional
    public void toggle(Long postId, String clientHash) {
        postLikeRepository.findByPostIdAndClientHash(postId, clientHash)
                .ifPresentOrElse(postLikeRepository::delete,
                        () -> postLikeRepository.save(new PostLike(postId, clientHash, Instant.now(clock))));
    }
}
