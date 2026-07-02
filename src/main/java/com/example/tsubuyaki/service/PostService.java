package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;
    private final PostSearchService postSearchService;
    private final TagService tagService;

    public PostService(PostRepository repository, PostSearchService postSearchService,
            TagService tagService) {
        this.repository = repository;
        this.postSearchService = postSearchService;
        this.tagService = tagService;
    }

    public List<Post> latest() {
        return repository.findTop50ByDeletedAtOrderByCreatedAtDesc(Post.NOT_DELETED);
    }

    public List<Post> search(String q) {
        return postSearchService.search(q);
    }

    public Optional<Post> findById(Long id) {
        return repository.findByIdAndDeletedAt(id, Post.NOT_DELETED);
    }

    @Transactional
    public Post create(Post post) {
        Post savedPost = repository.save(post);
        tagService.saveTagsFor(savedPost);
        return savedPost;
    }

    @Transactional
    public void delete(Long id) {
        Post post = repository.findByIdAndDeletedAt(id, Post.NOT_DELETED)
                .orElseThrow(() -> new PostNotFoundException(id));
        post.markDeleted();
        repository.save(post);
    }
}
