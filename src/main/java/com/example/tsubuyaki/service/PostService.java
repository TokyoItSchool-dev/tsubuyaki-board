package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> findLatestPosts() {
        return postRepository.findTop50ByOrderByCreatedAtDesc();
    }

    @Transactional
    public void createPost(String author, String body) {
        postRepository.save(new Post(author, body, LocalDateTime.now()));
    }
}
