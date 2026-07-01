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

**フェーズ**:M1: 投稿一覧

**プロンプト本文**:

```
投稿一覧表示機能（GET /posts ）を TDD で実装してください。

順序:
1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
2. その失敗テストを通す最小実装を書きます (Green)。
3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

受入基準:
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

制約:
- AGENTS.md と .codex/instructions.md を参照すること。
- このタスクのスコープ外のファイルは触らないこと。
- テストを書かずに本体コードを書き始めないこと。
- 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
- 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。
- 前提アセット: `Post` entity (`src/main/java/com/example/tsubuyaki/domain/Post.java`) と V1 Flyway マイグレーション (`src/main/resources/db/migration/V1__init.sql`) は既に提供済。M1 ではこの上に Repository / Service / Controller を実装する。

```


**結果**: 効いた
以下内容
commit f8057d5e1b4b3e6fff046d209e748be64abe3a8f
  Author: Codex <codex@example.com>
  Date:   Tue Jun 30 16:24:32 2026 +0900

      feat(post): 投稿一覧表示を実装

  差分概要:

  5 files changed, 77 insertions(+), 7 deletions(-)

  src/main/java/com/example/tsubuyaki/repository/PostRepository.java
  src/main/java/com/example/tsubuyaki/service/PostService.java
  src/main/resources/static/css/app.css
  src/main/resources/templates/posts/list.html
  src/test/java/com/example/tsubuyaki/controller/PostListFeatureTest.java

  主な内容:

  - PostRepository に findTop50ByOrderByCreatedAtDesc() を追加
  - PostService.latest() を空リスト返却から Repository 呼び出しへ変更
  - posts/list.html に更新ボタンと投稿者→内容→投稿日順の表示を追加
  - app.css に本文折り返し補強を追加
  - PostListFeatureTest を新規追加し、空表示・新着50件・更新ボタン・表示順を検証

**振り返り**:

投稿一覧表示機能は追加出来た。
ソースについては、posts/list.htmlのposts/list.html の空表示文言が仕様通りでは無かったため、
プロンプト3にて修正を実施した。
また、「順序」に記載の手順にて、TDDを用いた実装が走ることを確認した。

---

## プロンプト 3

**フェーズ**:M1: 投稿一覧：文言修正

**プロンプト本文**:

```
 posts/list.html について、０件時の「まだ投稿はありません。」以外の
  記載があるため、修正し、Conventional Commits でコミットしてくださ
  い。
```

**結果**: 効いた

```
以下変更結果
 確認:

  - ./mvnw -B -Ph2 -Dtest=PostListFeatureTest test 成功
  - コミット: 9652eb6 fix(post): 空の投稿一覧メッセージを修正

  ブランチ d-fujie はorigin/d-fujie より 2 コミット ahead です。
```

**振り返り**:

当該の文言修正が完了した。
また、テストについては、指示しなかったが、CodeXが判断し、テスト実施を行っていた。
今後のプロンプトでは、テスト実施を必ず盛り込む。

---

## プロンプト 4 

**フェーズ**:M2: 投稿作成フォーム

**プロンプト本文**:

```
投稿作成フォーム機能（GET /posts/new ）を TDD で実装してください。

順序:
1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
2. その失敗テストを通す最小実装を書きます (Green)。
3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

受入基準:
- posts/form.html を表示
- フォームバインド用に postForm (PostForm) を model に積む

以下の観点のテストを用意してから本機能の実装を行う。
- posts/new にリクエストした際に、返されるビュー名がposts/form.htmlであること
- modelに postForm というキーでオブジェクトが格納されていること
- modelに格納されている postForm オブジェクトの型が PostForm クラスであること。

制約:
- AGENTS.md と .codex/instructions.md を参照すること。
- このタスクのスコープ外のファイルは触らないこと。
- テストを書かずに本体コードを書き始めないこと。
- 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
- 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。
```

**結果**: 効いた

変更内容

  - src/test/java/com/example/tsubuyaki/controller/PostNewFormFeatureTest.java を追加
      - GET /posts/new が posts/form.html を返すこと
      - model に postForm があること
      - postForm が PostForm 型であることを確認

  - src/main/java/com/example/tsubuyaki/controller/PostController.java を更新
      - GET /posts/new の戻り値を posts/form.html に変更

  確認

  - Red: 追加テストが View name expected:<posts/form.html> but was:<posts/form> で失敗
  - Green: 最小実装後、追加テスト成功
  - 全体テスト: ./mvnw -B -Ph2 test 成功
      - Tests run: 7, Failures: 0, Errors: 0, Skipped: 0

  コミット

  - a33c0f1 feat(post): 投稿作成フォーム表示を実装


