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
また、「順序」に記載の手順にて、TDDを用いた実装が走ることを確認した。
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
また、「順序」に記載の手順にて、TDDを用いた実装が走ることを確認した。


## プロンプト 9

**フェーズ**:S1: いいね

**プロンプト本文**:

```
いいね機能（POST /posts/{id}/likes ）を TDD で実装してください。

順序:
1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
2. その失敗テストを通す最小実装を書きます (Green)。
3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

受入基準:
- 投稿一覧画面（posts/list.html）の各投稿にいいねトグルを表示する
- 同一 clientHash (= ハッシュ化された IP + UA、SHA-256 の先頭 8 文字) が再度押したら解除
- 詳細画面（posts/detail.html）で総いいね数と Like ボタン（いいねトグル）を表示する

以下の観点のテストを用意してから本機能の実装を行う。
- 投稿一覧画面（posts/list.html）の各投稿にいいねトグルがあり、/posts/{id}/likes にリクエストすること
- 詳細画面（posts/detail.html）で総いいね数と Like ボタン（いいねトグル）があり、があり、Like ボタンが/posts/{id}/likes にリクエストすること
- 同一 clientHashにいいね がされていない状態で押したら、当該clientHashが登録されること
- 同一 clientHashにいいね がされている状態で押したら、当該clientHashが解除されること
- /posts/{id}/likes のidが存在しない場合は、404を返すこと。

制約:
- AGENTS.md と .codex/instructions.md を参照すること。
- このタスクのスコープ外のファイルは触らないこと。
- テストを書かずに本体コードを書き始めないこと。
- 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
- 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。
```

**結果**: 部分的に効いた
Red は PostLikeFeatureTest 1 本を先に追加し、一覧/詳細の Like フォーム、同一 clientHash の登録・解除、存在しない
  id の 404 を固定しました。その後 Green 実装と小さな Refactor を行い、./mvnw -B -Ph2 test は Tests run: 11,
  Failures: 0, Errors: 0, Skipped: 0 で成功しています。

  コミット:
  1332eaa feat(like): 投稿にいいねトグルを追加

  主な変更:

  - src/main/java/com/example/tsubuyaki/controller/PostController.java: POST /posts/{id}/likes と clientHash 生成を
    追加

  - src/main/java/com/example/tsubuyaki/service/PostService.java: 同一 clientHash のいいね登録/解除を実装
  - src/main/java/com/example/tsubuyaki/domain/PostLike.java, src/main/java/com/example/tsubuyaki/repository/
    PostLikeRepository.java, src/main/resources/db/migration/V2__create_post_likes.sql: いいね永続化を追加

  - src/main/resources/templates/posts/list.html, src/main/resources/templates/posts/detail.html: Like トグルと総い
    いね数を表示

  - src/test/java/com/example/tsubuyaki/controller/PostLikeFeatureTest.java: 受入基準テストを追加
  

**振り返り**:
いいね機能は追加出来た。
「順序」に記載の手順にて、TDDを用いた実装が走ることを確認した。
実装内容については、「いいね」ボタン押下時に、posts/detail.htmlに強制的に遷移してしまう実装となってしまっている
そのため、プロンプト 10にて修正を行う


## プロンプト 10

**フェーズ**:S1: いいね：修正

**プロンプト本文**:

```
いいね機能（POST /posts/{id}/likes ）の修正について TDD で実装してください。

順序:
1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
2. その失敗テストを通す最小実装を書きます (Green)。
3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

背景:
- 投稿一覧画面（posts/list.html）および詳細画面（posts/detail.html）に配置されたいいねトグルについて、押下時に詳細画面（posts/detail.html）に遷移してしまう。

修正方針:
- 投稿一覧画面（posts/list.html）のいいねトグルを押下：/posts/{id}/likesにリクエストされる、画面はposts/list.htmlのままとする。
- 詳細画面（posts/detail.html）のいいねトグルを押下：/posts/{id}/likesにリクエストされる、画面はposts/detail.htmlのままとする。（現状仕様と同等）

受入基準:
以下の観点のテストを用意してから本機能の実装を行う。
- 投稿一覧画面（posts/list.html）の各投稿にいいねトグルがあり、/posts/{id}/likes にリクエストし、画面はposts/list.htmlのままとすること
- 詳細画面（posts/detail.html）で総いいね数と Like ボタン（いいねトグル）があり、があり、Like ボタンが/posts/{id}/likes にリクエストし、画面はposts/detail.htmlのままとすること
- 同一 clientHashにいいね がされていない状態で押したら、当該clientHashが登録されること
- 同一 clientHashにいいね がされている状態で押したら、当該clientHashが解除されること
- /posts/{id}/likes のidが存在しない場合は、404を返すこと。

制約:
- AGENTS.md と .codex/instructions.md を参照すること。
- このタスクのスコープ外のファイルは触らないこと。
- テストを書かずに本体コードを書き始めないこと。
- 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
- 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。

```

**結果**: 効いた
Red: src/test/java/com/example/tsubuyaki/controller/PostLikeFeatureTest.java の 1 テストを更新し、一覧からの Like
  は /posts に戻る、詳細からの Like は /posts/{id} に戻る期待値で失敗確認しました。

  Green/Refactor:

  - src/main/java/com/example/tsubuyaki/controller/PostController.java: returnTo パラメータで戻り先を list/detail
    に分岐

  - src/main/resources/templates/posts/list.html: Like フォームに returnTo=list を追加
  - src/main/resources/templates/posts/detail.html: Like フォームに returnTo=detail を追加

  検証:
  ./mvnw -B -Ph2 test は Tests run: 11, Failures: 0, Errors: 0, Skipped: 0 で成功。

  コミット:
  5ed1b90 feat(like): いいね後に押下元画面へ戻す


