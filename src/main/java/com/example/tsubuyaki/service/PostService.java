package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.web.dto.PostDto;
import com.example.tsubuyaki.web.dto.PostForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }

    public List<PostDto> latest() {
        List<Post> posts = repository.findTop50ByOrderByCreatedAtDesc();
        if (posts == null) {
            return List.of();
        }
        return posts.stream()
                .map(PostDto::from)
                .toList();
    }

    @Transactional
    public void create(PostForm form) {
        repository.save(new Post(form.getAuthor(), form.getBody(), Instant.now()));
    }
}
