# 詳細リンク インクリメント要件

## 概要

今回のインクリメントは、投稿一覧ページの各投稿から詳細ページへ遷移する導線を追加することに限定する。

一覧ページの `<article class="post">` 内に `詳細` リンクを表示し、投稿 id に対応する
`/posts/{id}` へ遷移できるようにする。

## 要件

- 一覧ページで投稿が表示されるとき、各 `<article class="post">` 内に詳細リンクを表示する。
- 詳細リンクの `href` は投稿 id を使って `/posts/{id}` にする。
- リンク文言は `詳細` とする。
- 詳細リンクは作成日と同じ行の右側、つまり article の右下に配置する。
- 既存の投稿者名、本文、作成日の表示は維持する。
- このインクリメントでは `GET /posts/{id}` の詳細ページ表示は実装対象外とする。

## Red TODO

`PostControllerTest` に以下の失敗テストを追加する。

**一覧: 各投稿に詳細リンクを表示する**

テスト名: `投稿一覧_投稿あり_各投稿に詳細リンクを表示する`

| 観点 | 内容 |
|---|---|
| 入力 | id が `1` の投稿を `postService.latest()` が返す |
| HTTP | `GET /posts` が 200 を返す |
| HTML | `<article class="post">` 内に `href="/posts/1"` がある |
| 文言 | リンク文言 `詳細` を表示する |

`Post` の id はテスト内で `ReflectionTestUtils.setField(post, "id", 1L)` を使って設定する。

## 実装方針

- `posts/list.html` の投稿ごとの `<article class="post">` 内に詳細リンクを追加する。
- リンクは `th:href="@{/posts/{id}(id=${post.id})}"` を使って生成する。
- 作成日と詳細リンクを同じ行に並べるため、`time` と `a` を同じメタ情報用コンテナに入れる。
- CSS ではメタ情報用コンテナを `display: flex` にし、作成日は左、詳細リンクは右に配置する。

## テスト計画

最初は対象 Controller テスト 1 本だけを実行し、Red を確認する。

```bash
./mvnw -B -Ph2 -Dtest=PostControllerTest#投稿一覧_投稿あり_各投稿に詳細リンクを表示する test
```

Green 後に一覧表示まわりの Controller テストを実行する。

```bash
./mvnw -B -Ph2 -Dtest=PostControllerTest#投稿一覧_投稿あり_投稿者内容投稿日の順に表示する+投稿一覧_投稿あり_各投稿に詳細リンクを表示する test
```

## 前提

- URL は `/post/{id}` ではなく、既存の一覧 URL と整合する `/posts/{id}` で統一する。
- リンク先の詳細ページが未実装で一時的に 404 になる状態は、次インクリメントで解消する。
- DB スキーマや Service、Repository の変更は不要。
