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

---------

## プロンプト 2

**フェーズ**:
レビュー用プロンプトの作成

**プロンプト本文**:

```
あなたはSpring Boot開発のシニアエンジニアです。

以下のプロジェクトのコードレビューをしてください。

対象ディレクトリ
src/

重点的に確認してほしい内容

① レイヤーの責務
Controller
Service
Repository
Entity
Domain
DTO

それぞれが本来の責務になっているか確認してください。

例
・Controllerに業務ロジックを書いていないか
・ServiceがDBアクセスしていないか
・RepositoryがSQL以外の責務を持っていないか
・Entityに画面用の処理を書いていないか
・DTOとEntityの責務が混ざっていないか
・DomainがEntityやDTOの代わりになっていないか

② MVC設計
Controller → Service → Repository の依存関係が守られているか確認してください。

逆方向の依存や不適切な依存があれば指摘してください。

③ Spring Bootのベストプラクティス
以下も確認してください。

・DI（コンストラクタインジェクション）が適切か
・@Transactional の付け方
・命名規則
・例外処理
・Optionalの使い方
・バリデーション
・Bean Validation
・Mapperの責務
・HTTPステータス
・Redirectの使い方

④ Entity
JPA Entityとして適切か確認してください。

・@Id
・@GeneratedValue
・equals/hashCode
・Lombokの使い方
・コンストラクタ
・setterの公開範囲

⑤ DTO
DTOが画面との受け渡し専用になっているか確認してください。

・Entityをそのまま画面へ返していないか
・Form DTOとResponse DTOが適切か

⑥ Repository
Spring Data JPAとして適切か確認してください。

・メソッド名
・JPQL
・戻り値
・Optionalの利用

⑦ Service
Serviceにビジネスロジックが集約されているか確認してください。

ControllerやRepositoryへ責務が漏れていないか確認してください。

⑧ Controller
ControllerはHTTPの受け口だけになっているか確認してください。

・Modelへの格納
・リダイレクト
・Validation
・BindingResult

が適切か確認してください。

⑨ 保守性
以下も確認してください。

・重複コード
・可読性
・メソッド分割
・クラス分割
・SOLID原則
・Clean Architectureの観点

⑩ レビュー結果

問題点を

【重大】
【改善推奨】
【軽微】
【良い点】

に分けてください。

さらに

・なぜ問題なのか
・どう修正すべきか
・修正例

まで説明してください。

最後に100点満点で評価してください。
```

**結果**: 効いた / 部分的に効いた / 効かなかった
部分的に効いた

**振り返り**:
./mvnw -B -Ph2 verifyコマンドでJUnitのテスト結果確認時にプロンプトを流す前後で70％代から80％代にテスト結果が向上した。
ただ、節目で実行していたところ、実行→改善推奨と軽微な指摘の部分を修正を行っていたが、再度プロンプトを実行すると、別の改善推奨と軽微な指摘表示されるため、いたちごっこになったので、内容の修正が必要。

---------

## プロンプト 3

**フェーズ**:
M1: 投稿一覧
M2: 投稿作成フォーム

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

**結果**: 効いた / 部分的に効いた / 効かなかった
効いた

**振り返り**:
環境構築時に、プロンプトを流してしまったため、前後比較が出来なかったが、一覧作成が出来ていること、投稿作成フォームが作成出来ていること、挙動が想定通りだったため。
---------

## プロンプト 4

**フェーズ**:
M3: 投稿登録

**プロンプト本文**:

```
POST /posts による投稿登録機能を、TDD（テスト駆動開発）で実装してください。

要件
- POST /posts で投稿を登録できること
- 入力フォームは PostForm を使用すること
- バリデーションを実装すること
  - author：1～30文字
  - body：1～280文字
  - author、body ともに空白文字のみは入力不可
- バリデーション成功時
  - 投稿を保存すること
  - HTTPステータス302で /posts へリダイレクトすること
- バリデーション失敗時
  - HTTPステータス200を返すこと
  - posts/form を再表示すること
  - バリデーションエラーを画面に表示できること

実装手順
1. @WebMvcTest と MockMvc を使用し、失敗するテストを書く（RED）
   - 正常入力で302リダイレクトになること
   - author未入力でバリデーションエラーになること
   - body未入力でバリデーションエラーになること
   - authorが31文字以上でエラーになること
   - bodyが281文字以上でエラーになること
   - 空白のみの入力でエラーになること
   - エラー時は posts/form が表示され、model にエラー情報が含まれること

2. テストを通す最小限の実装を行う（GREEN）

3. 重複コードや命名を整理する（REFACTOR）

最後に
- ./mvnw -B -Ph2 test を実行し、すべてのテストが成功することを確認する
- Conventional Commits 形式でコミットする
  例:
  feat(post): implement post creation with validation
```

**結果**: 効いた / 部分的に効いた / 効かなかった
効いた

**振り返り**:
実装が出来た。入力のチェックも問題なく動作している。
---------

## プロンプト 5

**フェーズ**:
M4: 投稿詳細

**プロンプト本文**:

```
M4: 投稿詳細画面（GET /posts/{id}）をTDDで実装してください。

【要件】
- GET /posts/{id} にアクセスすると投稿詳細画面を表示する。
- ビューは posts/detail.html を返す。
- PathVariable の id を受け取り、その投稿を取得する。
- 取得した投稿を Model に追加する。
- 存在しない id が指定された場合は HTTP 404(Not Found) を返す。

【実装方針】
必ず TDD（RED → GREEN → REFACTOR）の流れで進めてください。

① RED
- @WebMvcTest + MockMvc を使用した失敗する Controller テストを最初に作成してください。
- 正常系
  - GET /posts/{id}
  - status 200
  - view が posts/detail
  - Model に post が入っていること
- 異常系
  - 存在しない id の場合
  - status 404 を返すこと

② GREEN
テストが通る最小限の実装を行ってください。
必要に応じて
- Controller
- Service
- Repository
- Entity
を修正してください。

③ REFACTOR
テストが通った状態で、
- 重複コードの削除
- 命名の改善
- 可読性の向上
- 責務の見直し
を行ってください。

【設計ルール】
- Controller は HTTP の受け口だけにする。
- 業務ロジックは Service に置く。
- DBアクセスは Repository のみが担当する。
- Controller から Repository を直接呼ばない。
- コンストラクタインジェクションを使用する。
- Spring Boot のベストプラクティスに従う。

【完了条件】
- ./mvnw -B -Ph2 test が成功すること。
- 変更内容を Conventional Commits 形式で
  feat(post): add post detail page
  のようなコミットメッセージでコミットしてください。
```

**結果**: 効いた / 部分的に効いた / 効かなかった
部分的に効いた

**振り返り**:
一覧から遷移させられなかった。また、あいまいな表現でプロンプトを流したため、少し挙動とUIがおかしかったように思う。

---------

## プロンプト 6

**フェーズ**:
M5: ヘルスチェック（回帰確認）

**プロンプト本文**:

```
M5: ヘルスチェック（回帰確認）を実施してください。

【要件】
- GET /actuator/health にアクセスすると HTTP 200 を返すこと。
- レスポンスの status が "UP" であることを確認する。
- M1～M4までの実装によってヘルスチェックが壊れていないことを検証する。

【実施内容】

① Health Endpoint の確認
- GET /actuator/health にアクセスし、HTTP 200 が返ることを確認してください。
- レスポンス JSON の status が "UP" であることを確認してください。

期待値

HTTP 200

{
  "status": "UP"
}

② 回帰確認
以下の変更によって Health Endpoint が影響を受けていないことを確認してください。

- Controller
- Service
- Repository
- Entity
- DTO
- View

追加した実装が Spring Boot の起動や Bean の生成に悪影響を与えていないことを確認してください。

③ テスト実行
以下を実行し、すべて成功することを確認してください。

./mvnw -B -Ph2 test

④ アプリケーション起動確認
アプリケーションを起動し、

GET /actuator/health

が正常に応答することを確認してください。

⑤ レビュー
Health Check が失敗する原因となり得る問題がないか確認してください。

例えば

- Bean生成エラー
- DI設定ミス
- ComponentScan漏れ
- Circular Dependency
- Spring Boot起動失敗
- Actuator設定の誤り

などがないかレビューしてください。

【完了条件】
- /actuator/health が HTTP 200 を返す。
- status が "UP" である。
- ./mvnw -B -Ph2 test が成功する。
- M1～M4で追加した機能によってヘルスチェックが壊れていないことを確認する。
- 変更が必要な場合のみ修正を行い、変更内容を Conventional Commits 形式（例: `test(health): verify actuator health endpoint` または `fix(health): restore actuator health endpoint`）でコミットしてください。
```

**結果**: 効いた / 部分的に効いた / 効かなかった
効かなかった

**振り返り**:
回帰確認の意味がよくわからない状態で流してしまったため、効いているか効いていないかよくわかっていない。
---------

## プロンプト 7

**フェーズ**:
S1: いいね

**プロンプト本文**:

```
S1: いいね機能を TDD（RED → GREEN → REFACTOR）で実装してください。

## 要件

### 1. いいね登録・解除

エンドポイント

POST /posts/{id}/likes

を実装してください。

Likeボタンを押したときの動作は以下のとおりです。

- 初回は「いいね」を登録する
- 同じ利用者が再度押した場合は「いいね」を解除する（トグル動作）
- 完了後は投稿詳細画面（/posts/{id}）へリダイレクトする

---

### 2. 利用者の判定

ログイン機能は使用しません。

利用者は

clientHash

で識別してください。

clientHash は

- IPアドレス
- User-Agent

を連結し、

SHA-256

でハッシュ化した文字列の

先頭8文字

を使用してください。

例

clientHash = SHA256(ip + userAgent).substring(0, 8)

同じ clientHash の場合は同一ユーザーとして扱います。

---

### 3. トグル仕様

Like が存在しない

→ Like を登録

Like が存在する

→ Like を削除

つまり

押すたびに

OFF → ON → OFF → ON

となるようにしてください。

---

### 4. 投稿詳細画面

GET /posts/{id}

の画面を修正してください。

表示内容

- 投稿者
- 本文
- 投稿日時
- 現在のいいね数
- Likeボタン

例

投稿本文

♥ 15

[ Like ]

---

### 5. Repository

LikeRepository を作成してください。

必要に応じて以下のようなメソッドを実装してください。

- existsByPostIdAndClientHash(...)
- findByPostIdAndClientHash(...)
- countByPostId(...)
- delete(...)
- save(...)

---

### 6. Service

PostService または LikeService に

toggleLike(...)

を実装してください。

Controllerにはビジネスロジックを書かず、

Controller
→ Service
→ Repository

の責務を守ってください。

---

## TDD

### RED

まず失敗するテストを書いてください。

対象

- LikeControllerTest（または PostControllerTest）
- LikeServiceTest
- 必要に応じて RepositoryTest

確認内容

- 初回でLike登録
- 2回目で解除
- countが増減する
- POST後に詳細画面へリダイレクトする

---

### GREEN

最小限のコードでテストを成功させてください。

---

### REFACTOR

最後に

- 重複コード削除
- 命名改善
- メソッド分割
- 可読性向上
- 不要コード削除

を行ってください。

---

## 完了条件

以下を確認してください。

- ./mvnw -B -Ph2 test が成功する
- いいね登録できる
- 再度押すと解除される
- いいね数が正しく表示される
- 投稿詳細画面が正常表示される

最後に Conventional Commits 形式のコミットメッセージを提案してください。

例

feat(like): implement toggle like feature
```

**結果**: 効いた / 部分的に効いた / 効かなかった
効いた

**振り返り**:
操作確認は問題なく実施できた。また、プロンプトに記載されている確認内容を基に操作を行ったが、問題なく実施できたため。
---------

## プロンプト 8

**フェーズ**:
S2: キーワード検索

**プロンプト本文**:

```
S2: キーワード検索機能を TDD（RED → GREEN → REFACTOR）で実装してください。

## 要件

### 1. 検索機能

エンドポイント

GET /posts?q=xxx

を実装してください。

クエリパラメータ

q

を受け取り、投稿本文（body）を対象に部分一致（LIKE検索）を行ってください。

例

- /posts?q=Spring
- /posts?q=Boot
- /posts?q=Java

検索文字列を含む投稿のみ一覧表示してください。

---

### 2. 検索条件

- body を対象とした部分一致検索
- 大文字・小文字はデータベースの設定に従う
- q が未指定または空文字の場合は通常の一覧表示（最新50件）
- 検索結果が0件の場合でも正常に一覧画面を表示する

---

### 3. 一覧画面

既存の

posts/index.html

を再利用してください。

画面上部に検索フォームを追加します。

表示内容

- 検索ボックス
- 検索ボタン
- 投稿一覧

例

---------------------------------
検索: [____________] [検索]
---------------------------------

投稿一覧...

---

検索後も入力した検索キーワードが検索ボックスに表示されるようにしてください。

---

### 4. Controller

GET /posts

で

q

の有無を判定してください。

- q が空の場合
    → 最新50件取得
- q が指定されている場合
    → 検索結果を取得

Controllerには検索ロジックを書かず、

Controller
→ Service
→ Repository

の責務を守ってください。

---

### 5. Service

PostService に検索処理を実装してください。

例

searchPosts(String keyword)

必要に応じて一覧取得処理との共通化も検討してください。

---

### 6. Repository

検索用メソッドを追加してください。

例

findByBodyContainingOrderByCreatedAtDesc(...)

または

@Query を使用した LIKE 検索

どちらでも構いません。

---

## TDD

### RED

まず失敗するテストを書いてください。

対象

- PostControllerTest
- PostServiceTest
- 必要に応じて RepositoryTest

確認内容

- キーワード検索ができる
- q が空なら通常一覧になる
- 検索結果0件でも正常表示
- 検索文字列が画面へ保持される

---

### GREEN

最小限のコードでテストを成功させてください。

---

### REFACTOR

最後に

- 重複コード削除
- メソッド分割
- 命名改善
- 可読性向上
- Serviceの責務整理
- Repositoryの命名見直し

を行ってください。

---

## 完了条件

以下を確認してください。

- ./mvnw -B -Ph2 test が成功する
- キーワード検索が動作する
- q が未指定なら通常一覧になる
- 検索ボックスに入力値が保持される
- 検索結果0件でもエラーにならない
- 一覧画面を再利用している

最後に Conventional Commits 形式のコミットメッセージを提案してください。

例

feat(search): implement keyword search for posts
```

**結果**: 効いた / 部分的に効いた / 効かなかった
部分的に効いた

**振り返り**:
UIは問題なく作成されていたと思うが、検索結果の表示が1回目の実行時にキーワード検索できなかった。
---------

## プロンプト 9

**フェーズ**:
S3: 投稿者名フィールド拡張

**プロンプト本文**:

```
S3: 投稿者名フィールド拡張を TDD（RED → GREEN → REFACTOR）で実装してください。

## 要件

### 1. 投稿フォーム

投稿フォーム（GET /posts/new）を修正してください。

以下の入力項目を表示します。

- 投稿者名（author）
- アバター色（avatarColor）
- 投稿本文（body）

---

### 2. 投稿者名

投稿者名（author）は必須項目です。

バリデーション

- 必須
- 1〜30文字
- 空白のみはNG

バリデーションエラー時は投稿フォームを再表示し、エラーメッセージを表示してください。

---

### 3. アバター色

投稿フォームにアバター色の選択項目を追加してください。

簡易実装として、以下の色から選択できるようにします。

- RED
- BLUE
- GREEN
- YELLOW
- PURPLE
- ORANGE

実装方法は以下のいずれかで構いません。

- `<select>`
- ラジオボタン

デフォルト値を設定してください。

例

BLUE

---

### 4. データ保存

投稿作成時に

avatarColor

も保存してください。

必要に応じて以下を修正してください。

- Entity
- DTO（PostForm）
- Service
- Repository

---

### 5. 一覧画面

投稿一覧（GET /posts）では

- 投稿者名
- アバター色
- 投稿本文
- 投稿日時

を表示してください。

アバター色は簡易的に表示します。

例

🟦 藤井
Spring Bootを勉強しています。

または

色付きの丸（●）や四角（■）をCSSで表示しても構いません。

---

### 6. 投稿詳細画面

投稿詳細（GET /posts/{id}）でも

- アバター色
- 投稿者名
- 本文
- 投稿日時

を表示してください。

---

### 7. Controller

Controllerには業務ロジックを書かず、

Controller
→ Service
→ Repository

の責務を守ってください。

---

## TDD

### RED

まず失敗するテストを書いてください。

対象

- PostControllerTest
- PostServiceTest
- 必要に応じて RepositoryTest

確認内容

- 投稿者名必須
- 空白のみNG
- アバター色が保存される
- 一覧画面で表示される
- 詳細画面で表示される

---

### GREEN

最小限の実装でテストを成功させてください。

---

### REFACTOR

最後に

- 重複コード削除
- 命名改善
- メソッド分割
- DTO・Entityの責務整理
- 不要コード削除
- 可読性向上

を行ってください。

---

## 完了条件

以下を確認してください。

- ./mvnw -B -Ph2 test が成功する
- 投稿者名のバリデーションが正しく動作する
- アバター色が保存される
- 一覧画面でアバター色が表示される
- 投稿詳細画面でアバター色が表示される
- フォームから正常に投稿できる

最後に Conventional Commits 形式のコミットメッセージを提案してください。

例

feat(post): add avatar color and author validation
```

**結果**: 効いた / 部分的に効いた / 効かなかった
部分的に効いた

**振り返り**:
入力フォームのUIは問題なく作成されていたと思うが、一覧表示がおかしく、「色の文字」が表示されてしまったため、部分的だったと思う。
もう少しプロンプトを細かく記載していればよかった。
---------

## プロンプト 10

**フェーズ**:
S3: 投稿者名フィールド拡張
一覧の修正

**プロンプト本文**:

```
一覧画面（GET /posts）および投稿詳細画面（GET /posts/{id}）のアバター表示を修正してください。

## 現状

現在は、選択したアバター色が

RED
BLUE
GREEN

のように文字列として表示されています。

## 修正内容

アバター色の文字列は表示せず、選択した色を実際の色で表示してください。

例

● 藤井

または

⬤ 藤井

のように、丸いアイコン（または四角）を選択した色で表示します。

### 対応する色

- RED → 赤
- BLUE → 青
- GREEN → 緑
- YELLOW → 黄
- PURPLE → 紫
- ORANGE → オレンジ

## 実装方法

- Thymeleafで `avatarColor` に応じてCSSクラスを切り替える
- CSSで色を指定する
- `avatarColor` の文字列（RED、BLUEなど）は画面に表示しない
- 一覧画面・投稿詳細画面の両方を修正する

## 完了条件

- 色名（RED、BLUEなど）が画面に表示されない
- 選択した色の丸（または四角）のアイコンが表示される
- 既存のテストがすべて成功する（`./mvnw -B -Ph2 test`）
```

**結果**: 効いた / 部分的に効いた / 効かなかった
効いた

**振り返り**:
問題なく色の表示が出来るようになった。
---------

## プロンプト 11

**フェーズ**:
C1: タグ機能

**プロンプト本文**:

```
C1: タグ機能を TDD（RED → GREEN → REFACTOR）で実装してください。

## 要件

### 1. タグの抽出

投稿作成時（POST /posts）に、投稿本文（body）からハッシュタグを抽出してください。

例

```
今日は #SpringBoot を勉強しました。
#Java #Spring
```

抽出されるタグ

- SpringBoot
- Java
- Spring

### 抽出ルール

- `#` から始まる文字列をタグとして認識する
- 英数字・アンダースコア（_）をタグ名として扱う
- タグ名には `#` を含めない
- 同じ投稿内で重複するタグは1つとして扱う
- タグが存在しない投稿も登録できる

