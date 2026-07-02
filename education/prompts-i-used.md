# 使ったプロンプトと評価

演習中に Codex CLI に投げた主要なプロンプトを記録する。
`EXERCISES.html` に記載の機能実装や、修正提案に関するプロンプトを記述する。

## 書き方

- どのフェーズで使ったか (例: 投稿一覧、いいね機能、リファクタ)
- 実際に投げたプロンプト本文 (省略せずコピペ)
- 結果: 効いた / 部分的に効いた / 効かなかった
- 振り返り: 次に同じ状況が来たらどう変えるか

---

## プロンプト 1(サンプル)

**フェーズ**: サンプルデータ(シーダー)の追加

**プロンプト本文**:

```
src\main\resources\db\migration\V1__init.sql に基づくH2用のシーダーを作成し、実行する。テストデータとして10個程度のシードデータを作成。H2で起動時に自動投入する。
```

**結果**: 効いた

```
• 提案どおり、H2 プロファイル起動時だけ seed が自動適用される構成にしました。

  差分:

  - src/main/resources/application-h2.yml:20: H2 の Flyway location に classpath:db/seed/h2 を追加。
  - src/main/resources/db/seed/h2/R__seed_posts_h2.sql:6: posts に 10 件投入する H2 専用 repeatable migration を追加。posts_seq は次
    ID が 11 になるよう更新。

  - src/test/resources/application-h2.yml:17: テストは空 DB 前提を維持するため seed location を外す設定を追加。
  - src/test/java/com/example/tsubuyaki/db/H2SeedMigrationTest.java:18: seed 適用で 10 件入ることと、次 sequence 値が 11 になることを
    検証。

  検証:

  - .\mvnw -B -Dmaven.repo.local=C:/home/codex/.m2/repository -Dtest=H2SeedMigrationTest test 成功
  - .\mvnw -B -Dmaven.repo.local=C:/home/codex/.m2/repository -Ph2 spring-boot:run "-Dspring-boot.run.arguments=--spring.main.web-
    application-type=none" 成功。ログで Migrating schema "PUBLIC" with repeatable migration "seed posts h2" を確認。

  - .\mvnw -B -Dmaven.repo.local=C:/home/codex/.m2/repository -Ph2 verify 成功。警告: Rule violated for bundle tsubuyaki-board: lines
    covered ratio is 0.48, but expected minimum is 0.60。日本語要約: 現在のカバレッジは 48% で 60% 未満ですが、このプロファイルでは
    fail 設定ではないためビルドは成功しています。
```

**振り返り**:

シーダーは追加できた。
検証方法を指定しなかったためハーネスであらかじめ定義された検証方法を自動的に起動した。
CI検証は成功しているが、実際に画面表示の確認はできていない(未実装のため)。

---








## プロンプト M1
**フェーズ**:
**プロンプト本文**:
```
◆M1最初の機能のスクリプト例
投稿一覧表示機能を作成する。
- 新着順 (created_at DESC) で最大 50 件を返す
- ビュー: posts/list.html
- 0 件なら「まだ投稿はありません」を表示
- model.posts 属性に List を積む
- 更新ボタンを設ける。押すとページをリロードする。
- 投稿者,内容,投稿日の順に表示する。
- 内容は、適度な文字数で折り返す

以下の観点のテストを用意してから本機能の実装を行う。
- 0件の場合に画面に「まだ投稿はありません」を表示できていること
- 51件以上の投稿がある場合に、新着50件しか表示されないこと
- 更新ボタンがあり、押すとposts/ にリクエストすること
- 投稿は「投稿者,内容,投稿日の順に表示できていること
```

**結果**: 効いた
提案どおり、投稿一覧が表示されるようにしました。

M1のテスト
① 投稿が0件の場合　		☑
② 投稿がある場合　		☑
③ 新着順になっているか　	☑
④ 更新ボタン　			☑
⑤ 50件制限　			☑　備考：ところてん方式　ID:５１が追加されたら、ID:１が消える。


**振り返り**:
　更新ボタンや新規投稿の投稿ボタンをおしたらエラーがでた。（未実装のため）
---



## プロンプト M2
**フェーズ**:
**プロンプト本文**:
```
◆M2「投稿作成フォーム (GET /posts/new)」をTDDで実装してください。

【機能要件】
- GET /posts/new
- posts/form.html を表示
- model に postForm (PostForm) を格納する
- M1の既存機能は壊さないこと

【実装手順】
1. RED：失敗するテストを作成
2. GREEN：最小限の実装でテストを通す
3. REFACTOR：重複除去・命名改善のみ行う

【テスト項目】
- GET /posts/new が HTTP200 を返すこと
- View名が posts/form であること
- model に postForm が格納されていること
- フォーム画面が表示されること
- author入力欄、body入力欄、送信ボタンがあること
- form の送信先が POST /posts であること

最後に `./mvnw -B -Ph2 test` を実行し、全テストが成功することを確認してください。
```

**結果**: 効いた
提案どおり、投稿作成フォームが表示されるようにしました。

M2：投稿フォーム（GET /posts/new）
① フォーム表示	☑
② 入力欄	☑
③ formの送信先	☑


**振り返り**:
---



## プロンプト M3
**フェーズ**:
**プロンプト本文**:
```
◆M3「投稿登録 (POST /posts)」をTDDで実装してください。

【機能要件】
- POST /posts
- author は 1～30文字、空白のみ不可
- body は 1～280文字、空白のみ不可
- バリデーション成功時は投稿を保存し、302で /posts にリダイレクトする
- バリデーション失敗時は posts/form を再表示し、入力値とエラーメッセージを表示する
- M1・M2の既存機能は壊さないこと

【実装手順】
1. RED：失敗するテストを作成
2. GREEN：最小限の実装でテストを通す
3. REFACTOR：重複除去・命名改善のみ行う

【テスト項目】
- 正常な入力で投稿が保存され、302で /posts にリダイレクトすること
- author が未入力・空白のみ・31文字以上でバリデーションエラーとなること
- body が未入力・空白のみ・281文字以上でバリデーションエラーとなること
- バリデーションエラー時に posts/form を表示すること
- 入力値とエラーメッセージが画面に表示されること

最後に `./mvnw -B -Ph2 test` を実行し、全テストが成功することを確認してください。
```

**結果**: 効いた
提案どおり、投稿作成フォームが表示されるようにしました。

M3：投稿登録（POST /posts）
① 正常登録　		☑
② author未入力　	☑　このフィールドに入力してください。　JS？
③ author31文字　	☑　そもそもフォームに３１文字以上入力できないようにした。
④ authorが空白のみ　	☑　「投稿者名を入力してください」
⑤ body未入力　		☑　このフィールドに入力してください。　JS？
⑥ body281文字　	☑　そもそもフォームに２８１文字以上入力できないようにしたのね。
⑦ body空白のみ　	☑　「本文を入力してください」
⑧ エラー時に入力ミスをする。　　□　不具合あり、仕様の可能性

**振り返り**:
---






## プロンプト 3

**フェーズ**:

**プロンプト本文**:

```

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**:

---

## プロンプト 4 以降

3 件目以降も追加可能。書ければ書くほど良い。
