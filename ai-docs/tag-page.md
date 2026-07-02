# タグページ `/tags/{タグ名}` TDD計画

## 概要

`/tags/{タグ名}` で、そのタグが登録されている投稿だけを表示する。

タグページでは HTML `<title>` と画面見出しにタグ名を表示する。

表示順は以下の 2 種類を切り替えられるようにする。

- 最新ポスト順: 作成日降順
- 人気ポスト順: いいね数降順、同数の場合は作成日降順

## インクリメント 1: タグページの基本表示

### 要件

- `GET /tags/{name}` を追加する。
- 指定タグに関連付いた投稿を取得する。
- View は既存の `posts/list` を再利用する。
- Model に `posts` と `tagName` を渡す。
- 存在しないタグは 404 にせず、空一覧として表示する。

### Red TODO

- `PostControllerTest` に `タグページ_GET_tags_name_関連投稿をposts_listへ渡す` を追加する。
- `PostControllerTest` に `タグページ_存在しないタグ_空一覧を表示する` を追加する。
- `PostServiceTest` に `タグページ_タグ名で関連投稿を返す` を追加する。

## インクリメント 2: ページタイトルと見出しにタグ名を表示

### 要件

- タグページでは HTML `<title>` に `#タグ名` を含める。
- タグページでは画面見出しに `#タグ名` を表示する。
- 通常の投稿一覧ページでは既存のタイトル・見出しを維持する。
- タグ名表示は `th:text` を使う。

### Red TODO

- `PostControllerTest` に `タグページ_タイトルにタグ名を表示する` を追加する。
- `PostControllerTest` に `タグページ_見出しにタグ名を表示する` を追加する。
- `PostControllerTest` に `投稿一覧_通常表示_既存タイトルと見出しを維持する` を追加または確認する。

## インクリメント 3: 最新ポスト順の表示

### 要件

- タグページのデフォルト表示順は最新ポスト順にする。
- `sort` 未指定時は `latest` として扱う。
- `GET /tags/{name}?sort=latest` でも最新ポスト順にする。
- 最新ポスト順は作成日降順、最大 50 件で返す。

### Red TODO

- `PostControllerTest` に `タグページ_sort未指定_latestでServiceを呼ぶ` を追加する。
- `PostControllerTest` に `タグページ_sort_latest_latestでServiceを呼ぶ` を追加する。
- `PostServiceTest` に `タグページ_latest_最新順Repositoryを呼ぶ` を追加する。
- Repository テストに `タグページ_latest_作成日降順で最大50件返す` を追加する。

## インクリメント 4: 人気ポスト順の表示

### 要件

- `GET /tags/{name}?sort=popular` で人気ポスト順にする。
- 人気ポスト順は、いいね数降順、同数なら作成日降順にする。
- いいね 0 件の投稿も結果に含める。
- 指定タグ以外の投稿は含めない。
- 最大 50 件で返す。

### Red TODO

- `PostControllerTest` に `タグページ_sort_popular_popularでServiceを呼ぶ` を追加する。
- `PostServiceTest` に `タグページ_popular_人気順Repositoryを呼ぶ` を追加する。
- Repository テストに `タグページ_popular_いいね数降順で返す` を追加する。
- Repository テストに `タグページ_popular_いいね同数なら作成日降順で返す` を追加する。
- Repository テストに `タグページ_popular_いいね0件も含める` を追加する。

## インクリメント 5: 並び替えリンクと選択状態

### 要件

- タグページに「最新ポスト」「人気ポスト」のリンクを表示する。
- 最新ポストリンクは `/tags/{name}?sort=latest` にする。
- 人気ポストリンクは `/tags/{name}?sort=popular` にする。
- 現在の並び順を Model の `sort` に保持する。
- 不正な `sort` は `latest` として扱う。
- 通常の投稿一覧ページにはタグページ用の並び替えリンクを出さない。

### Red TODO

- `PostControllerTest` に `タグページ_最新ポストリンクを表示する` を追加する。
- `PostControllerTest` に `タグページ_人気ポストリンクを表示する` を追加する。
- `PostControllerTest` に `タグページ_sort不正_latestとして扱う` を追加する。
- `PostControllerTest` に `投稿一覧_通常表示_タグ並び替えリンクを表示しない` を追加する。

## インクリメント 6: 詳細画面からタグページへの導線

### 要件

- 投稿詳細画面のタグを `#tag` 形式のリンクで表示する。
- リンク先は `/tags/{name}` にする。
- タグ名表示は `th:text` を使う。
- タグがない投稿ではタグリンク領域を表示しない。

### Red TODO

- `PostControllerTest` に `投稿詳細_タグリンクを表示する` を追加する。
- `PostControllerTest` に `投稿詳細_タグなし_タグリンク領域を表示しない` を追加する。
- `PostControllerTest` に `投稿詳細_タグ名表示_th_textでエスケープされる` を追加する。

## テスト計画

```bash
./mvnw -B -Ph2 -Dtest=PostControllerTest test
./mvnw -B -Ph2 -Dtest=PostServiceTest test
./mvnw -B -Ph2 -Dtest=TagRepositoryTest test
./mvnw -B -Ph2 verify
```

## 前提

- タグページ専用テンプレートは作らず、既存の `posts/list` を条件付きで拡張する。
- 人気ポスト順の「人気」は、投稿に付いたいいね数で判定する。
- URL の並び順指定は `sort=latest|popular` とする。
- 存在しないタグは 404 ではなく空一覧にする。
- タグ名は日本語を許可する。
