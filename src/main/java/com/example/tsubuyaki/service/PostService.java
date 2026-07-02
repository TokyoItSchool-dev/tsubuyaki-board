package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostTag;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostApiRow;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.PostTagRepository;
import com.example.tsubuyaki.repository.TagRepository;
import com.example.tsubuyaki.service.dto.PostApiResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class PostService {

    private static final int LATEST_POST_LIMIT = 50;

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final TagExtractor tagExtractor = new TagExtractor();

    public PostService(PostRepository postRepository,
            TagRepository tagRepository,
            PostTagRepository postTagRepository) {
        this.postRepository = postRepository;
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

    public List<Post> deleted() {
        return postRepository.findDeleted();
    }

    public List<PostApiResponse> latestForApi() {
        List<Long> ids = postRepository.findLatestIds(PageRequest.of(0, LATEST_POST_LIMIT));
        if (ids.isEmpty()) {
            return List.of();
        }

        Map<Long, ApiPostBuilder> posts = new LinkedHashMap<>();
        for (PostApiRow row : postRepository.findApiRowsByIds(ids)) {
            ApiPostBuilder post = posts.computeIfAbsent(row.id(), id -> new ApiPostBuilder(row));
            if (row.tagName() != null) {
                post.addTag(row.tagName());
            }
        }
        return posts.values().stream()
                .map(ApiPostBuilder::toResponse)
                .toList();
    }

    public Map<Long, Long> countLikesByPostIds(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> counts = new LinkedHashMap<>();
        postRepository.findLikeCountsByPostIds(postIds)
                .forEach(count -> counts.put(count.postId(), count.likesCount()));
        return counts;
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
    public Optional<Long> incrementLike(Long postId) {
        int updatedRows = postRepository.incrementLikesCountById(postId);
        if (updatedRows == 0) {
            return Optional.empty();
        }
        return postRepository.findLikesCountById(postId);
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
        return postRepository.findLikesCountById(postId).orElse(0L);
    }

    private void saveTags(Post post, String body) {
        Set<String> tagNames = tagExtractor.extract(body);
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(new Tag(tagName)));
            postTagRepository.save(new PostTag(post, tag));
        }
    }

    private static final class ApiPostBuilder {

        private final PostApiRow row;
        private final List<String> tags = new ArrayList<>();

        private ApiPostBuilder(PostApiRow row) {
            this.row = row;
        }

        private void addTag(String tagName) {
            tags.add(tagName);
        }

        private PostApiResponse toResponse() {
            return new PostApiResponse(
                    row.id(),
                    row.author(),
                    row.body(),
                    row.avatarColor(),
                    row.createdAt(),
                    row.updatedAt(),
                    List.copyOf(tags),
                    row.likesCount());
        }
    }
}
