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

**フェーズ**:M4: 投稿詳細 GET /posts/{id} 用スクリプト

**プロンプト本文**:

```
Spring Boot（Java）で、投稿詳細表示機能を実装してください。

制約:
- GET /posts/{id} をハンドリングするコントローラメソッドを作成すること
- ビュー posts/detail.html を返すこと
- 指定された id の Post が存在しない場合は 404 を返すこと
- 追加の機能（編集リンク、コメント表示など）は実装しないこと

テスト:
- 正常系: 存在する id を指定したとき、200 が返り、model に該当 Post が設定されること
- 異常系: 存在しない id を指定したとき、404 が返ること
```

**結果**:  部分的に効いた 

• 実装しました。

  差分

  - src/main/java/com/example/tsubuyaki/controller/PostController.java:31: GET /posts/{id} を追加し、存在しない場合は
    404 を返すようにしました。

  - src/main/java/com/example/tsubuyaki/service/PostService.java:26: findById を追加しました。
  - src/main/resources/templates/posts/detail.html:1: posts/detail.html を追加しました。追加機能は入れず、投稿者・本文・
    投稿日時のみ表示します。

  - src/test/java/com/example/tsubuyaki/controller/PostControllerTest.java:93: 正常系と 404 系の Controller テストを追加
    しました。

  検証

  - ./mvnw -B -Ph2 -Dtest=PostControllerTest test 成功
  - ./mvnw -B -Ph2 verify 成功

  補足: 作業前から作業対象外の未コミット変更が複数ありました。今回の変更対象には触れていません。

**振り返り**:

実装後起動時に「Whitelabel Error Page」が出てしまった。
AIへの命令時に created_at が Oracle 側ではタイムゾーンなし日時として扱われる一方、Java 側が Instant でタイムゾーン前提の型になっている点を考慮できていなかったことが原因
エラー修正をAIに投げた後正常に動くこと確認済み

---

## プロンプト 3

**フェーズ**:M5: ヘルスチェック確認用スクリプト

**プロンプト本文**:

```
Spring Boot Actuator のヘルスチェックが壊れていないことを確認するためのテストを作成してください。

制約:
- 既に /actuator/health が有効になっている前提で、追加の設定は行わないこと
- WebTestClient または MockMvc を用いて /actuator/health に GET し、
  ステータス 200 と、レスポンス JSON に "status": "UP" が含まれることを確認するテストを作成すること
- Actuator の他のエンドポイントは触らないこと

テスト:
- /actuator/health が 200 かつ status=UP を返すことを確認する単一テスト
```

**結果**: 効いた 

• Actuator のヘルスチェック確認テストを追加しました。

  追加ファイル:

  - src/test/java/com/example/tsubuyaki/ActuatorHealthTest.java:15

  内容は MockMvc で GET /actuator/health を実行し、200 OK と JSON の $.status == "UP" だけを確認する単一テストです。追加
  設定や他の Actuator エンドポイント変更はしていません。

  検証:

  - ./mvnw -B -Ph2 -Dtest=ActuatorHealthTest test 成功
  - ./mvnw -B -Ph2 verify 成功

  作業前からある setup 系スクリプトの未コミット変更には触れていません。

**振り返り**:

テストケースも問題なく起動できておりJSONテストも「$.status == "UP"」が出ること確認済み

---

---

## プロンプト 4 

**フェーズ**:S1: いいね 用スクリプト

**プロンプト本文**:

```
Spring Boot（Java）で、投稿に対する「いいね」トグル機能を実装してください。

制約:
- POST /posts/{id}/likes をハンドリングするコントローラメソッドを作成すること
- clientHash は「IP + User-Agent」を連結した文字列を SHA-256 でハッシュし、先頭 8 文字を使用すること
- 同じ clientHash が同じ投稿に対して再度リクエストした場合は、いいねを解除するトグル動作にすること
- 詳細画面（posts/detail.html）で、いいね数と Like ボタンを表示できるようにすること
- 追加の機能（ユーザ認証、複雑なレート制限など）は実装しないこと

テスト:
- Service テスト: 初回リクエストでいいねが追加され、2回目の同じ clientHash で解除されること
- Controller テスト: POST /posts/{id}/likes で適切に Service が呼ばれ、リダイレクトまたは詳細画面に戻ること
- いいね数が正しくカウントされることを確認するテスト

```

**結果**: 効いた 

• 実装しました。

  差分

  - src/main/java/com/example/tsubuyaki/domain/PostLike.java:14: post_likes 用 Entity を追加。
  - src/main/java/com/example/tsubuyaki/repository/PostLikeRepository.java:8: postId + clientHash 検索と投稿別カウントを
    追加。

  - src/main/resources/db/migration/V2__create_post_likes.sql:6: post_likes テーブル、sequence、外部キー、一意制約を追
    加。

  - src/main/java/com/example/tsubuyaki/service/PostService.java:35: countLikes と toggleLike を追加。初回は追加、同じ
    clientHash の再リクエストは削除します。

  - src/main/java/com/example/tsubuyaki/controller/PostController.java:63: POST /posts/{id}/likes を追加。IP + User-
    Agent を SHA-256 化し、先頭 8 文字を Service に渡して、/posts/{id} へリダイレクトします。

  - src/main/resources/templates/posts/detail.html:24: いいね数と Like ボタンを追加。_csrf がある場合だけ hidden token
    も出します。

  テスト

  - src/test/java/com/example/tsubuyaki/service/PostServiceTest.java:71: 同じ clientHash の 1 回目で追加、2 回目で解除を
    確認。

  - src/test/java/com/example/tsubuyaki/controller/PostControllerTest.java:123: POST 時に計算済み clientHash で Service
    が呼ばれ、詳細へ戻ることを確認。

  - src/test/java/com/example/tsubuyaki/repository/PostLikeRepositoryTest.java:27: いいね数が投稿別に正しくカウントされ
    ることを確認。

  検証

  - ./mvnw -B -Ph2 -Dtest=PostServiceTest,PostControllerTest,PostLikeRepositoryTest test 成功
  - ./mvnw -B -Ph2 verify 成功

  作業対象外の setup 系スクリプトと education/prompts-i-used.md の未コミット変更には触れていません。

**振り返り**:

テストケースも問題なく起動できておりJSONテストも「$.status == "UP"」が出ること確認済み

---