**振り返り**:
投稿作成フォーム機能は追加出来た。
ソースについて確認したところ、仕様の記載が間違っていたため、controller/PostController.java の
戻り値がposts/form.htmlとなってしまった。
上記はプロンプト5にて修正を実施した。
また、「順序」に記載の手順にて、TDDを用いた実装が走ることを確認した。


## プロンプト 5

**フェーズ**:M2: 投稿作成フォーム：修正

**プロンプト本文**:

```
投稿作成フォーム機能の修正を TDD で実装してください。

順序:
1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
2. その失敗テストを通す最小実装を書きます (Green)。
3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

修正内容:
-  posts/new にリクエストした際に、返されるビュー名をposts/formとする

制約:
- AGENTS.md と .codex/instructions.md を参照すること。
- このタスクのスコープ外のファイルは触らないこと。
- テストを書かずに本体コードを書き始めないこと。
- 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
- 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。
```

**結果**: 効いた

投稿作成フォーム機能の修正を TDD で完了しました。

  変更ファイル

  - src/test/java/com/example/tsubuyaki/controller/PostNewFormFeatureTest.java
      - GET /posts/new の期待ビュー名を posts/form に修正

  - src/main/java/com/example/tsubuyaki/controller/PostController.java
      - newForm の戻り値を posts/form に修正

  確認

  - Red: View name expected:<posts/form> but was:<posts/form.html> で失敗確認
  - Green: 最小実装後、対象テスト成功
  - 全体テスト: ./mvnw -B -Ph2 test 成功
      - Tests run: 7, Failures: 0, Errors: 0, Skipped: 0

  コミット

  - b78f280 feat(post): 投稿作成フォームのビュー名を修正

**振り返り**:
投稿作成フォーム機能の仕様修正に対するソース修正が完了した。
また、プロンプト 3にて記載した通り、テスト実施を必ず盛り込むことで、
修正内容の質が担保出来ている


## プロンプト 6

**フェーズ**:M3: 投稿登録

**プロンプト本文**:

```
投稿登録機能（POST /posts ）を TDD で実装してください。

順序:
1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
2. その失敗テストを通す最小実装を書きます (Green)。
3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

受入基準:
- テーブル「posts」にデータ登録を行う
- バリデーション: author 1～30 文字、body 1～280 文字、空白のみおよび文字数超過 NG
- 成功: 302 で /posts にリダイレクト
- 失敗: 200 で posts/form を再表示しエラー内容を表示

以下の観点のテストを用意してから本機能の実装を行う。
- バリデーションテスト: author 1～30 文字、body 1～280 文字、空白のみおよび文字数超過 NG
- 成功時：302リダイレクトされ、保存処理が呼ばれること
- 失敗時：200 OKで `posts/form` が返ること

制約:
- AGENTS.md と .codex/instructions.md を参照すること。
- このタスクのスコープ外のファイルは触らないこと。
- テストを書かずに本体コードを書き始めないこと。
- 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
- 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。
```

**結果**: 効いた

変更ファイル
  - src/test/java/com/example/tsubuyaki/controller/PostCreateFeatureTest.java
      - 正常系、境界値、空白のみ、文字数超過、失敗時の posts/form 再表示を 1 本のテストで確認

  - src/main/java/com/example/tsubuyaki/controller/PostController.java
      - POST /posts を追加
      - 成功時は保存して redirect:/posts
      - バリデーションエラー時は posts/form

  - src/main/java/com/example/tsubuyaki/service/PostService.java
      - posts テーブルへ Post を保存する create を追加

  確認

  - Red: PostService#create(String,String) 未実装で失敗確認
  - Green: 最小実装後、追加テスト成功
  - 全体テスト: ./mvnw -B -Ph2 test 成功
      - Tests run: 8, Failures: 0, Errors: 0, Skipped: 0

  コミット

  - ab3e137 feat(post): 投稿登録を実装


