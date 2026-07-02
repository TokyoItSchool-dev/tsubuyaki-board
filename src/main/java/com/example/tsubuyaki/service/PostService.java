package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;
    private final TagService tagService;
    private final Clock clock;

    /**
     * 投稿サービスを生成する。
     *
     * @param repository 投稿リポジトリ
     * @param tagService タグサービス
     * @param clock 現在日時取得用 Clock
     */
    public PostService(PostRepository repository, TagService tagService, Clock clock) {
        this.repository = repository;
        this.tagService = tagService;
        this.clock = clock;
    }

    /**
     * 未削除の最新投稿を最大 50 件取得する。
     *
     * @return 最新投稿一覧
     */
    public List<Post> latest() {
        return repository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    /**
     * 本文にキーワードを含む未削除投稿を検索する。
     *
     * @param keyword 検索キーワード
     * @return 検索結果の投稿一覧
     */
    public List<Post> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return latest();
        }
        // 前後空白だけを取り除き、本文 LIKE 検索に渡す。
        return repository.findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc(keyword.strip());
    }

    /**
     * 未削除の投稿を ID で取得する。
     *
     * @param id 投稿 ID
     * @return 投稿
     * @throws NoSuchElementException 投稿が存在しない、または削除済みの場合
     */
    public Post findById(long id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NoSuchElementException("Post not found: " + id));
    }

    /**
     * 指定された ID 順で未削除投稿を取得する。
     *
     * @param ids 投稿 ID 一覧
     * @return ID 一覧の順序に並べ直した投稿一覧
     */
    public List<Post> findByIdsInOrder(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Post> postsById = new LinkedHashMap<>();
        // DB から取得した投稿を ID で引けるようにして、後段で指定順へ並べ直す。
        for (Post post : repository.findByIdInAndDeletedAtIsNull(ids)) {
            postsById.put(post.getId(), post);
        }
        List<Post> posts = new ArrayList<>();
        for (Long id : ids) {
            Post post = postsById.get(id);
            // 削除済みなどで取得できなかった ID は結果から除外する。
            if (post != null) {
                posts.add(post);
            }
        }
        return posts;
    }

    /**
     * 投稿を作成し、本文中のタグも登録する。
     *
     * @param author 投稿者名
     * @param body 本文
     * @param color 背景色コード
     * @param clientHash 投稿者判定に使うクライアントハッシュ
     */
    @Transactional
    public void create(String author, String body, String color, String clientHash) {
        Post post = repository.save(new Post(author, body, Instant.now(clock), color, clientHash));
        // 投稿 ID が確定してから、本文中のタグを投稿に紐づけて保存する。
        tagService.createForPost(post.getId(), body);
    }

    /**
     * 投稿者本人の要求であれば投稿を論理削除する。
     *
     * @param id 投稿 ID
     * @param clientHash 要求元クライアントのハッシュ
     * @return 削除できた場合 true、権限がない場合 false
     */
    @Transactional
    public boolean delete(long id, String clientHash) {
        Post post = findById(id);
        // 投稿時の clientHash と一致しない場合は削除させない。
        if (!post.getClientHash().equals(clientHash)) {
            return false;
        }
        post.markDeleted(Instant.now(clock));
        return true;
    }
}
