package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

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

    @Transactional
    public Post create(String author, String body) {
        return repository.save(new Post(author, body, clock.instant()));
    }
}