**振り返り**
:
投稿登録機能は追加出来た。
ソースについて確認したが、問題なく実装が出来ている。
また、「順序」に記載の手順にて、TDDを用いた実装が走ることを確認した。


## プロンプト 7

**フェーズ**:M4: 投稿詳細

**プロンプト本文**:

```
投稿詳細機能（GET /posts/{id} ）を TDD で実装してください。

順序:
1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
2. その失敗テストを通す最小実装を書きます (Green)。
3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

受入基準:
- posts/detail.html を表示する
- 存在しない id が指定された場合は 404を返す

以下の観点のテストを用意してから本機能の実装を行う。
- /posts/{id} にリクエストした際に、返されるビュー名がposts/detailであること
- /posts/{id} のidが存在する場合は、posts/detail.html を表示し、当該データが表示されること
- /posts/{id} のidが存在しない場合は、404を返すこと。

制約:
- AGENTS.md と .codex/instructions.md を参照すること。
- このタスクのスコープ外のファイルは触らないこと。
- テストを書かずに本体コードを書き始めないこと。
- 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
- 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。
```

**結果**: 効いた
変更ファイル

  - src/test/java/com/example/tsubuyaki/controller/PostDetailFeatureTest.java
      - 存在する ID は posts/detail を返し、投稿内容を表示
      - 存在しない ID は 404 を返すことを確認

  - src/main/java/com/example/tsubuyaki/controller/PostController.java
      - GET /posts/{id} を追加
      - 未存在時は ResponseStatusException(NOT_FOUND) で 404

  - src/main/java/com/example/tsubuyaki/service/PostService.java
      - findById を追加

  - src/main/resources/templates/posts/detail.html
      - th:text で投稿者・本文・作成日時を表示

  確認

  - Red: Status expected:<200> but was:<404> で失敗確認
  - Green: 最小実装後、詳細テスト成功
  - 全体テスト: ./mvnw -B -Ph2 test 成功
      - Tests run: 9, Failures: 0, Errors: 0, Skipped: 0

  コミット

  - 8113c3a feat(post): 投稿詳細を実装


**振り返り**:
投稿詳細機能は追加出来た。
ソースについて確認したが、問題なく実装が出来ている。
また、「順序」に記載の手順にて、TDDを用いた実装が走ることを確認した。


## プロンプト 7

**フェーズ**:M5: ヘルスチェック
背景：WEB画面を動かそうとした際に、/postsが500エラーで返ってくることがあった
LogをCodexと確認したところ、Java側の日時型がOlacle側とあっていないことが原因である、という結論となり
修正を実施することとなった


**プロンプト本文**:

```
Oracle 実行時に一覧表示で発生する ORA-18716 を TDD で修正してください。

  背景:
  - `/posts` にアクセスすると以下のエラーが発生する
    `ORA-18716: {0}はどのタイム・ゾーンでもありません。DATE`
  - スタックトレース上は `PostRepository.findTop50ByOrderByCreatedAtDesc`
    → `PostService.latest`
    → `PostController.list` で発生している
  - `Post.createdAt` は現在 `Instant`
  - DB の `posts.created_at` は `TIMESTAMP(6)`
  - Hibernate / Oracle JDBC が `Instant` を `OffsetDateTime` として取り出そうとして失敗している可能性が高い

  修正方針:
  - `created_at TIMESTAMP(6)` に合わせて、Java 側の `createdAt` を `Instant` から `LocalDateTime` に変更する
  - `PostService#create` は `Instant.now()` ではなく `LocalDateTime.now()` を使う
  - 既存テスト内の `Instant.parse(...)` は `LocalDateTime.of(...)` などに置き換える
  - Thymeleaf の日時表示は既存の `#temporals.format(...)` を維持する

  順序:
  1. まず、この不具合を再現または防止する失敗テストを 1 本だけ書く (Red)。
     - H2 では Oracle JDBC の ORA-18716 自体は再現できない可能性があるため、
       `Post.createdAt` が `LocalDateTime` として扱われること、または一覧取得・詳細表示で `LocalDateTime` の投稿を
       表示できることを検証するテストにする。
  2. その失敗テストを通す最小実装を書く (Green)。
  3. 既存テストを `Instant` 依存から `LocalDateTime` 依存へ修正する。
  4. 重複・命名・抽象度の観点でリファクタリングする (Refactor)。
  5. `./mvnw -B -Ph2 test` で全テストが緑であることを確認する。
  6. 可能なら `./mvnw -B -Ph2 verify` も実行する。
  7. Conventional Commits でコミットする。
     - 例: `fix(post): Oracleで投稿日時を読み出せない問題を修正`

  受入基準:
  - `Post.createdAt` が `LocalDateTime` である
  - `posts.created_at TIMESTAMP(6)` との対応で Oracle JDBC の `ORA-18716` を避ける設計になっている
  - `/posts` の一覧表示テストが通る
  - `/posts/{id}` の詳細表示テストが通る
  - 投稿登録時に `created_at` が保存される
  - `./mvnw -B -Ph2 test` が成功する

  制約:
  - AGENTS.md と `.codex/instructions.md` を参照すること
  - TDD: RED → GREEN → REFACTOR の順を守ること
  - テストを書かずに本体コードを書き始めないこと
  - 失敗テストに `@Disabled` を貼ることは禁止
  - スコープ外のファイルは触らないこと
  - `th:utext` は使わず、既存どおり `th:text` を使うこと
  - 完了後に変更ファイルとテスト結果を要約して報告すること
```

**結果**: 効いた
  変更内容

  - src/main/java/com/example/tsubuyaki/domain/Post.java
      - createdAt を Instant から LocalDateTime に変更

  - src/main/java/com/example/tsubuyaki/service/PostService.java
      - 投稿登録時の日時を LocalDateTime.now() に変更

  - src/test/java/com/example/tsubuyaki/domain/PostCreatedAtTypeTest.java
      - createdAt が LocalDateTime として扱われることを防止テスト化

  - 既存テスト 3 本
      - Instant.parse(...) を LocalDateTime.of(...) に置き換え

  確認

  - Red: expected: java.time.LocalDateTime but was: java.time.Instant で失敗確認
  - Green: 型変更後、追加テスト成功
  - ./mvnw -B -Ph2 test 成功
      - Tests run: 10, Failures: 0, Errors: 0, Skipped: 0

  - ./mvnw -B -Ph2 verify は最初 JaCoCo の生成物不整合で失敗
  - ./mvnw -B -Ph2 clean verify 成功
      - Checkstyle 0 件
      - SpotBugs 0 件
      - JaCoCo check 成功

  コミット

  - ab496f7 fix(post): Oracleで投稿日時を読み出せない問題を修正


**振り返り**:
本エラーについて、修正されていることを確認
ソースについても、当該箇所が修正され、WEBアプリも動いていることが確認出来た。
M4: 投稿詳細の表示方法がないため、プロンプト8にて追加する


## プロンプト 8

**フェーズ**:M4: 投稿詳細：投稿一覧からの表示

**プロンプト本文**:

```
投稿一覧機能の修正を TDD で実装してください。

順序:
1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
2. その失敗テストを通す最小実装を書きます (Green)。
3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

修正内容:
-  投稿一覧画面（posts/list.html）の投稿一覧内タイトル（author）をクリックすることで当該の投稿詳細に遷移する

受入基準:
-  投稿一覧内のタイトルにリンクがあり、押すとposts/{id} にリクエストすること

制約:
- AGENTS.md と .codex/instructions.md を参照すること。
- このタスクのスコープ外のファイルは触らないこと。
- テストを書かずに本体コードを書き始めないこと。
- 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
- 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。
```

**結果**: 効いた

変更ファイル

  - src/test/java/com/example/tsubuyaki/controller/PostListFeatureTest.java
      - 一覧 HTML に href="/posts/{id}" が含まれることを追加検証

  - src/main/resources/templates/posts/list.html
      - 投稿者名を span から詳細リンク付きの a に変更
      - 表示は引き続き th:text を使用

  確認

  - Red: href="/posts/{id}" が見つからず失敗
  - Green: テンプレート修正後、一覧テスト成功
  - 全体テスト: ./mvnw -B -Ph2 test 成功
      - Tests run: 10, Failures: 0, Errors: 0, Skipped: 0

  コミット

  - 58cf64e feat(post): 投稿一覧から詳細へ遷移できるようにする

**振り返り**:

本内容について、修正されていることを確認
ソースについても、当該箇所が修正され、WEBアプリも動いていることが確認出来た。


## プロンプト 9

**フェーズ**:

**プロンプト本文**:

```

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**: