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

    private final PostRepository repository;
    private final PostLikeRepository likeRepository;

    public PostService(PostRepository repository, PostLikeRepository likeRepository) {
        this.repository = repository;
        this.likeRepository = likeRepository;
    }

    public List<PostView> latest() {
        return toViews(repository.findTop50ByOrderByCreatedAtDesc());
    }

    public List<PostView> searchByBody(String keyword) {
        return toViews(repository.findTop50ByBodyContainingOrderByCreatedAtDesc(keyword));
    }

    @Transactional
    public void create(String author, String body) {
        create(author, body, PostBackgroundColor.DEFAULT);
    }

    @Transactional
    public void create(String author, String body, String backgroundColor) {
        repository.save(new Post(author, body, LocalDateTime.now(), backgroundColor));
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
                likeRepository.countByPostId(post.getId()),
                post.getBackgroundColor());
    }

    private List<PostView> toViews(List<Post> posts) {
        return posts.stream()
                .map(this::toView)
                .toList();
    }
}
