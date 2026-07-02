package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    private final PostLikeRepository postLikeRepository;

    private final TagRepository tagRepository;

    private final HashtagParser hashtagParser = new HashtagParser();

    public PostService(
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.tagRepository = tagRepository;
    }

    public List<Post> latest() {
        return postRepository.findTop50ByOrderByCreatedAtDesc();
    }

    public Page<Post> latestPage(int page, int size) {
        return postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    public List<Post> searchByBody(String query) {
        return postRepository.findByBodyContainingOrderByCreatedAtDesc(query);
    }

    public Page<Post> searchByBodyPage(String query, int page, int size) {
        return postRepository.findByBodyContainingOrderByCreatedAtDesc(query, PageRequest.of(page, size));
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    public List<Post> findByTag(String tagName) {
        return postRepository.findDistinctByTagsNameOrderByCreatedAtDesc(normalizeTagName(tagName));
    }

    public Page<Post> findByTagPage(String tagName, int page, int size) {
        return postRepository.findDistinctByTagsNameOrderByCreatedAtDesc(
                normalizeTagName(tagName),
                PageRequest.of(page, size));
    }

    public long countLikes(Long id) {
        return postLikeRepository.countByPostId(id);
    }

    @Transactional
    public Post create(String author, String body, String avatarColor) {
        Post post = new Post(author, body, avatarColor, LocalDateTime.now());
        post.replaceTags(resolveTags(body));
        return postRepository.save(post);
    }

    @Transactional
    public boolean update(Long id, String author, String body, String avatarColor) {
        Optional<Post> post = postRepository.findById(id);
        if (post.isEmpty()) {
            return false;
        }

        post.get().update(author, body, avatarColor);
        post.get().replaceTags(resolveTags(body));
        return true;
    }

    @Transactional
    public boolean toggleLike(Long id, String clientHash) {
        Optional<Post> post = postRepository.findById(id);
        if (post.isEmpty()) {
            return false;
        }

        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndClientHash(id, clientHash);
        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            return true;
        }

        postLikeRepository.save(new PostLike(post.get(), clientHash));
        return true;
    }

    private List<Tag> resolveTags(String body) {
        return hashtagParser.parse(body).stream()
                .map(this::findOrCreateTag)
                .toList();
    }

    private Tag findOrCreateTag(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> tagRepository.save(new Tag(name)));
    }

    private String normalizeTagName(String tagName) {
        if (tagName == null) {
            return "";
        }
        return tagName.toLowerCase(Locale.ROOT);
    }
}
