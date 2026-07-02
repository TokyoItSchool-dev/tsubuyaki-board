/*
 * 投稿へのいいね数取得と、利用者ごとのいいね切り替えを扱うサービス。
 */
package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public PostLikeService(PostRepository postRepository, PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
    }

    /**
     * 指定投稿のいいね数を取得する。
     *
     * @param postId 投稿 ID
     * @return 指定投稿に紐づくいいね数
     */
    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    /**
     * 指定投稿に対する利用者のいいね状態を反転する。
     *
     * <p>同じ投稿 ID とクライアントハッシュの組み合わせが既にあれば削除し、
     * なければ新しく保存する。対象投稿が存在しない場合は、詳細画面側で
     * 404 に変換できるよう {@link NoSuchElementException} を投げる。</p>
     *
     * @param postId 投稿 ID
     * @param clientHash 利用者を識別する短いハッシュ
     */
    @Transactional
    public void toggleLike(Long postId, String clientHash) {
        if (!postRepository.existsById(postId)) {
            throw new NoSuchElementException("post not found: " + postId);
        }

        postLikeRepository.findByPostIdAndClientHash(postId, clientHash)
                .ifPresentOrElse(
                        postLikeRepository::delete,
                        () -> postLikeRepository.save(new PostLike(postId, clientHash)));
    }
}
