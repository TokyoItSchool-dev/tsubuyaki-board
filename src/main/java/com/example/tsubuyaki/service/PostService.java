package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;
    private final TagService tagService;
    private final Clock clock;

    public PostService(PostRepository repository, TagService tagService, Clock clock) {
        this.repository = repository;
        this.tagService = tagService;
        this.clock = clock;
    }

    public List<Post> latest() {
        return repository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    public List<Post> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return latest();
        }
        return repository.findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc(keyword.strip());
    }

    public Post findById(long id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NoSuchElementException("Post not found: " + id));
    }

    public List<Post> findByIdsInOrder(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Post> postsById = new LinkedHashMap<>();
        for (Post post : repository.findByIdInAndDeletedAtIsNull(ids)) {
            postsById.put(post.getId(), post);
        }
        List<Post> posts = new ArrayList<>();
        for (Long id : ids) {
            Post post = postsById.get(id);
            if (post != null) {
                posts.add(post);
            }
        }
        return posts;
    }

    @Transactional
    public void create(String author, String body, String color, String clientHash) {
        Post post = repository.save(new Post(author, body, Instant.now(clock), color, clientHash));
        tagService.createForPost(post.getId(), body);
    }

    @Transactional
    public boolean delete(long id, String clientHash) {
        Post post = findById(id);
        if (!post.getClientHash().equals(clientHash)) {
            return false;
        }
        post.markDeleted(Instant.now(clock));
        return true;
    }
}