**振り返り**:
いいね機能の修正について完了した。
ソースについても、当該箇所の修正、WEBアプリも動いていることが確認出来た。
「順序」に記載の手順にて、TDDを用いた実装が走ることを確認した。


## プロンプト 11

**フェーズ**:S2: キーワード検索

**プロンプト本文**:

```
キーワード検索機能（GET /posts?q=xxx ）を TDD で実装してください。

順序:
1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
2. その失敗テストを通す最小実装を書きます (Green)。
3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

受入基準:
- 投稿一覧画面（posts/list.html）の上部（更新ボタンの下）に、検索ボックスおよび検索ボタンを横並びで追加する
- GET /posts?q=xxx で本文をLIKE検索する（部分一致とする）
- 検索結果の投稿を新着順 (created_at DESC) で最大 50 件を返し、投稿一覧画面（posts/list.html）に反映する
- 各画面項目（ヘッダー、更新ボタン、検索部、投稿一覧部）が見やすいよう、間隔を調整する

以下の観点のテストを用意してから本機能の実装を行う。
- 投稿一覧画面（posts/list.html）の上部に検索ボックスおよび検索ボタンを配置され、GET /posts?q=xxx にリクエストすること
- 検索ボックスが空（スペースのみの場合も含む）の場合に検索ボタンが押下された場合、posts/ にリクエストすること
- 検索結果が0件の場合に画面に「条件に一致する結果が見つかりませんでした。」を表示できていること
- 51件以上の投稿がある場合に、新着50件しか表示されないこと

制約:
- AGENTS.md と .codex/instructions.md を参照すること。
- このタスクのスコープ外のファイルは触らないこと。
- テストを書かずに本体コードを書き始めないこと。
- 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
- 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。

```

**結果**: 効いた

  Red: src/test/java/com/example/tsubuyaki/controller/PostListFeatureTest.java に検索受入テストを 1 本追加し、検索
  フォーム未実装で失敗確認しました。

  主な変更:

  - src/main/java/com/example/tsubuyaki/controller/PostController.java: GET /posts?q=... を受け、空白 trim 後に通常
    一覧/検索を切り替え

  - src/main/java/com/example/tsubuyaki/repository/PostRepository.java, src/main/java/com/example/tsubuyaki/
    service/PostService.java: 本文部分一致を新着順最大 50 件で取得

  - src/main/resources/templates/posts/list.html, src/main/resources/static/css/app.css: 更新ボタン下に横並び検索
    フォーム、0 件メッセージ、間隔調整を追加

  検証:
  ./mvnw -B -Ph2 test は Tests run: 12, Failures: 0, Errors: 0, Skipped: 0 で成功。

  コミット:
  bb5f5ca feat(search): 投稿本文のキーワード検索を追加

**振り返り**:
キーワード検索の実装について完了した。
ソースについても、当該箇所の実装、WEBアプリも動いていることが確認出来た。
また、各画面項目について、項目間の間隔修正を実施、反映されていることを確認出来た。
「順序」に記載の手順にて、TDDを用いた実装が走ることを確認した。


## プロンプト 12

**フェーズ**:S3: 投稿者名フィールド拡張

**プロンプト本文**:

```
投稿内容の機能拡張について TDD で実装してください。

順序:
1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
2. その失敗テストを通す最小実装を書きます (Green)。
3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

受入基準:
- 投稿作成画面（posts/form.html）にて投稿者名を必須入力とし、項目名を赤で強調する
  - 投稿者名が入力されていない場合、エラーメッセージを表示する
- 投稿作成画面（posts/form.html）にて投稿の背景色の選択項目を追加する（本文の下）
  - 色は6色のカラーパターン（白、赤、紫、青、緑、黄）から選択可能とする。デフォルトは白とする。
  - カラーパターンは薄めの配色とする
  - カラーパターンはカラーパレットとして各色画面上に表示し、直感的に選択可能とする
  - 選択した色については、postsテーブルにカラーコードとして保存する
- 投稿一覧画面の各投稿（posts/list.html）および詳細画面の投稿部（posts/detail.html）の背景色は、postsテーブルに保存した背景色をセットする
  - postsテーブルに背景色がセットされていない場合、背景色を白とする

以下の観点のテストを用意してから本機能の実装を行う。
- 投稿作成画面（posts/form.html）にて、投稿者名が未入力の場合は、失敗: 200 で posts/form を再表示しエラー内容を表示すること
- 投稿作成画面（posts/form.html）にカラーパレットが6色分横並びで表示されること
- 選択した色については、postsテーブルにカラーコードとして保存されること
- 投稿一覧画面の各投稿（posts/list.html）および詳細画面の投稿部（posts/detail.html）の背景色は、、postsテーブルに保存した背景色となること

制約:
- AGENTS.md と .codex/instructions.md を参照すること。
- このタスクのスコープ外のファイルは触らないこと。
- テストを書かずに本体コードを書き始めないこと。
- 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
- 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。
```

