package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PostSearchService {

    private static final int MAX_RESULTS = 50;

    private final PostRepository postRepository;

    public PostSearchService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> search(String q) {
        if (q == null || q.isBlank()) {
            return postRepository.findTop50ByOrderByCreatedAtDesc();
        }

        String keyword = q.trim();
        return mergeByNewest(
                postRepository.findTop50ByBodyContainingIgnoreCaseOrderByCreatedAtDesc(keyword),
                postRepository.findByTagNameOrderByCreatedAtDesc(
                        normalizeTagName(keyword), PageRequest.of(0, MAX_RESULTS)));
    }

    private String normalizeTagName(String keyword) {
        if (keyword.startsWith("#")) {
            return keyword.substring(1);
        }
        return keyword;
    }

    private List<Post> mergeByNewest(List<Post> bodyPosts, List<Post> tagPosts) {
        List<Post> posts = new ArrayList<>();
        posts.addAll(bodyPosts);
        posts.addAll(tagPosts);

        Map<Object, Post> distinctPosts = new LinkedHashMap<>();
        posts.stream()
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .forEach(post -> distinctPosts.putIfAbsent(uniqueKey(post), post));
        return distinctPosts.values().stream()
                .limit(MAX_RESULTS)
                .toList();
    }

    private Object uniqueKey(Post post) {
        if (post.getId() == null) {
            return post;
        }
        return post.getId();
    }
}
