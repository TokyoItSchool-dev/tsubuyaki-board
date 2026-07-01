package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;

    private final Map<Long, Set<String>> likedClientHashesByPostId = new ConcurrentHashMap<>();

    public PostService(PostRepository repository) {
        this.repository = repository;
    }

    public List<Post> findLatest50() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    public long countLikes(Long postId) {
        return likedClientHashesByPostId.getOrDefault(postId, Collections.emptySet()).size();
    }

    public void toggleLike(Long postId, String clientHash) {
        likedClientHashesByPostId.compute(postId, (id, clientHashes) -> {
            Set<String> updatedClientHashes = clientHashes;
            if (updatedClientHashes == null) {
                updatedClientHashes = ConcurrentHashMap.newKeySet();
            }
            if (!updatedClientHashes.add(clientHash)) {
                updatedClientHashes.remove(clientHash);
            }
            return updatedClientHashes.isEmpty() ? null : updatedClientHashes;
        });
    }

    @Transactional
    public void create(String author, String body) {
        repository.save(new Post(author, body, Instant.now()));
    }
}
