package com.example.tsubuyaki.service;

import com.example.tsubuyaki.repository.LikeEntity;
import com.example.tsubuyaki.repository.LikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    public LikeService(LikeRepository likeRepository, PostRepository postRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
    }

    @Transactional(noRollbackFor = DataIntegrityViolationException.class)
    public void toggleLike(Long postId, String clientHash) {
        likeRepository.findByPostIdAndClientHash(postId, clientHash)
                .ifPresentOrElse(
                        likeRepository::delete,
                        () -> {
                            ensurePostExists(postId);
                            saveLikeOrDeleteConcurrentLike(postId, clientHash);
                        });
    }

    public long countByPostId(Long postId) {
        ensurePostExists(postId);
        return likeRepository.countByPostId(postId);
    }

    private void ensurePostExists(Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    private void saveLikeOrDeleteConcurrentLike(Long postId, String clientHash) {
        try {
            likeRepository.saveAndFlush(new LikeEntity(postId, clientHash));
        } catch (DataIntegrityViolationException e) {
            likeRepository.findByPostIdAndClientHash(postId, clientHash)
                    .ifPresent(likeRepository::delete);
        }
    }
}
