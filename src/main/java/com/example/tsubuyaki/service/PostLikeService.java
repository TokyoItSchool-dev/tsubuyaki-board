package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository repository;
    private final Clock clock;

    public PostLikeService(PostLikeRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public void toggle(long postId, String clientHash) {
        if (repository.existsByPostIdAndClientHash(postId, clientHash)) {
            repository.deleteByPostIdAndClientHash(postId, clientHash);
            return;
        }
        repository.save(new PostLike(postId, clientHash, Instant.now(clock)));
    }

    public long countByPostId(long postId) {
        return repository.countByPostId(postId);
    }
}
