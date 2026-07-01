# 投稿登録 POST /posts TDD計画

## 概要

今回のユースケースは、投稿作成フォームから `POST /posts` へ送信された入力を検証し、
妥当な場合は投稿を保存して一覧へリダイレクトする投稿登録機能に限定する。

既存コードには `PostForm`、`GET /posts/new`、`posts/form.html` が存在する。
最初のインクリメントでは Controller 層の HTTP 挙動を固定し、Service の保存処理は
次のインクリメントで実装する。

## インクリメント分割

1. Controller の成功/失敗フロー: `POST /posts` のリダイレクトとフォーム再表示を固定する
2. Service の保存処理: `PostService` が `Post` を作成して Repository に保存する
3. Repository/結合確認: H2 で保存後に一覧へ出ることを確認する

## 最初のステップ: Red 要件

- `POST /posts` に妥当な `author` と `body` が送られたら、
  `postService.create(author, body)` を呼ぶ。
- 成功時は HTTP 302 で `/posts` にリダイレクトする。
- バリデーション失敗時は HTTP 200 で `posts/form` を再表示する。
- バリデーション失敗時は Service を呼ばない。
- フォーム再表示時は `postForm` の field error が model に残り、
  既存の `th:errors` で表示できる状態にする。

## エラー文言

| フィールド | 条件 | 文言 |
|---|---|---|
| `author` | 未入力・空白のみ | 投稿者名を入力してください |
| `author` | 31 文字以上 | 投稿者名は 30 文字以内で入力してください |
| `body` | 未入力・空白のみ | 本文を入力してください |
| `body` | 281 文字以上 | 本文は 280 文字以内で入力してください |

## バリデーション表示スタイル

`posts/form.html` の `th:errors` で表示するエラー文言は、既存の `.error` クラスで
以下のスタイルを適用する。

| プロパティ | 値 |
|---|---|
| `color` | `#e11d48` |
| `margin-top` | `1px` |
| `font-size` | `0.85rem` |

## Red TODO

`PostControllerTest` に以下の失敗テストを追加する。

**1. 正常系: 投稿を作成して一覧へリダイレクトする**

テスト名: `投稿登録_妥当な入力_投稿を作成して一覧へリダイレクトする`

| 観点 | 内容 |
|---|---|
| 入力 | `author = "alice"`、`body = "hello"` |
| HTTP | 302 |
| 遷移先 | `/posts` へリダイレクト |
| Service | `postService.create("alice", "hello")` が呼ばれる |

**2. 異常系: 投稿者名が空白のみ**

テスト名: `投稿登録_author空白のみ_フォームを再表示し投稿者名必須エラーを表示する`

| 観点 | 内容 |
|---|---|
| 入力 | `author = "   "` |
| HTTP | 200 |
| View | `posts/form` |
| Model | `postForm.author` に field error がある |
| エラー文言 | `投稿者名を入力してください` |
| Service | 呼ばれない |

**3. 異常系: 投稿者名が31文字**

テスト名: `投稿登録_author31文字_フォームを再表示し投稿者名文字数エラーを表示する`

| 観点 | 内容 |
|---|---|
| 入力 | 31 文字の `author` |
| HTTP | 200 |
| View | `posts/form` |
| Model | `postForm.author` に field error がある |
| エラー文言 | `投稿者名は 30 文字以内で入力してください` |
| Service | 呼ばれない |

**4. 異常系: 本文が空白のみ**

テスト名: `投稿登録_body空白のみ_フォームを再表示し本文必須エラーを表示する`

| 観点 | 内容 |
|---|---|
| 入力 | `body = "   "` |
| HTTP | 200 |
| View | `posts/form` |
| Model | `postForm.body` に field error がある |
| エラー文言 | `本文を入力してください` |
| Service | 呼ばれない |

**5. 異常系: 本文が281文字**

テスト名: `投稿登録_body281文字_フォームを再表示し本文文字数エラーを表示する`

| 観点 | 内容 |
|---|---|
| 入力 | 281 文字の `body` |
| HTTP | 200 |
| View | `posts/form` |
| Model | `postForm.body` に field error がある |
| エラー文言 | `本文は 280 文字以内で入力してください` |
| Service | 呼ばれない |

## テスト計画

最初は対象 Controller テスト 1 本だけを実行し、Red を確認する。

```bash
./mvnw -B -Ph2 -Dtest=PostControllerTest#投稿登録_妥当な入力_投稿を作成して一覧へリダイレクトする test
```

Controller の Green 後に Controller テスト全体を実行する。

```bash
./mvnw -B -Ph2 -Dtest=PostControllerTest test
```

Service の保存処理を実装した後、Service テストを実行する。

```bash
./mvnw -B -Ph2 -Dtest=PostServiceTest test
```

ユースケース完了時に H2 プロファイルで verify を実行する。

```bash
./mvnw -B -Ph2 verify
```

## 前提

- 入力値はこのインクリメントでは trim して保存しない。
- 空白のみ NG は `@NotBlank` で扱う。
- Controller は `@Valid PostForm` と `BindingResult` を使う。
- Service は `create(String author, String body)` を追加する。
- `posts/form.html` は既存テンプレートを使い、エラー表示は既存の `th:errors` を使う。
