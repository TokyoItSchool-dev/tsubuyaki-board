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

**フェーズ**:

**プロンプト本文**:

```
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

```
• 実装完了しました。

  変更内容:

  - GET /posts / GET /posts/ で投稿一覧を表示
  - created_at DESC の新着順で最大 50 件取得
  - model.posts に List<Post> を設定
  - 0 件時に「まだ投稿はありません」を表示
  - 更新ボタンを追加し、GET /posts/ へリロード
  - 表示順を「投稿者 → 内容 → 投稿日」に変更
  - 本文の折り返しを CSS で強化
  - Repository / Service / Controller / DTO のテストを追加
  - 既存の SamplePostServiceTest を実装後の挙動に合わせて更新

  検証:

  $env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot'
  $env:MAVEN_OPTS='-Dmaven.repo.local=C:\home\codex\.m2\repository'
  .\mvnw.cmd -B -Ph2 verify

  結果: BUILD SUCCESS、テスト 12 件成功、Checkstyle 違反 0、SpotBugs 0、JaCoCo も All coverage checks have been met. です。
```

**振り返り**:

更新ボタンのテストでURLを「posts/」と指定してしまったために、404エラーが正になってしまった(正しくはposts)。もともと要件にない機能を突然実装しようとすると失敗しやすい。
実際にH2で起動するとシーダーにより、10件分表示された。
→エラーになることを伝えて修正OK

---

## プロンプト 3

**フェーズ**:

**プロンプト本文**:

```
投稿機能を作成する。
- posts/form.html を表示
- フォームバインド用に postForm (PostForm) を model に積む

以下の観点のテストを用意する。
- テストデータを入力して投稿し、投稿一覧の一番上に投稿した内容が表示されること

```

**結果**: 部分的に効いた

```
• 投稿機能を実装しました。

  主な変更:

  - src/main/java/com/example/tsubuyaki/controller/PostController.java: GET /posts/new のフォーム表示に加えて、POST /posts で入力検証、保存、/posts へのリダイ
    レクトを実装

  - src/main/java/com/example/tsubuyaki/service/PostService.java: create(author, body) で Post を保存
  - src/test/java/com/example/tsubuyaki/controller/PostControllerTest.java: postForm の model 設定、正常投稿時の保存とリダイレクトを検証
  - src/test/java/com/example/tsubuyaki/PostPostingIntegrationTest.java: テストデータを投稿し、一覧の一番上に表示されることを H2 + MockMvc で検証

  検証済み:

  .\mvnw.cmd -B -Ph2 verify

  結果: BUILD SUCCESS、テスト 16 件成功、Checkstyle 違反 0、SpotBugs 0、JaCoCo も条件達成です。
```

**振り返り**:

投稿画面は実装できたが、投稿すると405エラーになってしまう。
→エラーになることを伝えて修正OK


---

## プロンプト 4

**フェーズ**:

**プロンプト本文**:

```
投稿内容詳細(1ポストのみを画面表示する)機能を追加する。
- posts/detail.html を表示
- 存在しない id は 404
- URLがposts/{id}となる
```

**結果**: 効いた

```
 投稿詳細機能を追加しました。

  変更内容:

  - src/main/java/com/example/tsubuyaki/controller/PostController.java: GET /posts/{id} を追加。存在しない id は 404
  - src/main/java/com/example/tsubuyaki/service/PostService.java: findById(Long id) を追加
  - src/main/resources/templates/posts/detail.html: 投稿者・本文・投稿日時を表示する詳細画面を追加
  - src/test/java/com/example/tsubuyaki/controller/PostControllerTest.java: 詳細表示と 404 のテストを追加
  - src/test/java/com/example/tsubuyaki/service/PostServiceTest.java: findById のテストを追加

  検証済み:

  .\mvnw.cmd -B -Ph2 verify

  結果: BUILD SUCCESS、テスト 19 件成功、Checkstyle / SpotBugs / JaCoCo も通過です。

  ブラウザ確認時は、以前の 405 と同じく起動済みアプリが古い場合があるため、Spring Boot を再起動してから http://localhost:8080/posts/1
  のようにアクセスしてください。