**結果**: 部分的に効いた
Red: src/test/java/com/example/tsubuyaki/controller/PostBackgroundColorFeatureTest.java を 1 本追加し、フォームの
  必須エラー、6 色パレット、DB 保存、一覧/詳細の背景色反映を先に失敗させました。

  主な変更:

  - src/main/java/com/example/tsubuyaki/web/dto/PostForm.java, src/main/resources/templates/posts/form.html: 投稿者
    名ラベルの赤強調、6 色カラーパレット、背景色バリデーションを追加

  - src/main/java/com/example/tsubuyaki/domain/Post.java, src/main/java/com/example/tsubuyaki/domain/
    PostBackgroundColor.java, src/main/resources/db/migration/V3__add_post_background_color.sql:
    posts.background_color を追加し、未設定時は白に正規化

  - src/main/resources/templates/posts/list.html, src/main/resources/templates/posts/detail.html, src/main/
    resources/static/css/app.css: 保存済み背景色を投稿表示に反映し、パレット表示を整備

  検証:
  ./mvnw -B -Ph2 test は Tests run: 13, Failures: 0, Errors: 0, Skipped: 0 で成功。

  コミット:
  cd7a935 feat(post): 投稿背景色を選択できるようにする

**振り返り**:
投稿内容の機能拡張は追加出来た。
「順序」に記載の手順にて、TDDを用いた実装が走ることを確認した。
実装内容については、投稿作成画面（posts/form.html）のレイアウトが崩れてしまっている状態
そのため、プロンプト 13にて修正を行う


## プロンプト 13

**フェーズ**:S3: 投稿者名フィールド拡張：レイアウト修正

**プロンプト本文**:

```
投稿作成画面のレイアウト修正について TDD で実装してください。

背景:
- 投稿作成画面（posts/form.html）の各項目について、間隔が狭いため、レイアウトが崩れている状態

修正方針:
- 投稿作成画面（posts/form.html）の各項目について、見やすいよう、間隔を調整する
- 色選択のカラーパレットのデザインについて、以下に変更する
  - 丸チェックの表示を削除
  - 角丸ボタン風に変更
  - 選択されている、またはマウスオーバーされているボタンは協調表示する

順序:
1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
2. その失敗テストを通す最小実装を書きます (Green)。
3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

受入基準:
以下の観点のテストを用意してから本機能の実装を行う。
- 修正方針の通り、デザインが修正されていること

制約:
- AGENTS.md と .codex/instructions.md を参照すること。
- このタスクのスコープ外のファイルは触らないこと。
- テストを書かずに本体コードを書き始めないこと。
- 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
- 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。
```

**結果**: 効いた
 Red/Green/Refactor の流れで、投稿作成フォームのレイアウトとカラーパレット表示を検証するテストを追加し、最小実装後
  に構造上の追加リファクタは不要と判断しました。

  変更した主な3ファイル:

  - src/test/java/com/example/tsubuyaki/controller/PostNewFormFeatureTest.java
  - src/main/resources/templates/posts/form.html
  - src/main/resources/static/css/app.css

  確認:

  - ./mvnw -B -Ph2 -Dtest=PostNewFormFeatureTest test 成功
  - ./mvnw -B -Ph2 test 成功
  - diff --check 問題なし
  - コミット: e75e421 feat(post): 投稿作成フォームのレイアウトを調整


**振り返り**:
投稿作成画面のレイアウト修正は出来た。
ソースについても、当該箇所の修正、WEBアプリも動いていることが確認出来た。
一覧及び詳細画面のレイアウトについても、より見やすくするため、プロンプト 14にて修正を行う


## プロンプト 13

**フェーズ**:S3: 投稿者名フィールド拡張：レイアウト修正

**プロンプト本文**:

```
投稿一覧画面の各投稿および詳細画面の投稿部のレイアウト修正について TDD で実装してください。

背景:
- 投稿一覧画面の各投稿（posts/list.html）および詳細画面の投稿部（posts/detail.html）について、レイアウトをより見やすくしたい

修正方針:
- 投稿一覧画面の各投稿（posts/list.html）および詳細画面の投稿部（posts/detail.html）について、以下レイアウト修正を行う
  - 投稿者、内容、投稿日、いいね数、いいねボタン の表示順は変更なし
  - 各項目が見やすいよう、サイズおよび間隔を調整する
    - サイズ大：内容
    - サイズ中：投稿者、投稿日、いいね数、いいねボタン
  - いいねボタンは角丸ボタン風にデザイン変更を行う（サイズは変更なし）
  - いいねボタンのキャプションは「いいね！」とする
  - いいね数、いいねボタン は横並びとする

順序:
1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
2. その失敗テストを通す最小実装を書きます (Green)。
3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

受入基準:
以下の観点のテストを用意してから本機能の実装を行う。
- 修正方針の通り、デザインが修正されていること

制約:
- AGENTS.md と .codex/instructions.md を参照すること。
- このタスクのスコープ外のファイルは触らないこと。
- テストを書かずに本体コードを書き始めないこと。
- 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
- 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。

```

**結果**: 効いた
  Red では PostCardLayoutFeatureTest を1本追加し、一覧・詳細の投稿カード構造、いいね数とボタンの横並び、ボタン表記
  「いいね！」、CSSのサイズ・間隔・角丸指定を検証しました。Green で posts/list.html / posts/detail.html / app.css
  を修正し、既存のいいねテストも新キャプション仕様へ合わせました。

  確認:

  - ./mvnw -B -Ph2 -Dtest=PostCardLayoutFeatureTest test 成功
  - ./mvnw -B -Ph2 test 成功: Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
  - コミット: 0b88de4 feat(post): 投稿カードのレイアウトを調整

  変更ファイル:

  - src/main/resources/templates/posts/list.html
  - src/main/resources/templates/posts/detail.html
  - src/main/resources/static/css/app.css
  - src/test/java/com/example/tsubuyaki/controller/PostCardLayoutFeatureTest.java
  - src/test/java/com/example/tsubuyaki/controller/PostLikeFeatureTest.java

