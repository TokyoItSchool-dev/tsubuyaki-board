package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    public static final int PAGE_SIZE = 50;

    private final PostRepository repository;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }

    public List<Post> latest() {
        return repository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    public Page<Post> latestPage(int page) {
        int pageNumber = Math.max(page, 0);
        return repository.findAllByDeletedAtIsNullOrderByCreatedAtDesc(PageRequest.of(pageNumber, PAGE_SIZE));
    }

    public Optional<Post> findVisibleById(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id);
    }

    public List<Post> trashedPosts() {
        return repository.findAllByDeletedAtIsNotNullOrderByDeletedAtDesc();
    }

    @Transactional
    public Post create(String author, String body) {
        Post post = new Post();
        post.setAuthor(author);
        post.setBody(body);
        post.setCreatedAt(Instant.now());
        return repository.save(post);
    }

    @Transactional
    public Optional<Post> update(Long id, String author, String body) {
        Optional<Post> post = repository.findByIdAndDeletedAtIsNull(id);
        post.ifPresent(target -> {
            target.setAuthor(author);
            target.setBody(body);
        });
        return post;
    }

    @Transactional
    public Optional<Post> moveToTrash(Long id) {
        Optional<Post> post = repository.findByIdAndDeletedAtIsNull(id);
        post.ifPresent(target -> target.setDeletedAt(Instant.now()));
        return post;
    }
}
