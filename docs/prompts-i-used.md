# 実行したプロンプトまとめ

各機能実装ごとに使ったプロンプトを残す。

## M1投稿一覧画面
```
GET /posts/new の投稿作成画面を TDD で実装してください。
@education/EXERCISES.md の M2 を受入基準とし、
まず @WebMvcTest + MockMvc で失敗するテストを 1 本書いてください。
Thymeleaf では th:utext を使わず th:text を使ってください。
完了条件は ./mvnw -B -Ph2 test が緑になることです。
```


## M2投稿作成フォーム
```
GET /posts/new の投稿作成画面を TDD で実装してください。
@education/EXERCISES.md の M2 を受入基準とし、
まず @WebMvcTest + MockMvc で失敗するテストを 1 本書いてください。
Thymeleaf では th:utext を使わず th:text を使ってください。
完了条件は ./mvnw -B -Ph2 test が緑になることです。
```

## M3投稿登録フォーム

```md
POST /posts の投稿登録機能を TDD で実装してください。
@education/EXERCISES.md の M3 を受入基準とし、
まず @WebMvcTest + MockMvc で失敗するテストを 1 本書いてください。
Thymeleaf では th:utext を使わず th:text を使ってください。
完了条件は ./mvnw -B -Ph2 test が緑になることです。
```


## M4投稿詳細

```md
GET /posts/{id} の投稿詳細画面を TDD で実装してください。
@education/EXERCISES.md の M4 を受入基準とし、
まず @WebMvcTest + MockMvc で失敗するテストを 1 本書いてください。
Thymeleaf では th:utext を使わず th:text を使ってください。
完了条件は ./mvnw -B -Ph2 test が緑になることです。
```



## M5ヘルスチェック（回帰確認）

**指示内容**
```md
`/actuator/health` が UP を返すことを確認してください。
@education/EXERCISES.md の M5 を受入基準としてください。
完了条件は ./mvnw -B -Ph2 test が緑になることです。
```




## S1いいね機能

Plan
```
/plan /posts/{id}/likes のいいね機能を TDD で実装する計画を立ててください。
@education/EXERCISES.md の S1 を受入基準とします。
```

```md
POST /posts/{id}/likes のいいね機能を TDD で実装してください。
@education/EXERCISES.md の S1 を受入基準とし、
まず @WebMvcTest + MockMvc で失敗するテストを 1 本書いてください。
Thymeleaf では th:utext を使わず th:text を使ってください。
完了条件は ./mvnw -B -Ph2 test が緑になることです。
```



## S2 キーワード検索機能


```Plan
/plan GET /posts?q=xxx の本文LIKE検索機能を TDD で実装する計画を立ててください。
@education/EXERCISES.md の S2 を受入基準とします。
```


```実装指示
› このplanに沿ってTDD で実装してください。
また、 .codex/prompts/review.mdに記載の観点でコードレビューしてください。
完了条件は ./mvnw -B -Ph2 test が緑になることです。
```



## S3投稿者名フィールド拡張

```Plan
/plan TDD で実装する計画を立ててください。
@education/EXERCISES.md の S3 を受入基準とします。
```

そのままだと、PostsテーブルにUserのアバター色が列追加されてしまった。
あと既存の色が明るすぎる。そのため修正指示。


```修正指示
既存投稿・seed データの色は グレー にする。
アバター色の選択は任意とする。選択されていないまま投稿保存した場合はデフォルト色が適用される。
PostsテーブルにUserのアバター色を持たせるのではなく、PostsにUserIdをもたせ、Userマスタテーブルにアバター色を持たせる実装に変更する。
```

指示が有効だった。
ModelにUserエンティティが定義され、PostsにはUserマスタの参照キーが付与された。
データモデルを正規化できた。

```実装指示
› このplanに沿ってTDD で実装してください。
また、 .codex/prompts/review.mdに記載の観点でコードレビューしてください。
完了条件は ./mvnw -B -Ph2 test が緑になることです。
```

```微調整
/plan 投稿一覧画面と投稿詳細画面にアバター色を反映したい。
投稿者名の左側にアバター色の円形の図形を表示させたい。サイズは投稿者名とそろえること。
```

```コードレビュー依頼
/plan 投稿一覧画面と投稿詳細画面にアバター色を反映したい。
投稿者名の左側にアバター色の円形の図形を表示させたい。サイズは投稿者名とそろえること。
```