**振り返り**:
一覧及び詳細画面のレイアウト修正は出来た。
ソースについても、当該箇所の修正、WEBアプリも動いていることが確認出来た。


## プロンプト 15

**フェーズ**:投稿一覧のモダンなタイムラインUI化
経緯：
現在のつぶやきボードアプリのデザインがモダンではないため、デザイン変更を実施する
デザイン案をCodexと壁打ちし作成、テンプレートとCSS（外部CDNを使用しない）にて実装する。

**プロンプト本文**:

```
現在の「社内つぶやきボード」アプリのデザインを、業務アプリ寄りのモダンな X 風ミニ SNS UI に改善する。

順序:
1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
2. その失敗テストを通す最小実装を書きます (Green)。
3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

実装したいデザイン:
- 全体は薄いグレー背景の、落ち着いた社内向け UI
- 中央にタイムラインを配置する 1 カラム中心のレイアウト
- 投稿フォームは白背景のカード風 UI
- 投稿一覧は白背景の投稿カードとして表示
- カードの角丸は 8px 程度
- border と余白で読みやすく整理
- ボタンは控えめなアクセントカラーで統一
- 本文は読みやすい行間にする
- スマホ表示でも 1 カラムで破綻しないレスポンシブ CSS にする
- ランディングページ風・派手なグラデーション・装飾過多にはしない
- Thymeleaf では XSS 対策として th:text を維持し、th:utext は使わない
- Spring Security の CSRF がある場合はフォームの _csrf を維持する

受入基準:
- 投稿一覧画面がモダンな中央タイムライン型 UI になっている
- 投稿フォームと投稿カードが視覚的に分離されている
- PC / スマホ幅の両方でレイアウトが破綻しない

実装上の注意:
- HTML 構造は大きく変えすぎず、既存機能のテストが通る範囲で class 追加を中心にする。
- CSS は既存の static/css 配下の構成に合わせて配置する。
- 既存 CSS がある場合は、無関係なリセットや全面置換ではなく、読みやすく整理した差分にする。
- th:text を th:utext に変更しない。
- JavaScript は追加しない。
- 画像や外部 CDN は使わない。
- Maven Central / GitHub / api.openai.com 以外へのネットワークアクセスはしない。

制約:
- 既存機能の挙動を変えず、主にテンプレートと CSS の改善に限定すること。
- AGENTS.md と .codex/instructions.md を参照すること。
- このタスクのスコープ外のファイルは触らないこと。
- テストを書かずに本体コードを書き始めないこと。
- 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
- 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。


```

**結果**: 効いた
  差分

  - src/test/java/com/example/tsubuyaki/controller/PostListFeatureTest.java: モダン UI 用の Red テストを 1 本追加
  - src/main/resources/templates/posts/list.html: app-shell、app-header、composer-card、timeline-feed、post-card を
    追加
    ポンシブを追加

  検証

  - Red: 追加テストが未実装 class 不足で失敗することを確認
  - Green: 追加テスト成功
  - 全体: ./mvnw -B -Ph2 test 成功
      - Tests run: 16, Failures: 0, Errors: 0, Skipped: 0

  コミット

  - 2b2b7bf feat(ui): 投稿一覧をモダンなタイムラインUIにする
  
**振り返り**:
一覧及び詳細画面のレイアウト修正は出来た。
ソースについても、当該箇所の修正、WEBアプリも動いていることが確認出来た。
また、Codexと壁打ちし、よりよい仕様に出来た。


## プロンプト 14

**フェーズ**:C2: 投稿削除

**プロンプト本文**:

```
投稿削除機能（POST /posts/{id}/del）を TDD で実装してください。

順序:
1. まず受入基準を満たすための失敗テストを 1 本だけ書く (Red)。
2. その失敗テストを通す最小実装を書く (Green)。
3. 重複・命名・抽象度の観点でリファクタリングする (Refactor)。
4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認する。
5. Conventional Commits でコミットする (`feat(<scope>): <要約>`)。

受入基準:
概要
- ユーザログイン機能が無い現状で、投稿者本人に近い判定として clientHash を使い、投稿削除機能を追加する。
- 削除は物理削除ではなく論理削除とする。
- 削除時はいきなり削除せず、削除確認画面を挟む。

仕様内容
- postsテーブルに 「client_hash」と「deleted_at」を作成する
  - 「deleted_at」のデフォルト値は0とする
- 投稿作成時に clientHash を取得し、Posts テーブルに保存する。
- clientHash は「ハッシュ化された IP + UA、SHA-256 の先頭 8 文字」である。
- 詳細画面 （posts/detail.html） に削除確認画面へ進むボタンまたはリンクを追加する。
- 投稿したユーザ、つまり clientHash が同一の投稿にのみ削除確認ボタンを表示する。
- 削除確認ボタン押下時に /posts/{id}/delete-confirm に GET リクエストする。
- 削除確認画面（posts/delete-confirm.html） を追加する。
- 削除確認画面では、削除対象の投稿内容と警告メッセージを表示する。
  - 文言例: `この投稿を削除します。よろしいですか？`
- 削除確認画面には以下を表示する。
  - 削除実行ボタン
  - キャンセルリンクまたは戻るリンク
- 削除実行ボタン押下時に /posts/{id}/del に POST リクエストする。
- 削除リクエストでは現在の clientHash を再計算し、投稿の clientHash と比較する。
  - 一致していれば削除 OK。
  - 一致していなければ削除 NG とし、エラーメッセージを表示する。
- 削除 OK の場合、論理削除を行う。
  - `posts.deleted_at = 1` とする。
- 論理削除済みデータは投稿一覧画面（posts/list.html） で非表示にする。
- 論理削除済みデータは検索対象外にする。
- 論理削除済みデータに詳細画面 URL から直接アクセスした場合、エラーメッセージを表示する。

以下の観点のテストを用意してから本機能の実装を行う。
- postsテーブルに 「client_hash」と「deleted_at」 が追加されていること。
- 投稿作成時に 「clientHash」 が保存されること。
- 詳細画面で投稿者本人のみ削除確認ボタンが表示されること。
- 削除確認ボタンがあり 「GET /posts/{id}/delete-confirm」 にリクエストされること。
- 削除確認画面（posts/delete-confirm.html） があること。
- 削除確認画面に削除対象の投稿内容と警告メッセージが表示されること。
- 削除確認画面に削除実行ボタンとキャンセルリンクが表示されること。
- 削除実行ボタンがあり、「POST /posts/{id}/del」にリクエストされること。
- clientHash 一致時のみ「posts.deleted_at = 1」になること。
- clientHash 不一致時は削除されず、エラーメッセージが表示されること。
- 論理削除済み投稿は一覧に表示されないこと。
- 論理削除済み投稿は検索対象外であること。
- 論理削除済み投稿の詳細 URL 直アクセスではエラーメッセージが表示されること。
- 既存の投稿作成、一覧、検索、詳細、いいね機能が壊れていないこと。

制約:
- AGENTS.md と .codex/instructions.md を参照すること。
- このタスクのスコープ外のファイルは触らないこと。
- テストを書かずに本体コードを書き始めないこと。
- 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
- 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。
- Maven Central / GitHub / api.openai.com 以外へのネットワークアクセスはしない。
- Thymeleaf では XSS 対策として `th:text` を維持し、`th:utext` は使わない。
- JavaScript は追加しない。
- 外部 CDN、外部画像、外部フォント、外部コンポーネントは使わない。

```

**結果**: 効いた
  追加内容:

  - client_hash / deleted_at 追加 migration を作成
  - 投稿作成時に IP + ":" + User-Agent を SHA-256 化し、先頭 8 文字を保存
  - 詳細画面で本人投稿のみ削除確認リンクを表示
  - GET /posts/{id}/delete-confirm の確認画面を追加
  - POST /posts/{id}/del で clientHash 一致時のみ deleted_at = 1
  - 削除済み投稿を一覧・検索・詳細表示から除外
  - 削除済み詳細直アクセス時は 投稿が見つかりません。 を表示

  主な変更ファイル:

  - src/main/java/com/example/tsubuyaki/controller/PostController.java
  - src/main/java/com/example/tsubuyaki/service/PostService.java
  - src/main/java/com/example/tsubuyaki/domain/Post.java
  - src/main/resources/db/migration/V4__add_post_delete_columns.sql
  - src/main/resources/templates/posts/delete-confirm.html
  - src/test/java/com/example/tsubuyaki/controller/PostDeleteFeatureTest.java

  検証:

  - Red: PostDeleteFeatureTest を 1 本追加し、client_hash 未追加で失敗確認
  - Green: 実装後、追加テスト成功
  - 全体: ./mvnw -B -Ph2 test 成功
      - Tests run: 17, Failures: 0, Errors: 0, Skipped: 0

  コミット:

  - 77a6282 feat(post): 投稿削除機能を追加する

**振り返り**:
投稿の削除機能が実装出来た。
ソースについても、当該箇所の実装、WEBアプリも動いていることが確認出来た。
「順序」に記載の手順にて、TDDを用いた実装が走ることを確認した。
また、Codexと壁打ちし、仕様の詳細化を実施した


## プロンプト 15

**フェーズ**:投稿編集機能
UXの観点から、投稿編集機能は必須。
そのため、Codexと壁打ちし仕様策定、実装を行う。

**プロンプト本文**:

```
投稿編集機能を TDD で実装する。

目的:
- ユーザログイン機能が無い現状で、投稿者本人に近い判定として clientHash を使い、投稿編集機能を追加する。
- 保守しやすさ・読みやすさを重視する。
- 新規投稿用 DTO と編集用 DTO を分ける。
- 更新結果を enum で表現し、Controller の分岐を読みやすくする。
- JavaScript、外部 CDN、外部コンポーネントは使わない。

順序:
1. まず受入基準を満たすための失敗テストを 1 本だけ書く (Red)。
2. その失敗テストを通す最小実装を書く (Green)。
3. 重複・命名・抽象度の観点でリファクタリングする (Refactor)。
4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認する。
5. Conventional Commits でコミットする (`feat(<scope>): <要約>`)。

仕様:
- 詳細画面 `posts/detail.html` に編集画面へ進むリンクを追加する。
- 投稿したユーザ、つまり clientHash が同一の投稿にのみ編集リンクを表示する。
- 編集リンク押下時に `GET /posts/{id}/edit` にリクエストする。
- 編集画面 `posts/edit.html` を追加する。
- 編集画面では、編集対象の投稿者名、本文、背景色を表示する。
- 投稿者名は変更不可とする。
- 編集できる項目は以下に限定する。
  - 本文 `body`
  - 背景色 `backgroundColor`
- 編集できない項目は以下とする。
  - 投稿者名 `author`
  - 投稿日時 `createdAt`
  - clientHash
  - deletedAt
  - likeCount
- 更新ボタン押下時に `POST /posts/{id}/edit` にリクエストする。
- 更新リクエストでは現在の clientHash を再計算し、投稿の clientHash と比較する。
  - 一致していれば更新 OK。
  - 一致していなければ更新 NG とし、エラーメッセージを表示する。
- 更新 OK の場合、本文と背景色を更新する。
- 更新 OK の場合、`posts.updated_at` に更新日時を設定する。
- 更新成功後は `redirect:/posts/{id}` で詳細画面へ戻る。
- 論理削除済み投稿は編集不可とする。
- 論理削除済み投稿に編集画面 URL から直接アクセスした場合、エラーメッセージを表示する。
- 本文空文字は更新不可とする。
- 本文 281 文字以上は更新不可とする。
- Thymeleaf では XSS 対策として `th:text` を維持し、`th:utext` は使わない。

設計方針:
- `posts.updated_at` カラムを追加する。
  - 未編集: `null`
  - 編集済み: 更新日時
- `clientHash` 生成は既存の削除機能と同じ仕組みを使う。
- 新規投稿用の `PostForm` は流用しない。
- 編集専用 DTO として `PostEditForm` を新規作成する。
- 更新結果 enum として `UpdatePostResult` を新規作成する。
- 編集画面は `posts/edit.html` として新規作成する。
- Entity に編集用メソッドを追加する。
  - 例: `canModify(clientHash)`
  - 例: `updateBodyAndBackgroundColor(body, backgroundColor, updatedAt)`
- `PostView` には編集可否を読みやすく表すプロパティを追加する。
  - 例: `canEdit`
  - 既存削除機能の `canDelete` は壊さない。
- Service に編集用メソッドを追加する。
  - 例: `findEditableById(id, clientHash)`
  - 例: `update(id, body, backgroundColor, clientHash)`
- Controller は画面遷移とリクエスト変換に集中させる。
- 削除機能の既存挙動を壊さない。

`PostEditForm` の方針:
- パッケージは既存の `PostForm` と同じ `com.example.tsubuyaki.web.dto` に置く。
- 入力項目は `body` と `backgroundColor` のみとする。
- `author` はフォーム送信対象に含めない。
- `body` には以下の validation を付ける。
  - `@NotBlank(message = "本文を入力してください")`
  - `@Size(max = 280, message = "本文は 280 文字以内で入力してください")`
- `backgroundColor` は既存の `PostBackgroundColor` と同じ選択肢を使う。
- 既存投稿から初期化できるファクトリまたはコンストラクタを用意してよい。
  - 例: `PostEditForm.from(PostView post)`

`UpdatePostResult` の方針:
- パッケージは Service 層に置く。
  - 例: `com.example.tsubuyaki.service.UpdatePostResult`
- enum として作成する。
- 値は以下を基本とする。
  - `UPDATED`
  - `NOT_FOUND`
  - `FORBIDDEN`
- validation error は `BindingResult` で扱うため、enum には含めなくてよい。
- Service の更新メソッドはこの enum を返す。
  - 例:
    - `UpdatePostResult update(Long id, String body, String backgroundColor, String clientHash)`
- Controller は `switch` または同等の明確な分岐で結果を処理する。
  - `UPDATED`: `redirect:/posts/{id}`
  - `FORBIDDEN`: 編集画面または詳細画面に `この投稿は編集できません。` を表示
  - `NOT_FOUND`: `投稿が見つかりません。` を表示、または既存方針に合わせて詳細画面でエラー表示


受入基準:
以下の観点のテストを用意してから本機能の実装を行う。
- `posts.updated_at` が追加されている
- `PostEditForm` が存在し、新規投稿用 `PostForm` と分離されている
- `UpdatePostResult` が存在し、更新結果を `UPDATED` / `NOT_FOUND` / `FORBIDDEN` で表せる
- 詳細画面で投稿者本人のみ編集リンクが表示される
- 編集リンクは `GET /posts/{id}/edit` に遷移する
- 編集画面 `posts/edit.html` がある
- 編集画面には投稿者名、本文、背景色が表示される
- 投稿者名は変更できない
- 本文と背景色を更新できる
- 更新成功後は詳細画面へリダイレクトする
- 更新時に `updated_at` が設定される
- clientHash 不一致時は更新されず、エラーメッセージが表示される
- 論理削除済み投稿は編集できない
- 本文空文字は更新できない
- 本文 281 文字以上は更新できない
- 既存の投稿作成、一覧、検索、詳細、いいね、削除機能が壊れていない

制約:
- AGENTS.md と .codex/instructions.md を参照すること。
- このタスクのスコープ外のファイルは触らないこと。
- テストを書かずに本体コードを書き始めないこと。
- 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
- 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。
- Maven Central / GitHub / api.openai.com 以外へのネットワークアクセスはしない。
- Thymeleaf では XSS 対策として `th:text` を維持し、`th:utext` は使わない。
- JavaScript は追加しない。
- 外部 CDN、外部画像、外部フォント、外部コンポーネントは使わない。


```

