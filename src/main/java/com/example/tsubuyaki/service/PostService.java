package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }

    public List<Post> latest() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public void create(String author, String body) {
        repository.save(new Post(author, body, Instant.now()));
    }

    @Transactional
    public Optional<Post> update(Long id, String author, String body) {
        return repository.findById(id)
                .map(post -> {
                    post.update(author, body);
                    repository.save(post);
                    return post;
                });
    }
}
