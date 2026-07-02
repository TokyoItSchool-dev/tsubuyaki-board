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

```

**結果**: 効いた / 部分的に効いた / 効かなかった

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

---

## プロンプト 1

**フェーズ **: M1 投稿一覧機能

**プロンプト本文**:

```
# タスク: M1 投稿一覧機能 (GET /posts) の TDD 実装

Spring Boot (Java) と Thymeleaf を使用して、以下の仕様を満たす「投稿一覧表示機能」を実装してください。
まずは提示したテストケースを満たすテストコードを実装し、その後、テストをパスする最小限の製品コードを構築します。

## 1. 満たすべき仕様 (受入基準)

### A. バックエンド (Controller / Service / Repository)
- **エンドポイント**: `GET /posts`
- **データ取得ルール**: 
  - データベースから投稿データを取得する際、**新着順 (`created_at DESC`)** でソートすること。
  - 取得件数は **最大 50 件** に制限すること（51件以上あっても50件のみ返す）。
- **Modelへの格納**: 
  - `model` の `posts` 属性 (`model.addAttribute("posts", ...)`）に、取得した投稿の `List<Post>` を格納すること。
- **画面遷移**: 
  - 遷移先として `posts/list`（Thymeleafテンプレート）を返すこと。

### B. フロントエンド (View: `src/main/resources/templates/posts/list.html`)
- **0件時の表示**: 投稿が 0 件の場合、画面に「まだ投稿はありません」という文言を表示する。
- **データ存在時の表示**: 
  - 投稿が存在する場合、各投稿を **「投稿者」「内容」「投稿日」の順** に表示する。
  - 「内容」のテキストは、画面幅に応じて **適度な文字数で自動的に折り返す** ようにCSS（`word-break: break-all;` や `white-space: pre-wrap;` など）を適切に適用すること。
- **更新ボタン**: 
  - 画面内に「更新」ボタンを設けること。
  - ボタンを押下した際、`posts/` (または `/posts`) にリクエストを送り、ページがリロードされる挙動にすること。

## 2. 必須テストケース (最初に実装すること)
製品コードを実装する前に、以下の4つの観点を検証する統合テスト（MockMvc等を使用）を作成してください。

1. **0件時の挙動テスト**: 投稿が0件の場合に、画面内に「まだ投稿はありません」という文言が含まれていること。
2. **50件上限のテスト**: データベースに51件以上の投稿が存在する場合、画面（またはModel内）に新着の50件しか含まれていないこと。
3. **更新ボタンの検証**: 画面内に更新ボタンが存在し、その遷移先またはアクションが `posts/` へのリクエストになっていること。
4. **表示順・項目の検証**: 取得された投稿が、画面上で「投稿者」「内容」「投稿日」の順番で正しく配置・表示されていること。

## 3. 進め方の指示
1. まず、上記「2. 必須テストケース」を満たすテストコードのモックまたは実装案を提示してください。
2. テストコードの合意が取れたら、それをパスするための最小限の製品コード（Repository -> Service -> Controller -> HTML）をステップ・バイ・ステップで実装してください。一気にすべてのコードを書き換えず、確認を取りながら進めてください。
```

**結果**: 効いた 


**振り返り**:
実際の一覧画面に投稿内容の表示確認はできていない(未実装のため)。

---

## プロンプト 2

**フェーズ**: M2 投稿作成フォーム表示機能

**プロンプト本文**:

```
# タスク: M2 投稿作成フォーム表示機能 (GET /posts/new) の TDD 実装

  Spring Boot (Java) と Thymeleaf を使用して、以下の仕様を満たす「投稿作成フォーム画面の表示機能」を実装してください。
  まずは提示したテストケースを満たすテストコードを実装し、その後、テストをパスする最小限の製品コードを構築します。
  ※なお、投稿の登録処理（POST /posts）は別タスクとするため、今回は含めません。

  ### A. バックエンド (Controller)
  - **エンドポイント**: `GET /posts/new`
  - **フォームオブジェクトの準備**:

  ### B. フロントエンド (View: `src/main/resources/templates/posts/form.html`)
  - **入力フォームの配置**:
    - 画面内に「投稿者（author）」と「内容（content）」を入力するための `<form>` 要素を配置すること。
    - 各入力項目は、バックエンドから渡された `postForm` オブジェクトのプロパティと正しくバインド（`th:field` 等を使用）できるように構築すること。
  - **戻るリンク**:
    - フォーム画面内から、投稿一覧画面（`/posts`）へ戻るためのリンク（`<a>` タグ等）またはボタンを設けること。

  ## 2. 必須テストケース (最初に実装すること)
  製品コードを実装する前に、以下の2つの観点を検証する統合テスト（MockMvc等を使用）を作成してください。

  1. **画面表示およびModelの検証**:
     - `GET /posts/new` にアクセスした際、ステータス200でフォーム画面が返ってくること。
     - 遷移先のビュー名が `posts/form` であること。
     - Modelに空のFormオブジェクト（`postForm`）が正しく設定されていること。
  2. **HTML要素の存在検証**:
     - 表示されたHTML内に、「投稿者」および「内容」の入力項目（`<input>` や `<textarea>` 等）が存在すること。
     - 投稿一覧（`/posts`）へ戻るための正確なリンク（`href="/posts"` など）が存在すること。

  ## 3. 進め方の指示
  1. まず、上記「2. 必須テストケース」を満たすテストコードの実装案を提示してください。
  2. テストコードの合意が取れたら、それをパスするための最小限の製品コード（Formクラス -> Controller -> `posts/form.html` テンプレートの枠組み）をステッ
  プ・バイ・ステップで実装してください。
```

**結果**: 効いた 


**振り返り**:
 現状の PostForm は body プロパティだが、プロンプトにて content としてしまったため、
 不要な差分が発生したため、元のbodyへ戻した。

---

## プロンプト 3

**フェーズ**: M3: 投稿登録

**プロンプト本文**:

```
# タスク: M3 投稿登録機能 (POST /posts) の TDD 実装

Spring Boot (Java) と Thymeleaf を使用して、以下の厳密な仕様を満たす「投稿登録機能」を実装してください。
すでに実装済みの M2 で定義した `PostForm`（プロパティ: `author`, `body`）を活用し、まずは仕様を満たすテストケースを構築してから、製品コードの実装に進みます。

## 1. 満たすべき仕様 (受入基準)

### A. バックエンド (Controller / Service / Repository)
- **エンドポイント**: `POST /posts`
- **引数の受け取り**: `PostForm` オブジェクトを `@ModelAttribute` 経由で受け取り、同時にバリデーション（`@Valid` または `@Validated`）を実行すること。
- **バリデーションルール (厳密)**:
  - **投稿者 (`author`)**: `1文字以上、30文字以下` であること。かつ、**空白（スペース）のみの入力はNG** とすること。
  - **内容 (`body`)**: `1文字以上、280文字以下` であること。かつ、**空白（スペース）のみの入力はNG** とすること。
  - ※実装時は `@NotBlank`, `@Size(min = 1, max = 30)`, `@Size(min = 1, max = 280)` などのアノテーションを適切に使用してください。
- **正常系（成功時）の処理挙動**:
  - バリデーションを通過した場合、データをデータベースに保存すること。
  - 保存完了後は、**HTTPステータス「302（Found）」** で、**投稿一覧画面（`/posts`）へリダイレクト（PRGパターン）** させること。
- **異常系（失敗時）の処理挙動**: 
  - 入力値にエラーがある（`BindingResult.hasErrors()` が true）場合は、データベースへの保存を行わないこと。
  - **HTTPステータス「200（OK）」** を返し、投稿作成画面のビュー（**`posts/form`**）を再表示すること。

### B. フロントエンド (View: `src/main/resources/templates/posts/form.html` の修正)
- **エラーメッセージの表示**: 
  - バリデーションエラーが発生して画面が再表示された際、`author` および `body` の各入力項目の近くに、エラーメッセージが日本語で表示されるように Thymeleaf の `th:errors` などを配置すること。

## 2. 必須テストケース (最初に実装すること)
製品コードを実装・修正する前に、以下の観点を検証する統合テスト（MockMvc等を使用）を作成してください。

1. **正常系登録・リダイレクトの検証 (302)**: 
   - `POST /posts` に有効なパラメータ（例: 境界値である1文字の入力や、上限以内の文字列）を送信した際、**HTTPステータスが 302** であること。
   - リダイレクト先のURLが `/posts` であること。
2. **異常系（バリデーションエラー）の検証 (200)**: 
   - 以下のケースを送信した際、**HTTPステータスが 200** で返り、遷移先のビュー名が `posts/form` であること。また、`BindingResult` にエラーが含まれていること。
     - `author` が空、または空白スペースのみ、または31文字以上
     - `body` が空、または空白スペースのみ、または281文字以上

## 3. 進め方の指示
1. まず、上記「2. 必須テストケース」を満たすテストコードの実装案（MockMvcの `.andExpect(status().isFound())` や `isOk()` などのアサーションを含む）を提示してください。
2. テストコードの合意が取れたら、それをパスするための最小限の製品コードをステップ・バイ・ステップで実装・修正してください。
```

**結果**: 効いた

```
• M3投稿登録機能をTDDで実装しました。

  追加・変更内容:

  - 追加: src/test/java/com/example/tsubuyaki/controller/PostCreateIntegrationTest.java
      - 正常系: POST /posts が 302 で /posts へリダイレクトし、DB保存されること
      - create(PostForm) を追加し、Post を保存

  - 変更: src/main/java/com/example/tsubuyaki/controller/PostController.java
      - @PostMapping("/posts") を追加

  検証:

  - ./mvnw -B -Ph2 clean verify
      - Tests: 19件成功
      - Checkstyle: 0件
      - SpotBugs: 0件
  - 実HTTP確認
      - 正常POST: 302 http://localhost:8080/posts
      - 異常POST: 200、エラーメッセージ表示
      - 一覧に登録内容が表示されることを確認済み

  確認用サーバーは停止済みです。セットアップ系ファイルの既存変更は今回触っていません。
```

**振り返り**:
 投稿実行時、Oracleとの日付型が合わないエラーが発生。エラー内容をCodexに展開し、修正した。
 OracleはTIMESTAMP(6)、Javaはjava.time.Instant となっていたため、java.time.LocalDateTimeに修正

---

## プロンプト 4

**フェーズ**: M4: 投稿詳細

**プロンプト本文**:

```
# タスク: M4 投稿詳細機能 (GET /posts/{id}) の TDD 実装

Spring Boot (Java) と Thymeleaf を使用して、以下の仕様を満たす「投稿詳細表示機能」を実装してください。
テスト駆動開発 (TDD) のアプローチに則り、まずは仕様を満たすテストケースを構築し、それをパスする最小限の製品コードを段階的に構築します。

また、今回はコードの理解を深めるため、**バックエンドおよびフロントエンドの修正箇所・新規実装箇所には、処理内容が明確にわかる日本語のコメントを必ず記述**してください。

## 1. 満たすべき仕様 (受入基準)

### A. バックエンド (Controller / Service / Repository)
- **エンドポイント**: `GET /posts/{id}`（`{id}` は各投稿のプライマリキー）
- **正常系の挙動 (データが存在する場合)**:
  - パス変数（Path Variable）から `id` を受け取り、データベースから該当する投稿データを1件取得すること。
  - `model` の `post` 属性 (`model.addAttribute("post", ...)`）に、取得した `Post` エンティティを格納すること。
  - 遷移先のビューとして **`posts/detail`**（Thymeleafテンプレート: `posts/detail.html`）を返すこと。
- **異常系の挙動 (指定された id が存在しない場合)**:
  - データベースに該当する `id` の投稿が存在しない場合は、**HTTPステータス「404 (Not Found)」** をブラウザに返すこと。
  - ※カスタム例外（例: `ResourceNotFoundException`）をスローし、`@ResponseStatus(HttpStatus.NOT_FOUND)` や `@ControllerAdvice` でハンドリングする実装を推奨します。

### B. フロントエンド (View: `src/main/resources/templates/posts/detail.html`)
- **詳細情報の表示**: 
  - 画面内に、対象の投稿の「投稿者（author）」「内容（body）」「投稿日（created_at）」を表示すること。
  - 「内容」は M1 同様、画面幅に応じて適度な文字数で自動的に折り返すようにCSSを適切に適用すること。
- **戻るリンク**: 
  - 詳細画面内から、投稿一覧画面（`/posts`）へ戻るためのリンク（`<a>` タグ等）を設けること。

### C. 実装コードへの記述ルール (必須)
- 新しく追加・修正するJavaコード（Controller, Service, Repository等）およびHTMLテンプレート内の処理ブロックには、**どのような意図や処理を行っているかが一目でわかる日本語コメント**を必ず含めてください。

## 2. 必須テストケース (最初に実装すること)
製品コードを実装・修正する前に、以下の2つの観点を検証する統合テスト（MockMvc等を使用）を作成してください。

1. **正常系（データ存在時）の検証 (200)**: 
   - 存在する `id`（例: `1`）を指定して `GET /posts/1` にアクセスした際、**HTTPステータスが 200 (OK)** であること。
   - 遷移先のビュー名が `posts/detail` であること。
   - Modelに指定した投稿データ（`post`）が正しく格納されていること。
2. **異常系（データ不在時）の検証 (404)**: 
   - 存在しない `id`（例: `999`）を指定して `GET /posts/999` にアクセスした際、**HTTPステータスが 404 (Not Found)** で返ってくること。

## 3. 進め方の指示
1. まず、上記「2. 必須テストケース」を満たすテストコードの実装案（MockMvcの `.andExpect(status().isOk())` や `isNotFound()` などのアサーション、および日本語コメントを含む骨組み）を提示してください。
2. テストコードの合意が取れたら、それをパスするための最小限の製品コードをステップ・バイ・ステップで実装・修正してください。すべての修正箇所に分かりやすい日本語コメントが入っていることを確認しながら進めます。
```

**結果**: 効いた 

```
• 実装しました。M4の GET /posts/{id} は、存在する投稿なら posts/detail に model.post を渡し、存在しないIDなら PostNotFoundException 経由で404を返します。

  変更箇所:

  - src/test/java/com/example/tsubuyaki/controller/PostDetailIntegrationTest.java:27: 200正常系、404異常系、表示内容、一覧戻りリンクを検証
  - src/main/java/com/example/tsubuyaki/controller/PostController.java:35: GET /posts/{id} を追加
  - src/main/java/com/example/tsubuyaki/service/PostService.java:26: ID検索と未存在時例外を追加
  - src/main/java/com/example/tsubuyaki/service/PostNotFoundException.java:6: 404返却用例外を追加
  - src/main/resources/templates/posts/detail.html:1: 投稿者、本文、投稿日、一覧リンクを表示

  検証:

  - RED確認: /posts/{id} 未実装時に Status expected:<200> but was:<404> で失敗することを確認。
  - GREEN確認: ./mvnw -Dtest=PostDetailIntegrationTest -DforkCount=0 test は BUILD SUCCESS、2件成功。
  - 全体確認: ./mvnw -B -Ph2 -DforkCount=0 verify は BUILD SUCCESS、21件成功、Checkstyle違反0、SpotBugs警告0。
```

**振り返り**:
 /posts/{id}で投稿詳細画面が表示され、存在しないidで404となることを確認した。
 404の場合はエラー画面が表示されることが望ましいが、現時点では未実装。

---

## プロンプト 5

**フェーズ**: S1: いいね

**プロンプト本文**:

```
/plan 「S1: いいね」機能をTDDサイクルで実装する計画を作ってください。
@education/EXERCISES.md の「S1: いいね」要件、および @AGENTS.md の開発規約を受入基準とします。
まず、作成予定のテーブル定義やハッシュ生成ロジックのコンポーネント配置などの実装計画を組み立ててください。

### 1. 求める機能（ゴール）
- `POST /posts/{id}/likes` へのリクエストで、いいねの状態をトグル（登録/解除）させてください。
  - **成功時の挙動**: トグル処理が完了した後は、**HTTPステータス「302（Found）」** で、元の投稿詳細画面（`redirect:/posts/{id}`）へリダイレクトさせてください。
- 同一の clientHash が再度ボタンを押した場合はいいねを解除します。
  - ※ clientHash 仕様: 接続元の「IPアドレス + User-Agent」を SHA-256 でハッシュ化した文字列の先頭 8 文字。
  - バックエンドでは `HttpServletRequest` からこれらを取得してロジックを組み立ててください。
- 投稿詳細画面（@src/main/resources/templates/posts/detail.html）に、現在のいいね総数と、いいねをリクエストする Like ボタン（フォーム）を表示してください。
- 存在しない id へのリクエストは 404 としてください。

### 2. 実装の制約・手順
- **データモデル拡張**: いいね情報を永続化するため、新しい Flyway マイグレーション DDL（例: src/main/resources/db/migration/V2__create_likes.sql）を H2/Oracle 両対応で作成し、対応する JPA Entity、Repository を作成してください。
- **アーキテクチャ階層**: Controller -> Service -> Repository -> Entity の一方向依存、および Constructor Injection を徹底してください。
- **TDDサイクル**: 最初に @WebMvcTest や @DataJpaTest を用いて、期待する挙動が失敗するテスト（RED）を1本書き、それを GREEN にする最小実装を行い、最後にリファクタリング（REFACTOR）を行ってください。
  - Controllerのテストでは、MockMvcでリモートIPアドレスや `User-Agent` ヘッダーを擬似的に設定し、ハッシュ生成ロジックが正しく検証できるテストケースを含めてください。
- **セキュリティ**:
  - Thymeleaf テンプレートでは th:utext を絶対に使わず、th:text を使用してください。
  - Like ボタンのフォームには必ず Spring Security の CSRF トークン（_csrf）を含めてください。
  - クエリ発行時は SQL インジェクション対策として必ずバインド変数を使用してください。
- **コードコメント**: 今回はコードの理解を深めるため、**バックエンドおよびフロントエンドの修正箇所・新規実装箇所には、処理内容が明確にわかる日本語のコメントを必ず記述**してください。

### 3. 完了条件
- `./mvnw -B -Ph2 -Pcoverage-day3 -Pstrict verify` を実行し、BUILD SUCCESS になること（カバレッジ 80% 以上を維持）。
- Repository / Service / Controller それぞれに1本以上のテストが存在すること。
```

**結果**: 効いた 

```
• S1「いいね」機能を GREEN まで実装しました。

  主な追加・変更:

  - Like 永続化: src/main/resources/db/migration/V2__create_post_likes.sql:1, src/main/java/com/example/tsubuyaki/domain/
    PostLike.java:1, src/main/java/com/example/tsubuyaki/repository/PostLikeRepository.java:1

  - Controller / 詳細画面: src/main/java/com/example/tsubuyaki/controller/PostController.java:1, src/main/resources/templates/
    posts/detail.html:1

  - Controller: src/test/java/com/example/tsubuyaki/controller/PostLikeIntegrationTest.java:1
  - Repository: src/test/java/com/example/tsubuyaki/repository/PostLikeRepositoryTest.java:1
  - Service: src/test/java/com/example/tsubuyaki/service/PostLikeServiceTest.java:1
  - clientHash: src/test/java/com/example/tsubuyaki/service/ClientHashGeneratorTest.java:1

  確認結果:

  ./mvnw -B -Ph2 -Pcoverage-day3 -Pstrict verify

  結果は BUILD SUCCESS です。テストは 30件成功 / 失敗0 / エラー0、Checkstyle 違反 0、SpotBugs 0、JaCoCo も All coverage checks
  have been met. まで確認済みです。

---
• 進めてセルフレビューまで確認しました。追加修正は不要です。

  確認済み:

  - th:utext なし
  - 文字列連結の @Query / native SQL なし
  - Like フォームに _csrf あり
  - Controller → Service → Repository → Entity の依存方向維持
  - Repository / Service / Controller テストあり
  - ./mvnw -B -Ph2 -Pcoverage-day3 -Pstrict verify は BUILD SUCCESS
```

**振り返り**:
 /posts/{id}/likes 投稿詳細画面のLikeボタン押下で「いいね」が増減し、
 別ブラウザからの押下で、増加することを確認した。

---



