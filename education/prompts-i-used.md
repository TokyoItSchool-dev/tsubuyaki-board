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

## プロンプト 2

**フェーズ**:M3対応

**プロンプト本文**:

```
投稿登録機能を作成する。
    - 入力項目は PostForm の author, body を使う
    - 投稿者 author は必須、30文字まで、空白のみはNG
    - 本文 body は必須、280文字まで、空白のみはNG
    - 登録後、投稿一覧画面で登録した投稿が見えること

    以下の観点のテストを用意してから本機能の実装を行う。
    - GET /posts/new で postForm が model に積まれ、posts/form.html を表示できること
    - author が空文字または空白のみの場合、登録せず posts/form を再表示しエラーを表示すること
    - author が31文字以上の場合、登録せず posts/form を再表示しエラーを表示すること
    - body が空文字または空白のみの場合、登録せず posts/form を再表示しエラーを表示すること
    - body が281文字以上の場合、登録せず posts/form を再表示しエラーを表示すること
    - 入力が正常な場合、PostService が投稿者・本文・投稿日を保存すること
    - 入力が正常な場合、POST /posts 後に /posts へリダイレクトすること
    - 登録後、GET /posts で登録した投稿が一覧に表示されること
```

**結果**: 効いた
```
M3 投稿登録機能を実装しました。

  差分:

  - 成功時は PostService.create(...) で保存し、redirect:/posts
  - src/main/java/com/example/tsubuyaki/service/PostService.java:33 で createdAt = Instant.now() を設定して
    保存

      - PostServiceTest
      - PostCreateFlowTest

  検証:

  - ./mvnw -B -Ph2 -Dtest=PostControllerTest,PostServiceTest,PostCreateFlowTest test 成功、16件
  - ./mvnw -B -Ph2 verify 成功、22件
  - Checkstyle 0、SpotBugs 0、JaCoCo OK
```

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
