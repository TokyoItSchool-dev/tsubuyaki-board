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

    // 投稿の存在確認に使うRepository。
    private final PostRepository postRepository;

    // いいね情報の検索・追加・削除に使うRepository。
    private final PostLikeRepository postLikeRepository;

    public PostLikeService(PostRepository postRepository, PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
    }

    // 同一clientHashが未いいねなら追加、いいね済みなら削除する。
    @Transactional
    public boolean toggleLike(Long postId, String clientHash) {
        Post post = postRepository.findByIdAndDeletedAt(postId, Post.NOT_DELETED)
                .orElseThrow(() -> new PostNotFoundException(postId));

        return postLikeRepository.findByPostIdAndClientHash(postId, clientHash)
                .map(existingLike -> {
                    postLikeRepository.delete(existingLike);
                    return false;
                })
                .orElseGet(() -> {
                    postLikeRepository.save(new PostLike(post, clientHash, LocalDateTime.now()));
                    return true;
                });
    }

    // 投稿詳細画面に表示するため、指定投稿のいいね数を取得する。
    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }
}