**結果**: 効いた
  実装内容:

  - posts.updated_at 追加 migration を作成
  - PostEditForm を新規作成し、PostForm と分離
  - UpdatePostResult enum を追加し、UPDATED / NOT_FOUND / FORBIDDEN で更新結果を表現
  - 本人投稿のみ詳細画面に編集リンクを表示
  - GET /posts/{id}/edit と POST /posts/{id}/edit を追加
  - 本文・背景色のみ編集可能にし、投稿者名は表示専用
  - 更新成功時に updated_at を設定し、詳細画面へリダイレクト
  - clientHash 不一致、削除済み、本文 validation エラーをテストで確認

  主な変更ファイル:

  - src/main/java/com/example/tsubuyaki/controller/PostController.java
  - src/main/java/com/example/tsubuyaki/service/PostService.java
  - src/main/java/com/example/tsubuyaki/web/dto/PostEditForm.java
  - src/main/java/com/example/tsubuyaki/service/UpdatePostResult.java
  - src/main/resources/templates/posts/edit.html
  - src/test/java/com/example/tsubuyaki/controller/PostEditFeatureTest.java

  検証:

  - Red: PostEditForm / UpdatePostResult 未実装で失敗確認
  - Green: 追加テスト成功
  - 全体: ./mvnw -B -Ph2 test 成功
      - Tests run: 18, Failures: 0, Errors: 0, Skipped: 0

  コミット:

  - bae90d1 feat(post): 投稿編集機能を追加する

**振り返り**:
投稿の削除機能が実装出来た。
ソースについても、当該箇所の実装、WEBアプリも動いていることが確認出来た。
「順序」に記載の手順にて、TDDを用いた実装が走ることを確認した。
また、Codexと壁打ちし、仕様の詳細化を実施した


## プロンプト 16

**フェーズ**:UI/UX改善
一通り機能の実装が終わったため、UI/UXの改善案をCodexと壁打ち、実装を行う。

**プロンプト本文**:

```
現在の「社内つぶやきボード」について、JavaScript と外部CDNを使わず、Thymeleaf + CSS + サーバー側状態のみで UI/UX改善を TDD で実装してください。

 ## 方針

 - すべて日本語で応答すること。
 - AGENTS.md と .codex/instructions.md を最優先で守ること。
 - RED → GREEN → REFACTOR を 1 改善単位で回すこと。
 - 失敗テストに @Disabled を付けて通すことは禁止。
 - JavaScript は追加しない。
 - 外部CDN、外部フォント、外部アイコンライブラリは使わない。
 - Thymeleaf の出力は XSS 対策として th:text を使い、th:utext は使わない。
 - 既存のテスト雛形 src/test/java/com/example/tsubuyaki/sample/** は削除しない。
 - 実装後に ./mvnw -B -Ph2 verify を実行し、結果を報告すること。
 -  ./mvnw -B -Ph2 verify後、Conventional Commits でコミットすること (`feat(<scope>): <要約>`)。

 ## 対象ファイル候補

 - src/main/resources/templates/posts/list.html
 - src/main/resources/templates/posts/detail.html
 - src/main/resources/templates/posts/form.html
 - src/main/resources/templates/posts/edit.html
 - src/main/resources/templates/posts/delete-confirm.html
 - src/main/resources/static/css/app.css
 - 必要に応じて Controller / DTO / Service / Test

 ## 実装したい UI/UX 改善

 ### 1. 全画面のレイアウト統一

 一覧画面だけでなく、詳細・新規投稿・編集・削除確認画面にも共通の app-shell / app-header / app-nav 相当のレイアウ
 トを適用してください。

 受入基準:
 - 詳細画面に「社内つぶやきボード」の共通ヘッダーが表示される。
 - 新規投稿画面に共通ヘッダーが表示される。
 - 編集画面に共通ヘッダーが表示される。
 - 削除確認画面に共通ヘッダーが表示される。
 - 一覧へ戻る導線、新規投稿導線が画面間で一貫している。
 - モバイル幅でも主要ボタンやリンクがはみ出さない。

 TDD:
 - まず Controller Feature Test / WebMvcTest 相当で、各画面の HTML に共通ヘッダー用 class または文言が含まれること
 を検証する失敗テストを書く。
 - その後、テンプレートと CSS を最小修正して通す。

 ### 2. 検索中の状態と解除導線の表示

 投稿一覧で検索している場合、現在の検索語と検索解除リンクを表示してください。

 受入基準:
 - /posts?q=xxx のとき、「検索: xxx」または同等の検索中表示が出る。
 - 検索解除リンクが /posts を指す。
 - q が空白のみの場合は検索中扱いにしない。
 - 検索語は th:text で表示する。

 TDD:
 - /posts?q=検索語 のレスポンスに検索中表示と /posts への解除リンクが含まれるテストを書く。
 - /posts?q=  または空白のみでは検索中表示が出ないテストを書く。

 ### 3. 投稿カードに詳細導線を追加

 一覧の投稿カードで、投稿者名リンクだけでなく「詳細を見る」リンクを表示してください。

 受入基準:
 - 各投稿カードに /posts/{id} への「詳細を見る」リンクがある。
 - いいねボタン、いいね数、投稿日時と干渉しない。
 - モバイル幅でもアクション領域が崩れない。

 TDD:
 - 一覧画面の HTML に投稿詳細 URL と「詳細を見る」が含まれるテストを書く。

 ### 4. フォームの入力支援とアクセシビリティ改善

 JavaScriptなしで、新規投稿・編集フォームの入力制約とエラー表示を分かりやすくしてください。

 受入基準:
 - 投稿者名は必須・30文字以内であることが画面上で分かる。
 - 本文は必須・280文字以内であることが画面上で分かる。
 - 入力欄に aria-describedby を付け、補足文またはエラー文と関連付ける。
 - バリデーションエラー時、エラー文が入力欄直下に表示される。
 - 既存の Bean Validation を壊さない。

 TDD:
 - 新規投稿画面に必須・文字数上限の補足テキストが出るテストを書く。
 - バリデーションエラー時にエラー文が表示される既存テストがあれば維持し、必要なら aria-describedby の検証を追加す
 る。
 - 編集画面にも本文の補足テキストが出るテストを書く。

 ### 5. 削除確認画面の危険操作表現を改善

 削除確認画面を、誤操作しにくい見た目と文言にしてください。

 受入基準:
 - 削除対象の投稿が明確に表示される。
 - 「この操作は取り消せません」または同等の注意文が表示される。
 - キャンセル導線が明確にある。
 - 削除実行ボタンは danger 用 class を持つ。
 - CSRF トークンは維持する。

 TDD:
 - 削除確認画面に注意文、キャンセルリンク、danger class が含まれるテストを書く。
 - 削除 POST の既存挙動が壊れていないことを確認する。

 ### 6. 編集済み表示

 PostView の updatedAt を使い、編集済み投稿には更新日時または「編集済み」表示を出してください。

 受入基準:
 - updatedAt が null でない投稿には「編集済み」または更新日時が表示される。
 - updatedAt が null の投稿には編集済み表示を出さない。
 - 一覧画面と詳細画面の両方で表示する。
 - 日時フォーマットは既存の createdAt と合わせる。

 TDD:
 - updatedAt ありの投稿で一覧に編集済み表示が出るテストを書く。
 - updatedAt なしの投稿では編集済み表示が出ないテストを書く。
 - 詳細画面でも同様に検証する。

 ### 7. ボタン・リンクの役割別スタイル整理

 CSSで primary / secondary / danger / ghost 相当のボタンスタイルを整理し、テンプレート側で使い分けてください。

 受入基準:
 - 投稿・更新など主操作は primary。
 - 検索解除・戻る・キャンセルなど補助操作は secondary または ghost。
 - 削除実行は danger。
 - 既存の button / app-nav / post__actions と矛盾しない。
 - モバイルでボタンテキストがはみ出さない。

 TDD:
 - CSS に button--danger など必要 class が存在することを確認するテストが既にある場合は追加する。
 - テンプレートに各 class が適用されていることを Controller Feature Test で検証する。

 ## 実装順序

 1. 既存テストを確認し、UI関連の Feature Test の書き方を把握する。
 2. 改善 1 から順に、1改善ごとに RED → GREEN → REFACTOR を回す。
 3. 各改善の差分が大きくなりすぎる場合は、テストと実装を小さく分割する。
 4. 最後に ./mvnw -B -Ph2 verify を実行する。
 5. 変更したファイル、追加したテスト、確認結果を日本語で簡潔に報告する。

 ## 完了条件

 - 上記 1〜7 の改善が実装されている。
 - JavaScript と外部CDNを追加していない。
 - th:utext を使っていない。
 - ./mvnw -B -Ph2 verify が成功している。
 - 失敗した場合は、原文エラーと日本語要約、次に見るべきファイル/行を報告する。

```

