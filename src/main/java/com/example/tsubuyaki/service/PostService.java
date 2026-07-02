package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    public static final int PAGE_SIZE = 50;

    private final PostRepository repository;

    private final PostLikeRepository likeRepository;

    public PostService(PostRepository repository, PostLikeRepository likeRepository) {
        this.repository = repository;
        this.likeRepository = likeRepository;
    }

    public List<Post> latest() {
        return repository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    public Page<Post> latestPage(int page) {
        int pageNumber = Math.max(page, 0);
        return repository.findAllByDeletedAtIsNullOrderByCreatedAtDesc(PageRequest.of(pageNumber, PAGE_SIZE));
    }

    public Page<Post> searchPage(String query, int page) {
        int pageNumber = Math.max(page, 0);
        String normalizedQuery = normalizeForSearch(query);
        List<Post> matches = repository.findAllByDeletedAtIsNullOrderByCreatedAtDesc().stream()
                .filter(post -> normalizeForSearch(post.getBody()).contains(normalizedQuery))
                .toList();
        int start = Math.min(pageNumber * PAGE_SIZE, matches.size());
        int end = Math.min(start + PAGE_SIZE, matches.size());
        return new PageImpl<>(matches.subList(start, end), PageRequest.of(pageNumber, PAGE_SIZE), matches.size());
    }

    public Optional<Post> findVisibleById(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id);
    }

    public List<Post> trashedPosts() {
        return repository.findAllByDeletedAtIsNotNullOrderByDeletedAtDesc();
    }

    @Transactional
    public Optional<Post> restoreFromTrash(Long id) {
        Optional<Post> post = repository.findByIdAndDeletedAtIsNotNull(id);
        post.ifPresent(target -> target.setDeletedAt(null));
        return post;
    }

    @Transactional
    public long emptyTrash() {
        long trashedPostCount = repository.countByDeletedAtIsNotNull();
        if (trashedPostCount > 0) {
            likeRepository.deleteAllByPostDeletedAtIsNotNull();
            repository.deleteAllByDeletedAtIsNotNull();
        }
        return trashedPostCount;
    }

    public long likeCount(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    @Transactional
    public Post create(String author, String body, String avatarColor,
            String avatarImageContentType, byte[] avatarImageData) {
        Post post = new Post();
        post.setAuthor(author);
        post.setBody(body);
        post.setAvatarColor(avatarColor);
        post.setAvatarImageContentType(avatarImageContentType);
        post.setAvatarImageData(avatarImageData);
        post.setCreatedAt(Instant.now());
        return repository.save(post);
    }

    @Transactional
    public Optional<Post> update(Long id, String author, String body, String avatarColor,
            String avatarImageContentType, byte[] avatarImageData) {
        Optional<Post> post = repository.findByIdAndDeletedAtIsNull(id);
        post.ifPresent(target -> {
            target.setAuthor(author);
            target.setBody(body);
            target.setAvatarColor(avatarColor);
            target.setAvatarImageContentType(avatarImageContentType);
            target.setAvatarImageData(avatarImageData);
        });
        return post;
    }

    @Transactional
    public Optional<Post> moveToTrash(Long id) {
        Optional<Post> post = repository.findByIdAndDeletedAtIsNull(id);
        post.ifPresent(target -> target.setDeletedAt(Instant.now()));
        return post;
    }

    @Transactional
    public Optional<Post> toggleLike(Long id, String clientHash) {
        Optional<Post> post = repository.findByIdAndDeletedAtIsNull(id);
        post.ifPresent(target -> likeRepository.findByPostAndClientHash(target, clientHash)
                .ifPresentOrElse(likeRepository::delete,
                        () -> likeRepository.save(new PostLike(target, clientHash, Instant.now()))));
        return post;
    }

    private static String normalizeForSearch(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFKC)
                .toLowerCase(java.util.Locale.ROOT)
                .strip();
    }
}
