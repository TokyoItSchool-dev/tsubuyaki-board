package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.domain.Reply;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.ReplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;
    private final PostLikeRepository postLikeRepository;
    private final ReplyRepository replyRepository;
    private final Clock clock;

    @Autowired
    public PostService(
            PostRepository repository,
            PostLikeRepository postLikeRepository,
            ReplyRepository replyRepository) {
        this(repository, postLikeRepository, replyRepository, Clock.systemUTC());
    }

    PostService(PostRepository repository, PostLikeRepository postLikeRepository) {
        this(repository, postLikeRepository, null, Clock.systemUTC());
    }

    PostService(PostRepository repository, PostLikeRepository postLikeRepository, Clock clock) {
        this(repository, postLikeRepository, null, clock);
    }

    PostService(
            PostRepository repository,
            PostLikeRepository postLikeRepository,
            ReplyRepository replyRepository,
            Clock clock) {
        this.repository = repository;
        this.postLikeRepository = postLikeRepository;
        this.replyRepository = replyRepository;
        this.clock = clock;
    }

    public List<Post> latest() {
        return findLatest50Posts();
    }

    public List<Post> findLatest50Posts() {
        List<Post> posts = repository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
        if (posts == null) {
            return List.of();
        }
        return posts;
    }

    public List<Post> searchPosts(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isEmpty()) {
            return findLatest50Posts();
        }

        List<Post> posts = repository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc(normalizedKeyword);
        if (posts == null) {
            return List.of();
        }
        return posts;
    }

    public Optional<Post> findDetailPost(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id);
    }

    public List<ReplyThreadItem> findReplyThread(Long postId) {
        List<Reply> replies = replyRepository.findByPostIdOrderByCreatedAtAscIdAsc(postId);
        if (replies == null) {
            return List.of();
        }

        Set<Long> replyIds = new HashSet<>();
        for (Reply reply : replies) {
            replyIds.add(reply.getId());
        }

        List<Reply> roots = new ArrayList<>();
        Map<Long, List<Reply>> childrenByParentId = new LinkedHashMap<>();
        for (Reply reply : replies) {
            Long parentId = reply.getParentId();
            if (parentId == null || !replyIds.contains(parentId)) {
                roots.add(reply);
                continue;
            }
            childrenByParentId.computeIfAbsent(parentId, ignored -> new ArrayList<>()).add(reply);
        }

        List<ReplyThreadItem> thread = new ArrayList<>();
        for (Reply root : roots) {
            appendReply(thread, root, childrenByParentId, 0);
        }
        return thread;
    }

    public boolean hasLiked(Long postId, String clientHash) {
        return postLikeRepository.existsByPostIdAndClientHash(postId, clientHash);
    }

    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    @Transactional
    public void toggleLike(Long postId, String clientHash) {
        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndClientHash(postId, clientHash);
        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.orElseThrow());
            return;
        }

        Post post = repository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new NoSuchElementException("投稿が見つかりません: " + postId));
        postLikeRepository.save(new PostLike(post, clientHash, Instant.now(clock)));
    }

    @Transactional
    public void createPost(String author, String body) {
        createPost(author, "red", body);
    }

    @Transactional
    public void createPost(String author, String avatarColor, String body) {
        repository.save(new Post(author, avatarColor, body, Instant.now(clock)));
    }

    @Transactional
    public void createReply(Long postId, Long parentReplyId, String author, String body) {
        Post post = repository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new NoSuchElementException("投稿が見つかりません: " + postId));
        Reply parent = null;
        if (parentReplyId != null) {
            parent = replyRepository.findByIdAndPostId(parentReplyId, postId)
                    .orElseThrow(() -> new NoSuchElementException("返信が見つかりません: " + parentReplyId));
        }
        replyRepository.save(new Reply(post, parent, author, body, Instant.now(clock)));
    }

    @Transactional
    public void toggleReplyRead(Long postId, Long replyId) {
        Reply reply = replyRepository.findByIdAndPostId(replyId, postId)
                .orElseThrow(() -> new NoSuchElementException("返信が見つかりません: " + replyId));
        if (reply.isRead()) {
            reply.markUnread();
            return;
        }
        reply.markRead(Instant.now(clock));
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = repository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new NoSuchElementException("投稿が見つかりません: " + postId));
        post.markDeleted(Instant.now(clock));
    }

    private static String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim();
    }

    private static void appendReply(
            List<ReplyThreadItem> thread,
            Reply reply,
            Map<Long, List<Reply>> childrenByParentId,
            int depth) {
        thread.add(new ReplyThreadItem(reply, depth));
        List<Reply> children = childrenByParentId.getOrDefault(reply.getId(), List.of());
        for (Reply child : children) {
            appendReply(thread, child, childrenByParentId, depth + 1);
        }
    }
}
