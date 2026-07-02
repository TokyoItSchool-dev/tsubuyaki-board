package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostRepository postRepository;

    private final PostLikeRepository postLikeRepository;

    public PostLikeService(PostRepository postRepository, PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
    }

    public long countByPostId(Long postId) {
        // 詳細画面に表示するため、投稿idに紐づく現在のいいね数を取得する。
        return postLikeRepository.countByPostId(postId);
    }

    @Transactional
    public void toggle(Long postId, String clientHash) {
        // 同じ投稿への同時トグルを直列化するため、投稿行を悲観ロック付きで取得する。
        Post post = postRepository.findByIdForUpdate(postId).orElseThrow(PostNotFoundException::new);

        postLikeRepository.findByPostIdAndClientHash(postId, clientHash).ifPresentOrElse(
                existingLike -> {
                    // 同じclientHashのいいねが既にある場合は、トグル解除として削除する。
                    postLikeRepository.delete(existingLike);
                },
                () -> {
                    // 未登録のclientHashの場合は、新しいいいねとして保存する。
                    postLikeRepository.save(new PostLike(post, clientHash, LocalDateTime.now()));
                });
    }
}