**結果**: 効いた
主な変更

  - 全投稿画面に共通ヘッダー / ナビを適用
    src/main/resources/templates/posts/detail.html, src/main/resources/templates/posts/form.html, src/main/
    resources/templates/posts/edit.html, src/main/resources/templates/posts/delete-confirm.html

  - 一覧に検索中表示・検索解除リンク・「詳細を見る」導線・編集済み表示を追加
    src/main/resources/templates/posts/list.html

  - 新規投稿 / 編集フォームに入力補足と aria-describedby を追加
  - 削除確認画面に取り消し不可の注意文、dangerボタン、明確なキャンセル導線を追加
  - primary / secondary / danger / ghost 系ボタンと各UI状態のCSSを追加
    src/main/resources/static/css/app.css

  - TDD用の受入テストを既存Feature Testへ追加
    src/test/java/com/example/tsubuyaki/controller/PostListFeatureTest.java, src/test/java/com/example/tsubuyaki/
    controller/PostDetailFeatureTest.java, src/test/java/com/example/tsubuyaki/controller/
    PostNewFormFeatureTest.java, src/test/java/com/example/tsubuyaki/controller/PostEditFeatureTest.java, src/test/
    java/com/example/tsubuyaki/controller/PostDeleteFeatureTest.java

  確認結果

  - ./mvnw -B -Ph2
    -Dtest=PostListFeatureTest,PostDetailFeatureTest,PostNewFormFeatureTest,PostEditFeatureTest,PostDeleteFeatureTe
    st test 成功

  - ./mvnw -B -Ph2 verify 成功
  - rg で <script> / onsubmit / th:utext / CDN追加なしを確認済み
  
  - コミット: 1ac32ab feat(ui): 投稿画面のUI/UXを改善する

**振り返り**:
UI/UXの改善が実装出来た。
ソースについても、当該箇所の実装、WEBアプリも動いていることが確認出来た。
「順序」に記載の手順にて、TDDを用いた実装が走ることを確認した。
また、Codexと壁打ちし、仕様の詳細化を実施した


## プロンプト 16

**フェーズ**:

**プロンプト本文**:

```

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**:



## プロンプト 16

**フェーズ**:

**プロンプト本文**:

```

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**: