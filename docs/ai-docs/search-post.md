# キーワード検索機能 GET /posts?q=xxx TDD計画

## 概要

`GET /posts?q=xxx` で投稿本文を部分一致検索し、既存の一覧画面 `posts/list` を再利用する。

検索ボックスは一覧画面上部に追加する。検索結果も一覧画面の投稿カード表示をそのまま使う。

## 全体インクリメント分割

1. Service / Repository: 本文検索の取得経路を追加する
2. Controller: `GET /posts?q=xxx` を受け取り、検索結果を一覧画面へ渡す
3. Frontend: 一覧画面上部に検索ボックスを追加する
4. 結合確認: 検索あり、検索なし、検索結果0件の表示を確認する

## インクリメント 1: Service / Repository の本文検索

### 要件

- `q` が指定された場合、投稿本文 `body` に `q` を含む投稿だけを取得する。
- 検索対象は本文 `body` のみとし、投稿者名 `author` は検索対象外とする。
- 検索結果は `createdAt` 降順で最大50件にする。
- `q` が `null`、空文字、空白のみの場合は、既存の最新50件取得と同じ結果を返す。
- Controller や画面はこのインクリメントでは変更しない。

### Red TODO

- `PostServiceTest` に `投稿検索_qあり_本文検索Repositoryを呼び結果を返す` を追加する。
- `PostServiceTest` に `投稿検索_q空白_latestを返す` を追加する。
- `PostRepositoryTest` に `投稿検索_本文にキーワードを含む投稿だけ新着順で最大50件返す` を追加する。
- Red 確認として、追加直後に対象テストがコンパイルエラーまたは失敗することを確認する。

### Green 方針

- `PostRepository` に本文部分一致検索用メソッドを追加する。
- `PostService#list(String q)` を追加する。
- `q` が空の場合は `latest()` と同じ Repository メソッドを呼ぶ。
- `q` が値ありの場合は本文検索 Repository メソッドを呼ぶ。

### 確認コマンド

```bash
./mvnw -B -Ph2 -Dtest=PostServiceTest test
./mvnw -B -Ph2 -Dtest=PostRepositoryTest test
./mvnw -B -Ph2 verify
```

## インクリメント 2: Controller の検索パラメータ対応

### 要件

- `GET /posts?q=xxx` を受け取れる。
- `q` が指定された場合、`PostService#list(q)` の結果を model の `posts` に積む。
- model に `q` も積み、画面側で検索文字列を保持できるようにする。
- View 名は既存と同じ `posts/list` とする。

### Red TODO

- `PostControllerTest` に `投稿一覧_qあり_検索結果をposts_listへ渡す` を追加する。
- `PostControllerTest` に `投稿一覧_qあり_qをmodelに保持する` を追加する。
- `PostControllerTest` に `投稿一覧_qなし_latest相当の一覧を表示する` を追加する。

## インクリメント 3: 一覧画面の検索ボックス

### 要件

- 一覧画面上部に検索フォームを表示する。
- フォームは `GET /posts` に送信する。
- 入力欄の `name` は `q` とする。
- 検索後も入力欄に検索キーワードを表示する。
- 既存の投稿一覧表示、詳細リンク、更新ボタンは維持する。

### Red TODO

- `PostControllerTest` に `投稿一覧_検索フォーム_GET_postsへqを送信できる` を追加する。
- `PostControllerTest` に `投稿一覧_検索後_入力欄にqを保持する` を追加する。

## インクリメント 4: 結合確認

### 要件

- `GET /posts?q=hello` で本文に `hello` を含む投稿だけが表示される。
- `GET /posts` は従来どおり最新50件を表示する。
- `GET /posts?q=` は従来どおり最新50件を表示する。
- 検索結果が0件の場合は既存の空メッセージを表示する。

### 確認コマンド

```bash
./mvnw -B -Ph2 verify
```

## 前提

- LIKE 検索は通常の部分一致検索とし、大文字小文字の特別な正規化はしない。
- 検索結果0件時の文言は、既存の「まだ投稿はありません」を流用する。
- URL は `/posts?q=xxx` を基本とし、既存の `/posts` と `/posts/` も維持する。
