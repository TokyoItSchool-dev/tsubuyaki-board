package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
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

    private final PostRepository repository;
    private final PostLikeRepository likeRepository;

    public PostService(PostRepository repository, PostLikeRepository likeRepository) {
        this.repository = repository;
        this.likeRepository = likeRepository;
    }

    public List<PostView> latest() {
        return repository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    public void create(String author, String body) {
        repository.save(new Post(author, body, LocalDateTime.now()));
    }

    public Optional<PostView> findById(Long id) {
        return repository.findById(id).map(this::toView);
    }

    @Transactional
    public boolean toggleLike(Long postId, String clientHash) {
        Optional<Post> post = repository.findById(postId);
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

    private PostView toView(Post post) {
        return new PostView(
                post.getId(),
                post.getAuthor(),
                post.getBody(),
                post.getCreatedAt(),
                likeRepository.countByPostId(post.getId()));
    }
}
