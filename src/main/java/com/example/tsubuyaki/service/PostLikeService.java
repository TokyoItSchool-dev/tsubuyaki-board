package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository repository;
    private final Clock clock;

    /**
     * いいねサービスを生成する。
     *
     * @param repository いいねリポジトリ
     * @param clock 現在日時取得用 Clock
     */
    public PostLikeService(PostLikeRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /**
     * 同一クライアントのいいね状態を切り替える。
     *
     * @param postId 投稿 ID
     * @param clientHash クライアントハッシュ
     */
    @Transactional
    public void toggle(long postId, String clientHash) {
        // 既にいいね済みなら取り消し、未いいねなら新規作成する。
        if (repository.existsByPostIdAndClientHash(postId, clientHash)) {
            repository.deleteByPostIdAndClientHash(postId, clientHash);
            return;
        }
        repository.save(new PostLike(postId, clientHash, Instant.now(clock)));
    }

    /**
     * 投稿に付いたいいね数を返す。
     *
     * @param postId 投稿 ID
     * @return いいね数
     */
    public long countByPostId(long postId) {
        return repository.countByPostId(postId);
    }
}
