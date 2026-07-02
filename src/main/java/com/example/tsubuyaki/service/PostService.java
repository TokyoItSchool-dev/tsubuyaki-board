package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;
    private final PostLikeRepository postLikeRepository;

    public PostService(PostRepository repository, PostLikeRepository postLikeRepository) {
        this.repository = repository;
        this.postLikeRepository = postLikeRepository;
    }

    public List<Post> latest() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<PostDetail> findDetail(Long id, String clientHash) {
        return repository.findById(id)
                .map(post -> new PostDetail(post,
                        postLikeRepository.countByPostId(id),
                        postLikeRepository.findByPostIdAndClientHash(id, clientHash).isPresent()));
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Optional<LikeToggleResult> toggleLike(Long id, String clientHash) {
        return repository.findById(id)
                .map(post -> postLikeRepository.findByPostIdAndClientHash(id, clientHash)
                        .map(like -> {
                            postLikeRepository.delete(like);
                            return new LikeToggleResult(false);
                        })
                        .orElseGet(() -> {
                            return saveLike(post, clientHash);
                        }));
    }

    private LikeToggleResult saveLike(Post post, String clientHash) {
        try {
            postLikeRepository.saveAndFlush(new PostLike(post, clientHash, LocalDateTime.now()));
        } catch (DataIntegrityViolationException e) {
            return new LikeToggleResult(true);
        }
        return new LikeToggleResult(true);
    }

    @Transactional
    public void create(String author, String body) {
        try {
            repository.save(new Post(author, body, LocalDateTime.now()));
        } catch (DataAccessException e) {
            throw new PostRegistrationException("投稿の登録に失敗しました", e);
        }
    }
}
