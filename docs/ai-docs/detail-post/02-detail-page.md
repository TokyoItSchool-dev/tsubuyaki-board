# 詳細ページ インクリメント要件

## 概要

今回のインクリメントは、`GET /posts/{id}` で指定 id の投稿を取得し、
`posts/detail.html` に投稿詳細を表示することに限定する。

投稿が存在する場合は投稿者名、本文、作成日を表示する。
投稿が存在しない場合は HTTP 404 を返す。

## 要件

- URL は `GET /posts/{id}` とする。
- 表示テンプレートは `src/main/resources/templates/posts/detail.html` とする。
- View 名は `posts/detail` とする。
- Controller は path variable の `id` を受け取り、Service 経由で投稿を取得する。
- 投稿詳細ページには以下を表示する。
  - 投稿者名
  - 本文
  - 作成日
- 投稿者名と本文は `th:text` で表示する。
- 「一覧に戻る」リンクを表示する。
- 「一覧に戻る」を押したときは `/posts` に遷移する。
- 指定 id の投稿が存在しない場合は HTTP 404 を返す。
- 専用の 404 エラーページはこのインクリメントでは作らない。

## Red TODO

**1. Service: id 検索結果を返す**

テスト名: `投稿詳細_findById_Repositoryの検索結果を返す`

| 観点 | 内容 |
|---|---|
| Given | `postRepository.findById(1L)` が `Optional.of(post)` を返す |
| When | `postService.findById(1L)` を呼ぶ |
| Then | 戻り値が `Optional.of(post)` |
| Repository | `findById(1L)` が呼ばれる |

**2. Controller: 存在する id の詳細を表示する**

テスト名: `投稿詳細_存在するid_posts_detailビューに投稿を渡す`

| 観点 | 内容 |
|---|---|
| HTTP | `GET /posts/1` が 200 を返す |
| View | `posts/detail` |
| Model | `post` に取得した投稿を渡す |
| HTML | 投稿者名、本文、作成日を表示する |
| リンク | `href="/posts"` の「一覧に戻る」を表示する |

**3. Controller: 存在しない id は 404 を返す**

テスト名: `投稿詳細_存在しないid_404を返す`

| 観点 | 内容 |
|---|---|
| Given | `postService.findById(999L)` が `Optional.empty()` を返す |
| HTTP | `GET /posts/999` が 404 を返す |

## 実装方針

- `PostService` に `findById(Long id)` を追加し、戻り値は `Optional<Post>` とする。
- Repository は `JpaRepository#findById` を使い、独自クエリは追加しない。
- `PostController` に `@GetMapping("/posts/{id}")` を追加する。
- 投稿が存在する場合、Model に `post` として追加して `posts/detail` を返す。
- 投稿が存在しない場合、`ResponseStatusException(HttpStatus.NOT_FOUND)` で 404 にする。
- 既存の `posts/detail.html` を使用し、既存の `.post` 系 CSS を使って表示する。

## テスト計画

Service の id 検索を実装した後、Service テストを実行する。

```bash
./mvnw -B -Ph2 -Dtest=PostServiceTest#投稿詳細_findById_Repositoryの検索結果を返す test
```

Controller とテンプレートを実装した後、投稿詳細関連の Controller テストを実行する。

```bash
./mvnw -B -Ph2 -Dtest=PostControllerTest#投稿詳細_存在するid_posts_detailビューに投稿を渡す+投稿詳細_存在しないid_404を返す test
```

ユースケース完了時に H2 プロファイルで verify を実行する。

```bash
./mvnw -B -Ph2 verify
```

## 前提

- URL は `/post/{id}` ではなく、既存の一覧 URL と整合する `/posts/{id}` で統一する。
- 専用の 404 HTML は作らず、HTTP ステータスで検証する。
- DB スキーマや Flyway migration の変更は不要。
