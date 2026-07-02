# いいね機能 TDD計画

## 概要

今回の機能は、投稿詳細画面から `POST /posts/{id}/likes` を実行し、
同一クライアントのいいねをトグルできるようにする。

クライアント識別子は、IP アドレスと User-Agent を連結した値を SHA-256 で
ハッシュ化し、先頭 8 文字を `clientHash` として扱う。

実装は一気に進めず、永続化、Service のトグル、Controller、詳細画面表示に
分けて進める。

## インクリメント分割

1. Like の永続化モデルを作る
   - 投稿ごとに `clientHash` 単位でいいねを 1 件だけ保存できる。
   - 同一投稿・同一 `clientHash` の重複を DB 制約で防ぐ。
2. いいねトグルの Service を作る
   - 未いいねなら追加する。
   - 既いいねなら削除する。
   - 投稿ごとの現在のいいね数を取得できる。
3. `POST /posts/{id}/likes` を作る
   - IP アドレスと User-Agent から `clientHash` を生成する。
   - Service のトグル処理を呼ぶ。
   - 処理後は投稿詳細へリダイレクトする。
4. 詳細画面にいいね数と Like ボタンを表示する
   - 詳細画面で現在のいいね数を表示する。
   - Like ボタンを表示する。
   - フォームには CSRF トークンを含める。

## 詳細インクリメント分割

テストを小さく追加しやすいように、いいね機能を以下の 15 インクリメントに細分化する。

1. `PostLike` Entity と `PostLikeRepository` で Like を保存できる。
2. 同一 `post_id` + `client_hash` の重複保存を DB 制約で防ぐ。
3. 投稿 id と `clientHash` で Like の存在確認ができる。
4. 投稿 id ごとの Like 件数を取得できる。
5. Service で未いいねの `clientHash` を追加できる。
6. Service で既いいねの `clientHash` を削除できる。
7. Service で存在しない投稿 id を指定したときに 404 相当の例外にできる。
8. IP アドレスと User-Agent から SHA-256 先頭 8 文字の `clientHash` を生成できる。
9. `POST /posts/{id}/likes` が Service のトグル処理を呼ぶ。
10. `POST /posts/{id}/likes` 成功後に `/posts/{id}` へリダイレクトする。
11. User-Agent が空または未指定でも `clientHash` を安定して生成できる。
12. `GET /posts/{id}` の model に `likeCount` を積める。
13. 詳細画面にいいね数を表示できる。
14. 詳細画面に Like ボタンを表示し、`POST /posts/{id}/likes` へ送信できる。
15. 同一クライアントが Like ボタンを 2 回押すと、追加後に解除されることを Controller 経由で確認できる。

## 最初のステップ: Like の永続化モデル

最初のインクリメントは、Like を DB に保存し、投稿ごとの件数や
同一クライアントの存在確認ができる状態にすることに限定する。

Controller、画面、IP アドレスと User-Agent からの `clientHash` 生成は、
このインクリメントには含めない。

## Red 要件

- `post_likes` 相当のテーブルを追加する。
- 1 件の Like は `post_id` と `client_hash` を持つ。
- `client_hash` は SHA-256 先頭 8 文字を保存する前提で、最大 8 文字とする。
- 同一 `post_id` + `client_hash` は重複登録できない。
- 投稿ごとのいいね数を取得できる。
- 投稿と `clientHash` の組み合わせで Like の存在確認ができる。

## Red TODO

最初に Repository テストを 1 本だけ追加する。

テスト名:

`いいね保存_同一投稿とclientHash_1件取得できる`

| 観点 | 内容 |
|---|---|
| Given | 投稿を 1 件保存し、`clientHash = "a1b2c3d4"` の Like を保存する |
| When | 投稿 id と `clientHash` で Like を検索する |
| Then | 保存した Like が 1 件取得できる |

このテストが Red になる理由:

- `PostLike` Entity がまだ存在しない。
- `PostLikeRepository` がまだ存在しない。
- `post_likes` 用の Flyway migration がまだ存在しない。

