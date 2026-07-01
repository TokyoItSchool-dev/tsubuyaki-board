package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.domain.TagParser;
import com.example.tsubuyaki.domain.User;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
import com.example.tsubuyaki.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public PostService(PostRepository repository, PostLikeRepository postLikeRepository,
            UserRepository userRepository, TagRepository tagRepository) {
        this.repository = repository;
        this.postLikeRepository = postLikeRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    public List<Post> findLatest50() {
        return initializeTags(repository.findTop50ByOrderByCreatedAtDesc());
    }

    public List<Post> searchByBody(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findLatest50();
        }
        return initializeTags(repository.findTop50ByBodyContainingOrderByCreatedAtDesc(keyword));
    }

    public Optional<Post> findById(Long id) {
        return repository.findWithTagsById(id);
    }

    public List<Post> findByTagName(String name) {
        return initializeTags(repository.findDistinctTop50ByTagsNameOrderByCreatedAtDesc(name));
    }

    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    @Transactional
    public Post create(String author, String body) {
        return create(author, body, "");
    }

    @Transactional
    public Post create(String author, String body, String avatarColor) {
        User user = userRepository.findByName(author)
                .map(existing -> {
                    existing.updateAvatarColor(avatarColor);
                    return existing;
                })
                .orElseGet(() -> userRepository.save(new User(author, avatarColor)));
        Post post = new Post(user, body, Instant.now());
        TagParser.extractNames(body).stream()
                .map(this::findOrCreateTag)
                .forEach(post::addTag);
        return repository.save(post);
    }

    @Transactional
    public void toggleLike(Long postId, String clientHash) {
        if (postLikeRepository.existsByPostIdAndClientHash(postId, clientHash)) {
            postLikeRepository.deleteByPostIdAndClientHash(postId, clientHash);
            return;
        }

        postLikeRepository.save(new PostLike(repository.getReferenceById(postId), clientHash, Instant.now()));
    }

    private Tag findOrCreateTag(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> tagRepository.save(new Tag(name)));
    }

    private List<Post> initializeTags(List<Post> posts) {
        List<Long> ids = posts.stream()
                .map(Post::getId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (!ids.isEmpty()) {
            repository.findAllWithTagsByIdIn(ids);
        }
        return posts;
    }
}
