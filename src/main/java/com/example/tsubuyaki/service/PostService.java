package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;
    private final TagRepository tagRepository;
    private final TagParser tagParser;

    public PostService(PostRepository repository, TagRepository tagRepository, TagParser tagParser) {
        this.repository = repository;
        this.tagRepository = tagRepository;
        this.tagParser = tagParser;
    }

    public List<Post> latest() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public List<Post> search(String keyword) {
        return repository.findByBodyContainingOrderByCreatedAtDesc(keyword);
    }

    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    public List<Post> findByTag(String name) {
        return tagRepository.findPostsByNameOrderByCreatedAtDesc(name);
    }

    public List<Post> searchByTag(String keyword) {
        return tagRepository.findPostsByNameContainingOrderByCreatedAtDesc(keyword);
    }

    @Transactional
    public Post create(String author, String body) {
        return create(author, body, null);
    }

    @Transactional
    public Post create(String author, String body, String avatarColor) {
        Post post = repository.save(new Post(author, body, LocalDateTime.now(), avatarColor));
        tagRepository.saveAll(tagParser.extractTags(body).stream()
                .map(tagName -> new Tag(tagName, post))
                .toList());
        return post;
    }
}
