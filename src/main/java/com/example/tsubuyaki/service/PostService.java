package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.domain.PostLikeId;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.web.dto.PostView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;
    private final PostLikeRepository likeRepository;
    private final Clock clock;

    public PostService(PostRepository repository, PostLikeRepository likeRepository, Clock clock) {
        this.repository = repository;
        this.likeRepository = likeRepository;
        this.clock = clock;
    }

    public List<PostView> latest() {
        List<Post> posts = repository.findTop50ByOrderByCreatedAtDesc();
        return toPostViews(posts);
    }

    public List<PostView> searchByBody(String query) {
        List<Post> posts = repository.findTop50ByBodyContainingOrderByCreatedAtDesc(query);
        return toPostViews(posts);
    }

    private List<PostView> toPostViews(List<Post> posts) {
        Map<Long, Long> likeCounts = likeCounts(posts.stream().map(Post::getId).toList());
        return posts.stream()
                .map(post -> new PostView(post, likeCounts.getOrDefault(post.getId(), 0L)))
                .toList();
    }

    public Optional<PostView> findById(Long id) {
        return repository.findById(id)
                .map(post -> new PostView(post, likeRepository.countByIdPostId(post.getId())));
    }

    public Optional<PostView> findById(Long id, String clientHash) {
        return repository.findById(id)
                .map(post -> new PostView(
                        post,
                        likeRepository.countByIdPostId(post.getId()),
                        likeRepository.existsById(new PostLikeId(post.getId(), clientHash))));
    }

    @Transactional
    public Optional<PostView> toggleLike(Long postId, String clientHash) {
        Optional<Post> post = repository.findById(postId);
        if (post.isEmpty()) {
            return Optional.empty();
        }

        PostLikeId id = new PostLikeId(postId, clientHash);
        if (likeRepository.existsById(id)) {
            likeRepository.deleteById(id);
        } else {
            likeRepository.save(new PostLike(id, Instant.now(clock)));
        }

        return Optional.of(new PostView(post.get(), likeRepository.countByIdPostId(postId)));
    }

    @Transactional
    public void create(String author, String body) {
        Long nextId = repository.findMaxId() + 1;
        repository.save(new Post(nextId, author, body, Instant.now(clock)));
    }

    private Map<Long, Long> likeCounts(Collection<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }
        return likeRepository.findCountsByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(
                        PostLikeRepository.PostLikeCount::getPostId,
                        PostLikeRepository.PostLikeCount::getLikeCount));
    }
}