---

### 2. データベース

Tag テーブルを作成してください。

必要に応じて中間テーブル（PostTag など）を作成してください。

例

Tag

- id
- name

PostTag

- post_id
- tag_id

同じタグ名は重複登録しないようにしてください。

既存タグが存在する場合は再利用してください。

---

### 3. 投稿登録

投稿保存時に

- 投稿を保存
- タグを抽出
- Tag を登録（存在しなければ作成）
- 投稿とタグを関連付け

までを1つのトランザクションで実施してください。

---

### 4. タグ一覧

以下を実装してください。

GET /tags/{name}

指定したタグを持つ投稿一覧を表示します。

例

```
GET /tags/SpringBoot
```

表示内容

- 投稿者
- 本文
- 投稿日時
- タグ一覧

画面は既存の投稿一覧画面（posts/index.html）を再利用して構いません。

---

### 5. 一覧画面・詳細画面

投稿一覧（GET /posts）および投稿詳細（GET /posts/{id}）で投稿に付いているタグを表示してください。

例

```
Spring Bootを勉強しています。

#Java
#SpringBoot
```

タグをクリックすると

```
/tags/{name}
```

へ遷移してください。

---

### 6. Repository

必要に応じて以下を追加してください。

TagRepository

例

- findByName(...)
- existsByName(...)
- save(...)

PostRepository

例

- findAllByTag(...)

または適切なメソッドを定義してください。

---

### 7. Service

タグの抽出・登録・関連付けのロジックは Service に実装してください。

Controller には業務ロジックを書かず、

Controller
→ Service
→ Repository

の責務を守ってください。

---

## TDD

### RED

まず失敗するテストを書いてください。

対象

- TagControllerTest
- TagServiceTest
- PostServiceTest
- 必要に応じて RepositoryTest

確認内容

- 本文からタグが抽出される
- 重複タグは1件のみ保存される
- 既存タグが再利用される
- /tags/{name} で関連投稿が表示される
- タグなし投稿も登録できる

---

### GREEN

最小限のコードでテストを成功させてください。

---

### REFACTOR

最後に

- 重複コード削除
- 命名改善
- メソッド分割
- タグ抽出処理の共通化
- Service・Repository の責務整理
- 可読性向上

を行ってください。

---

## 完了条件

以下を確認してください。

- ./mvnw -B -Ph2 test が成功する
- 投稿時にタグが抽出・保存される
- 同じタグは重複登録されない
- タグをクリックすると関連投稿一覧が表示される
- 投稿一覧・投稿詳細でタグが表示される
- 既存機能に影響がない

最後に Conventional Commits 形式のコミットメッセージを提案してください。

例

feat(tag): implement hashtag parsing and tag pages
```

**結果**: 効いた / 部分的に効いた / 効かなかった
効かなかった

**振り返り**:
tag機能の画面表示をした時に、表示自体出来なかったので、どこまで効いていたのかが不明。
---------

## プロンプト 12

**フェーズ**:
C2: 投稿削除

**プロンプト本文**:

```
C2: 投稿削除（論理削除）を TDD で実装してください。

## 要件
- 投稿を論理削除できるようにする。
- 削除対象は `POST /posts/{id}/delete` とする。
- 物理削除は行わず、`deleted_at` カラムに削除日時を保存する。
- 削除後は `302` で `/posts` にリダイレクトする。
- 論理削除済みの投稿は一覧画面（GET /posts）に表示しない。
- 投稿詳細（GET /posts/{id}）で論理削除済みの投稿へアクセスした場合は `404 Not Found` を返す。
- 削除ボタンは投稿詳細画面に配置する。

## 設計方針
### Domain
- `Post` に論理削除を表す `deletedAt` を持たせる。
- `markDeleted()` などのメソッドで削除日時を設定する。
- `isDeleted()` を用意し、論理削除済みか判定できるようにする。

