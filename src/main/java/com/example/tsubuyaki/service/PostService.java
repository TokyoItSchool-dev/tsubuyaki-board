package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;
    private final Clock clock;

    @Autowired
    public PostService(PostRepository repository) {
        this(repository, Clock.systemUTC());
    }

    PostService(PostRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public List<Post> latest() {
        return findLatest50Posts();
    }

    public List<Post> findLatest50Posts() {
        List<Post> posts = repository.findTop50ByOrderByCreatedAtDesc();
        if (posts == null) {
            return List.of();
        }
        return posts;
    }

    public Optional<Post> findDetailPost(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id);
    }

    @Transactional
    public void createPost(String author, String body) {
        repository.save(new Post(author, body, Instant.now(clock)));
    }
}