```

**振り返り**:

実装はできたが、URL直打ちする必要がある。
画面上のポストをクリックして、内容詳細に入る機能を別途追加する。


---

## プロンプト 5

**フェーズ**:

**プロンプト本文**:

```
一覧画面でポストをクリックすると、内容詳細に入れるようにする。
```

**結果**: 部分的に効いた

```
• 一覧画面の各ポストをクリックして詳細へ遷移できるようにしました。

  変更内容:

  - src/main/resources/templates/posts/list.html: 各投稿を href="/posts/{id}" のリンクで包むよう変更
  - src/main/resources/static/css/app.css: 投稿リンクの見た目を通常カード風に維持し、hover/focus 時に枠色が変わるよう追加
  - src/test/java/com/example/tsubuyaki/controller/PostControllerTest.java: 一覧に /posts/{id} へのリンクが出るテストを追加

  検証済み:

  .\mvnw.cmd -B -Ph2 verify

  結果: BUILD SUCCESS、テスト 20 件成功、Checkstyle / SpotBugs / JaCoCo も通過です。
```

**振り返り**:

ポストの文字列がそのままリンクになった。ちょっと思ったのと違う。本当は、ポストの枠全体がカードになっていて、カード(div要素)のクリックで画面遷移してほしかった。

---

## プロンプト 7

**フェーズ**:

**プロンプト本文**:

```
ポストに「いいね機能」を追加する。
- POST /posts/{id}/likes で いいねトグル
- 同一 clientHash (= ハッシュ化された IP + UA、SHA-256 の先頭 8 文字) が再度押したら解除
- 詳細画面でいいね数と Like ボタン表示
- Likeボタンは👍️で表示
- Likeボタンを押したら、ボタンをキラキラさせる
- いいね数は新しいテーブルを追加して管理する

```

**結果**: 部分的に効いた

```
• いいね機能を追加しました。

  実装内容:

  - V2__likes.sql で post_likes テーブルと post_likes_seq を追加
  - PostLike Entity と PostLikeRepository を追加
  - POST /posts/{id}/likes でいいねトグル
  - 同一 clientHash は再押下で解除
  - clientHash は IP + User-Agent を SHA-256 化し、先頭 8 文字を使用
  - 詳細画面にいいね数と ❤️ ボタンを表示
  - Like ボタン押下時に CSS の :active アニメーションでキラキラするよう追加
  - 存在しない投稿への Like は 404

  テスト:

  - Repository: post_likes の保存・件数・検索
  - Service: 追加、解除、件数取得
  - Controller: 詳細画面表示、Like ボタン、clientHash、リダイレクト、404

  検証済み:

  .\mvnw.cmd -B -Ph2 verify

  結果: BUILD SUCCESS、テスト 27 件成功、Checkstyle / SpotBugs / JaCoCo も通過です。
```

**振り返り**:

機能は正しく動いているが、なぜか👍️ではなく❤で実装した。
❤はキラキラしてくれない。

---

## プロンプト 8

**フェーズ**:

**プロンプト本文**:

```
ここまでの変更を、機能毎に順番にコミットする。
```

**結果**: 部分的に効いた

```
同じファイルに複数の変更があったため、順番に切り出しながらコミットした。
```

**振り返り**:

1つのファイルの変更を順番にコミットするのはファイル操作を伴うため危険。
1つの変更のたびに毎回きっちりコミットするべき。


---

## プロンプト 9

**フェーズ**:

**プロンプト本文**:

```

```

**結果**: 部分的に効いた

```

```

**振り返り**:


---

## プロンプト 10

**フェーズ**:

**プロンプト本文**:

```

```

**結果**: 部分的に効いた

```

```

**振り返り**:


---

## プロンプト 11

**フェーズ**:

**プロンプト本文**:

```

```

**結果**: 部分的に効いた

```

```

**振り返り**:


---

## プロンプト 12

**フェーズ**:

**プロンプト本文**:

```

```

**結果**: 部分的に効いた

```

```

**振り返り**:


---