### Repository
- 一覧取得では `deleted_at IS NULL` の投稿のみ取得する。
- ID検索も論理削除済みを除外する。
- 必要に応じて論理削除用の更新メソッドを追加する。

### Service
- `delete(Long id)` を追加する。
- 存在しない投稿や論理削除済みの投稿は適切な例外を送出する。

### Controller
- `POST /posts/{id}/delete` を追加する。
- 削除成功後は `/posts` にリダイレクトする。
- 詳細取得では論理削除済みを404として扱う。

## TDD
### RED
以下の失敗するテストから実装する。

#### Controller
- POST `/posts/{id}/delete` が302で `/posts` にリダイレクトする。
- 論理削除済み投稿へ GET `/posts/{id}` すると404になる。

#### Service
- delete() 実行で deletedAt が設定される。
- delete() を2回実行しても適切に扱われる。

#### Repository
- 一覧取得で deleted_at が設定された投稿は取得されない。
- ID検索でも deleted_at が設定された投稿は取得されない。

### GREEN
最小実装でテストを通す。

### REFACTOR
- 論理削除判定の重複をなくす。
- 命名を整理する。
- マジックナンバー・重複SQLを排除する。

## 品質確認
以下をすべて成功させること。

- `./mvnw -B -Ph2 test`
- `./mvnw checkstyle:check`
- `./mvnw spotbugs:check`
- `./mvnw jacoco:check`

## コミット
Conventional Commits 形式でコミットする。

```

**結果**:  効いた / 部分的に効いた / 効かなかった
部分的に効いた

**振り返り**:
削除後に画面遷移した画面のメッセージ表示に違和感があった。
目指したい画面のメッセージやUIをもう少し細かくプロンプトの記載するように意識する。
---------

## プロンプト 13

**フェーズ**:
C3: REST API

**プロンプト本文**:

```
C3: REST API を TDD（RED→GREEN→REFACTOR）で実装してください。

■ 要件
- GET /api/posts を追加する。
- HTMLではなく JSON を返す REST API とする。
- 投稿一覧を新着順で返却する。
- 論理削除済み（deleted_at が設定されている投稿）は返却しない。
- 返却件数は最大50件とする。
- API専用のレスポンスDTOを作成し、Entityをそのまま返却しない。
- レスポンスには最低限以下を含める。
  - id
  - author
  - avatarColor
  - body
  - createdAt
  - likeCount（存在する場合）
- ステータスコードは200を返す。

■ OpenAPI
- springdoc-openapi を利用して簡易APIドキュメントを追加する。
- Swagger UI から GET /api/posts を確認・実行できるようにする。
- 必要に応じて @Operation や @Schema などのアノテーションを付与し、API概要が表示されるようにする。

■ テスト（RED）
- @WebMvcTest + MockMvc を使用して失敗するテストを先に作成する。
- HTTPステータス200を検証する。
- Content-Type が application/json であることを検証する。
- JSON配列が返ることを検証する。
- 主要項目（id、author、avatarColor、body、createdAt）が返却されることを検証する。
- Serviceは @MockBean を使用する。

■ GREEN
- テストが通る最小限の実装を行う。

■ REFACTOR
- Controllerを薄く保ち、Serviceに業務ロジックを集約する。
- DTOへの変換処理を整理する。
- 命名・重複コードを整理する。
- レイヤードアーキテクチャ（Controller / Service / Repository / Domain / DTO）の責務を守る。
- SpotBugs・Checkstyle・PMDで警告が出ない実装を心掛ける。

■ 完了条件
- ./mvnw -B -Ph2 test が成功すること。
- Swagger UI（/swagger-ui/index.html）で API を確認できること。
- Conventional Commits形式でコミットすること。
  例:
  feat(api): add REST endpoint for posts
```

**結果**:  効いた / 部分的に効いた / 効かなかった
部分的に効いた

**振り返り**:
API確認は行い、API取得できていることを確認できた。
ただ、テストとして、APIの表示以外に何を実施すればよいか、自分自身の知識不足でテストケースが足りないと感じた。

---------






