/*
 * 投稿の取得、検索、作成と、投稿本文から抽出したタグの保存をまとめる
 * アプリケーションサービス。
 */
package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final TagParser tagParser;
    private final Clock clock;

    public PostService(PostRepository postRepository, TagRepository tagRepository, TagParser tagParser,
            Clock clock) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.tagParser = tagParser;
        this.clock = clock;
    }

    /**
     * 一覧画面に表示する最新投稿を取得する。
     *
     * <p>画面が重くならないよう、リポジトリ側の派生クエリで作成日時の降順に
     * 最大 50 件だけ取得する。</p>
     *
     * @return 最新順の投稿一覧
     */
    public List<Post> latest() {
        return postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    /**
     * 投稿本文を対象にキーワード検索する。
     *
     * <p>コントローラで空文字を除外した後に呼び出されるため、このメソッドでは
     * 本文にキーワードを含む投稿だけを新しい順に取得する。</p>
     *
     * @param keyword 本文検索キーワード
     * @return 本文にキーワードを含む投稿一覧
     */
    public List<Post> search(String keyword) {
        return postRepository.findByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc(keyword);
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id)
                .filter(post -> post.getDeletedAt() == null);
    }

    /**
     * タグ名に完全一致する投稿を取得する。
     *
     * <p>{@code GET /tags/{name}} で直接タグ別一覧を開くために使う。
     * 保存済みタグ名と一致した投稿だけを新しい順に返す。</p>
     *
     * @param name タグ名
     * @return 指定タグに紐づく投稿一覧
     */
    public List<Post> findByTag(String name) {
        return tagRepository.findPostsByNameOrderByCreatedAtDesc(name);
    }

    /**
     * タグ名を部分一致で検索し、該当タグを持つ投稿を取得する。
     *
     * <p>一覧画面のタグ検索窓から使うため、タグ名そのものを完全一致ではなく
     * LIKE 相当で検索する。</p>
     *
     * @param keyword タグ検索キーワード
     * @return タグ名にキーワードを含む投稿一覧
     */
    public List<Post> searchByTag(String keyword) {
        return tagRepository.findPostsByNameLikeOrderByCreatedAtDesc(toLikePattern(keyword));
    }

    /**
     * アバター色を指定せずに投稿を作成する。
     *
     * <p>既存呼び出し元との互換性を保つための入口。実際の保存処理は
     * アバター色付きの作成メソッドへ委譲する。</p>
     *
     * @param author 投稿者名
     * @param body 本文
     * @return 保存した投稿
     */
    @Transactional
    public Post create(String author, String body) {
        return create(author, body, null);
    }

    /**
     * 投稿と本文中のタグを保存する。
     *
     * <p>投稿を先に保存して ID を確定させた後、本文中の {@code #tag} を抽出して
     * 投稿に紐づく {@link Tag} として保存する。アバター色は任意入力のため
     * {@code null} や空文字でもエラーにしない。</p>
     *
     * @param author 投稿者名
     * @param body 本文
     * @param avatarColor アバター色
     * @return 保存した投稿
     */
    @Transactional
    public Post create(String author, String body, String avatarColor) {
        Post post = postRepository.save(new Post(author, body, LocalDateTime.now(clock), avatarColor));
        tagRepository.saveAll(tagParser.extractTags(body).stream()
                .map(tagName -> new Tag(tagName, post))
                .toList());
        return post;
    }

    /**
     * 投稿を論理削除する。
     *
     * <p>物理削除はせず、対象投稿の {@code deletedAt} に現在時刻を設定する。
     * 以後の一覧、本文検索、タグ検索、詳細表示では未削除投稿だけを対象にする。</p>
     *
     * @param id 投稿 ID
     */
    @Transactional
    public void delete(Long id) {
        Post post = postRepository.findById(id)
                .filter(target -> target.getDeletedAt() == null)
                .orElseThrow(() -> new NoSuchElementException("post not found: " + id));
        post.markDeleted(LocalDateTime.now(clock));
    }

    private static String toLikePattern(String keyword) {
        return "%" + escapeLike(keyword) + "%";
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
