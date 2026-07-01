package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;
    private final Clock clock;

    public PostService(PostRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public List<Post> latest() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public Post findById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Post not found: " + id));
    }

    @Transactional
    public void create(String author, String body) {
        repository.save(new Post(author, body, Instant.now(clock)));
    }
}
