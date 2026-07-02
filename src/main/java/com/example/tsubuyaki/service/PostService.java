package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.domain.PostTag;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.PostTagRepository;
import com.example.tsubuyaki.repository.TagRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class PostService {

    private static final int LATEST_POST_LIMIT = 50;

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final TagExtractor tagExtractor = new TagExtractor();

    public PostService(PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            TagRepository tagRepository,
            PostTagRepository postTagRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.tagRepository = tagRepository;
        this.postTagRepository = postTagRepository;
    }

    public List<Post> latest() {
        List<Long> ids = postRepository.findLatestIds(PageRequest.of(0, LATEST_POST_LIMIT));
        if (ids.isEmpty()) {
            return List.of();
        }
        return postRepository.findAllWithTagsByIdIn(ids);
    }

    public List<Post> search(String query) {
        if (!StringUtils.hasText(query)) {
            return latest();
        }

        return postRepository.findByKeywordWithTags(query);
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findByIdAndDeletedAtIsNull(id);
    }

    public List<Post> findByTagName(String name) {
        return postTagRepository.findPostsByTagName(name);
    }

    public List<Tag> findTagsByPostId(Long postId) {
        return postTagRepository.findTagsByPostId(postId);
    }

    @Transactional
    public void create(String author, String body) {
        create(author, body, Post.DEFAULT_AVATAR_COLOR);
    }

    @Transactional
    public void create(String author, String body, String avatarColor) {
        Post post = new Post(author, body, avatarColor, Instant.now());
        postRepository.save(post);
        saveTags(post, body);
    }

    @Transactional
    public Optional<Post> update(Long id, String author, String body) {
        return update(id, author, body, Post.DEFAULT_AVATAR_COLOR);
    }

    @Transactional
    public Optional<Post> update(Long id, String author, String body, String avatarColor) {
        return postRepository.findByIdAndDeletedAtIsNull(id)
                .map(post -> {
                    post.update(author, body, avatarColor);
                    postRepository.save(post);
                    postTagRepository.deleteByPostId(post.getId());
                    saveTags(post, body);
                    return post;
                });
    }

    @Transactional
    public Optional<Boolean> toggleLike(Long postId, String clientHash) {
        return postRepository.findByIdAndDeletedAtIsNull(postId)
                .map(post -> toggleLike(postId, post, clientHash));
    }

    @Transactional
    public boolean delete(Long id) {
        return postRepository.findByIdAndDeletedAtIsNull(id)
                .map(post -> {
                    post.delete(Instant.now());
                    postRepository.save(post);
                    return true;
                })
                .orElse(false);
    }

    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    public boolean likedBy(Long postId, String clientHash) {
        return postLikeRepository.existsByPostIdAndClientHash(postId, clientHash);
    }

    private boolean toggleLike(Long postId, Post post, String clientHash) {
        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndClientHash(postId, clientHash);
        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            return false;
        }

        postLikeRepository.save(new PostLike(post, clientHash, Instant.now()));
        return true;
    }

    private void saveTags(Post post, String body) {
        Set<String> tagNames = tagExtractor.extract(body);
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(new Tag(tagName)));
            postTagRepository.save(new PostTag(post, tag));
        }
    }
}
