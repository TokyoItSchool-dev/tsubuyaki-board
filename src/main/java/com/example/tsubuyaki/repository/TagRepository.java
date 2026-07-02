/*
 * 投稿本文から保存したタグと、タグに紐づく投稿を検索する
 * Spring Data JPA リポジトリ。
 */
package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * タグ名に完全一致するタグ行を投稿の作成日時が新しい順に取得する。
     *
     * @param name タグ名
     * @return 指定タグ名に一致するタグ一覧
     */
    List<Tag> findByNameOrderByPostCreatedAtDesc(String name);

    /**
     * タグ名に完全一致する未削除の投稿を重複なしで取得する。
     *
     * <p>1 投稿内の同名タグはユニーク制約で重複保存しないが、明示的に
     * {@code distinct} を付けて投稿一覧として重複しないことを保証する。</p>
     *
     * @param name タグ名
     * @return 指定タグに紐づく投稿一覧
     */
    @Query("select distinct t.post from Tag t "
            + "where t.name = :name and t.post.deletedAt is null "
            + "order by t.post.createdAt desc")
    List<Post> findPostsByNameOrderByCreatedAtDesc(@Param("name") String name);

    /**
     * タグ名にキーワードを含む未削除の投稿を重複なしで取得する。
     *
     * <p>一覧画面のタグ検索窓から使うため、完全一致ではなく LIKE 検索にしている。
     * 検索語を直接クエリ文字列へ埋め込まず、ワイルドカード付き pattern を
     * bind 変数として渡す。</p>
     *
     * @param keyword タグ検索キーワード
     * @return タグ名にキーワードを含む投稿一覧
     */
    default List<Post> findPostsByNameContainingOrderByCreatedAtDesc(String keyword) {
        return findPostsByNameLikeOrderByCreatedAtDesc("%" + keyword + "%");
    }

    @Query("select distinct t.post from Tag t "
            + "where t.post.deletedAt is null "
            + "and t.name like :pattern escape '\\' "
            + "order by t.post.createdAt desc")
    List<Post> findPostsByNameLikeOrderByCreatedAtDesc(@Param("pattern") String pattern);
}
