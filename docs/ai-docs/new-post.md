# 投稿作成フォーム GET /posts/new TDD計画

## 概要

今回のインクリメントは、投稿作成フォームを表示する `GET /posts/new` に限定する。
このエンドポイントは `posts/form.html` を表示し、フォームバインド用の `postForm`
として `PostForm` を model に積む。

現状確認では、既存コードに `GET /posts/new`、`PostForm`、`posts/form.html`、
Controller テストが存在し、対象テストは Green だった。

## インクリメント分割

1. 投稿作成フォーム表示: `GET /posts/new` で空フォームを表示する
2. 投稿作成バリデーション: 空欄・文字数超過時にフォームを再表示する
3. 投稿登録成功: 正常入力時に保存して一覧へリダイレクトする
4. 一覧との導線: 一覧から新規投稿へ移動できる

## 最初のステップ: Red 要件

- `GET /posts/new` は HTTP 200 を返す。
- View 名は `posts/form`。
- Model に `postForm` が存在する。
- `postForm` は `PostForm` 型で、初期状態の空フォームとして使える。
- Service の投稿一覧取得や保存は呼ばない。

## Red TODO

- `PostControllerTest` に
  `投稿作成フォーム_GET_posts_new_PostFormをmodelに積みformビューを返す`
  を追加、または既存テストを具体化する。
- MockMvc で `get("/posts/new")` を実行する。
- `status().isOk()` を検証する。
- `view().name("posts/form")` を検証する。
- `model().attribute("postForm", instanceOf(PostForm.class))` を検証する。
- `postService.latest()` が呼ばれないことを検証する。

## テスト計画

最初は対象 Controller テスト 1 本だけを実行する。

```bash
./mvnw -B -Ph2 -Dtest=PostControllerTest#投稿作成フォーム_GetPostsNew_空のフォームを表示する test
```

Green 後に Controller テスト全体を実行する。

```bash
./mvnw -B -Ph2 -Dtest=PostControllerTest test
```

## 前提

- 今回は POST 登録処理、DB 保存、入力バリデーション表示の実装は含めない。
- `posts/form.html` は既存テンプレートを使う。
- `PostForm` は既存の `com.example.tsubuyaki.web.dto.PostForm` を使う。
- 実装コードやテストコードは、この計画書作成作業では変更しない。