## 後続 TODO

最初の Red を Green にした後、同じインクリメント内で次のテストを追加する。

- `いいね保存_同一投稿とclientHash重複_DB制約で失敗する`
- `いいね検索_同一投稿とclientHashが存在する_TRUEを返す`
- `いいね数取得_投稿ごとの件数を返す`

## 追加 Red TODO

以下は、詳細インクリメント分割のうち 5, 6, 7, 12, 13, 14, 15 に対応する
Red TODO とする。

**5. Service: 未いいねなら追加する**

テスト名: `いいねトグル_未いいね_Likeを保存する`

| 観点 | 内容 |
|---|---|
| Given | `postRepository.findById(1L)` が投稿を返し、`postLikeRepository.findByPostIdAndClientHash(1L, "a1b2c3d4")` が空を返す |
| When | `postService.toggleLike(1L, "a1b2c3d4")` を呼ぶ |
| Then | `PostLike(post, "a1b2c3d4")` が保存される |
| 戻り値 | 投稿 id `1` の最新いいね数を返す |

このテストが Red になる理由:

- `PostService#toggleLike(Long id, String clientHash)` がまだ存在しない。
- `PostService` が `PostLikeRepository` に依存していない。
- `PostLike` Entity がまだ存在しない。

**6. Service: 既いいねなら削除する**

テスト名: `いいねトグル_既いいね_Likeを削除する`

| 観点 | 内容 |
|---|---|
| Given | `postRepository.findById(1L)` が投稿を返し、同一 `clientHash` の `PostLike` が存在する |
| When | `postService.toggleLike(1L, "a1b2c3d4")` を呼ぶ |
| Then | 既存の `PostLike` が削除される |
| 保存 | 新しい `PostLike` は保存されない |
| 戻り値 | 投稿 id `1` の最新いいね数を返す |

このテストが Red になる理由:

- 既存 Like の削除分岐がまだ存在しない。
- `PostLikeRepository#delete(PostLike like)` の呼び出しがまだ実装されていない。

**7. Service: 存在しない投稿 id は 404 相当にする**

テスト名: `いいねトグル_存在しない投稿id_ResponseStatusExceptionを投げる`

| 観点 | 内容 |
|---|---|
| Given | `postRepository.findById(999L)` が `Optional.empty()` を返す |
| When | `postService.toggleLike(999L, "a1b2c3d4")` を呼ぶ |
| Then | `ResponseStatusException` が投げられる |
| HTTP 相当 | status は `HttpStatus.NOT_FOUND` |
| Like Repository | 保存・削除・件数取得は呼ばれない |

このテストが Red になる理由:

- `toggleLike` の投稿存在チェックがまだ存在しない。
- 存在しない投稿 id の例外方針がまだ実装されていない。

**12. Controller: 詳細表示の model にいいね数を積む**

テスト名: `投稿詳細_存在するid_likeCountをmodelに渡す`

| 観点 | 内容 |
|---|---|
| Given | `postService.findById(1L)` が投稿を返し、`postService.countLikes(1L)` が `3` を返す |
| HTTP | `GET /posts/1` が 200 を返す |
| View | `posts/detail` |
| Model | `likeCount` に `3` を渡す |

このテストが Red になる理由:

- `PostService#countLikes(Long postId)` がまだ存在しない。
- `PostController#detail` が `likeCount` を model に積んでいない。

**13. Template: 詳細画面にいいね数を表示する**

テスト名: `投稿詳細_いいね数_詳細画面に表示する`

| 観点 | 内容 |
|---|---|
| Given | `postService.findById(1L)` が投稿を返し、`postService.countLikes(1L)` が `3` を返す |
| HTTP | `GET /posts/1` が 200 を返す |
| HTML | `いいね 3` を表示する |
| XSS | 表示は `th:text` または Thymeleaf の通常エスケープされる式で行う |

このテストが Red になる理由:

- `posts/detail.html` にいいね数表示がまだ存在しない。

レイアウト要件:

- いいね数と Like ボタンは投稿本文の直下、作成日の上に表示する。
- 詳細ページの `<article class="post">` 内で、表示順は投稿者名、本文、いいねボタン、作成日とする。
- いいね数は Like ボタン内に表示する。
- 表示文言は `3 いいね` の形式にする。
- 数値は model の `likeCount` を使い、固定値を HTML に埋め込まない。
- モバイル幅でも折り返して本文や作成日に重ならないようにする。
- 詳細なデザイン要件は `ai-docs/like-button-layout.md` を参照する。

**14. Template: 詳細画面に Like ボタンを表示する**

テスト名: `投稿詳細_Likeボタン_posts_id_likesへPOSTするフォームを表示する`

| 観点 | 内容 |
|---|---|
| Given | `postService.findById(1L)` が投稿を返す |
| HTTP | `GET /posts/1` が 200 を返す |
| Form | `action="/posts/1/likes"`、`method="post"` のフォームを表示する |
| Button | `Like` ボタンを表示する |
| CSRF | フォーム内に `_csrf` hidden input を含める |

このテストが Red になる理由:

- `posts/detail.html` に Like 用フォームがまだ存在しない。

レイアウト要件:

- Like ボタンは作成日の上に表示する。
- Like ボタン内に、ハートアイコン、いいね数、`いいね` テキストを横並びで表示する。
- Like ボタン内の要素間隔は CSS の `gap` で管理する。
- `.post__like-form` の余白は `margin: 0` とし、投稿カード内の縦方向のリズムを崩さない。
- ボタンは完全なカプセル型にする。
- フォームは詳細ページ内の投稿カードに収め、独立したカードや大きな説明文は追加しない。

**15. Controller: 同一クライアントの 2 回押しで解除される**

テスト名: `いいねトグル_同一クライアント2回押下_追加後に解除する`

| 観点 | 内容 |
|---|---|
| Given | IP アドレスと User-Agent が同じリクエストを 2 回送る |
| 1 回目 | `POST /posts/1/likes` で Like が追加される |
| 2 回目 | `POST /posts/1/likes` で同じ `clientHash` の Like が削除される |
| HTTP | どちらも `/posts/1` へリダイレクトする |
| 検証 | `postService.toggleLike(1L, 同じclientHash)` が 2 回呼ばれる |

このテストが Red になる理由:

- `POST /posts/{id}/likes` がまだ存在しない。
- IP アドレスと User-Agent から同じ `clientHash` を生成して Service に渡す処理がまだ存在しない。

## 想定する実装対象

- `src/main/java/com/example/tsubuyaki/domain/PostLike.java`
- `src/main/java/com/example/tsubuyaki/repository/PostLikeRepository.java`
- `src/main/resources/db/migration/V...__create_post_likes.sql`
- `src/test/java/com/example/tsubuyaki/repository/PostLikeRepositoryTest.java`

実際のパッケージ名と配置は、既存の `Post` Entity、`PostRepository`、
Repository テストの構成を確認してから合わせる。

## テスト計画

最初は対象 Repository テスト 1 本だけを実行する。

```bash
./mvnw -B -Ph2 -Dtest=PostLikeRepositoryTest#いいね保存_同一投稿とclientHash_1件取得できる test
```

Repository の永続化モデルが Green になった後、Repository テスト全体を実行する。

```bash
./mvnw -B -Ph2 -Dtest=PostLikeRepositoryTest test
```

インクリメント完了時に H2 プロファイルで verify を実行する。

```bash
./mvnw -B -Ph2 verify
```

## 前提

- この計画書作成では、実装コードやテストコードは変更しない。
- 投稿削除時の Like 削除方針は、既存の投稿削除仕様を確認してから決める。
- 詳細画面の Like ボタン表示、Controller、`clientHash` 生成は後続インクリメントで扱う。
- Thymeleaf ではユーザー入力由来の値に `th:text` を使い、`th:utext` は使わない。
- Spring Security の CSRF は有効のまま進める。
