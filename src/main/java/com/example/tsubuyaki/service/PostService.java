package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.AvatarColor;
import com.example.tsubuyaki.repository.LikeRepository;
import com.example.tsubuyaki.repository.PostEntity;
import com.example.tsubuyaki.repository.PostEntityMapper;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;
    private final LikeRepository likeRepository;
    private final TagService tagService;
    private final Clock clock;

    public PostService(PostRepository repository, LikeRepository likeRepository, TagService tagService, Clock clock) {
        this.repository = repository;
        this.likeRepository = likeRepository;
        this.tagService = tagService;
        this.clock = Objects.requireNonNullElse(clock, Clock.systemUTC());
    }

    List<Post> latest() {
        return toDomainList(repository.findTop50ByDeletedAtIsNullOrderByCreatedAtDescIdDesc());
    }

    public List<Post> findPosts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return latest();
        }
        return searchPosts(keyword);
    }

    List<Post> searchPosts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return latest();
        }
        return toDomainList(repository
                .findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDescIdDesc(keyword.strip()));
    }

    public Post getById(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .map(PostEntityMapper::toDomain)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    public List<Post> findPostsByTag(String tagName) {
        return toDomainList(repository.findTop50ByDeletedAtIsNullAndTagsNameOrderByCreatedAtDescIdDesc(tagName));
    }

    public PostDetail getDetail(Long id) {
        Post post = getById(id);
        return new PostDetail(post, likeRepository.countByPostId(id));
    }

    @Transactional
    public Post create(String author, String body) {
        return create(author, AvatarColor.DEFAULT.name(), body);
    }

    @Transactional
    public Post create(String author, String avatarColor, String body) {
        Post post = new Post(author, avatarColor, body, Instant.now(clock));
        List<TagEntity> tags = tagService.resolveTags(post.getBody());
        return PostEntityMapper.toDomain(repository.save(PostEntityMapper.toEntity(post, tags)));
    }

    @Transactional
    public void delete(Long id) {
        PostEntity post = repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        post.markDeleted(Instant.now(clock));
    }

    private static List<Post> toDomainList(List<PostEntity> entities) {
        return entities.stream()
                .map(PostEntityMapper::toDomain)
                .toList();
    }
}
