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

**フェーズ**: サンプルデータ(シーダー)を100件に変更

**プロンプト本文**

```
 src\main\resources\db\migration\V1__init.sql に基づくH2用のシーダーを作成し、実行する。テストデータとして100個程度のシードデータを作成。H2で起動時に自動投入する。
```

**結果**: 効いた
差分：
    18      @Test
    19 -    @DisplayName("H2シーダー_起動時_投稿10件を投入し次IDが11になる")
    20 -    void H2シーダー_起動時_投稿10件を投入し次Idが11になる() throws SQLException {
    19 +    @DisplayName("H2シーダー_起動時_投稿100件を投入し次IDが101になる")
    20 +    void H2シーダー_起動時_投稿100件を投入し次Idが101になる() throws SQLException {
    21          String url = "jdbc:h2:mem:seed_" + UUID.randomUUID().toString().replace("-", "")
       ⋮
    31               Statement statement = connection.createStatement()) {
    32 -            assertThat(singleLong(statement, "SELECT COUNT(*) FROM posts")).isEqualTo(10);
    33 -            assertThat(singleLong(statement, "SELECT NEXT VALUE FOR posts_seq")).isEqualTo(11);
    32 +            assertThat(singleLong(statement, "SELECT COUNT(*) FROM posts")).isEqualTo(100);
    33 +            assertThat(singleLong(statement, "SELECT NEXT VALUE FOR posts_seq")).isEqualTo(101);
    34          }

検証

**振り返り**:

---

## プロンプト 3

**フェーズ**:投稿登録画面

**プロンプト本文**:

```
› ■ 投稿登録（POST /posts）実装プロンプト

  あなたはSpring Boot（MVC + Thymeleaf + Bean Validation）の開発者です。
  ■ 機能概要

  画面：posts/form
  一覧画面：posts/list
  ■ バリデーション仕様
  author（投稿者）
  必須
  空白のみは禁止
  ■ 成功時の処理
  HTTPステータス：302
  リダイレクト先：/posts
  DBに投稿を保存する（Service層経由）
  ■ 失敗時の処理（バリデーションエラー）
  HTTPステータス：200
  画面：posts/form を再表示
  入力値は保持する
  エラーメッセージを画面に表示する
  ■ 実装要件
  Controller
  @PostMapping("/posts")
  @Valid @ModelAttribute PostForm form
  BindingResult result を必ず直後に置く
  バリデーション

  以下を使用すること：

  @NotBlank
  @Size(min=1, max=30)
  @Size(min=1, max=280)

  ※ 空白のみ対策は @NotBlank を使用すること

  完了条件は ./mvnw -B -Ph2 test が緑になることです。
```

**結果**: 部分的に効いた
差分
  - src/main/java/com/example/tsubuyaki/controller/PostController.java:34: POST /posts 追加
  - src/main/java/com/example/tsubuyaki/web/dto/PostForm.java:8: @NotBlank + @Size(min=1, ...) に更新
  - src/main/java/com/example/tsubuyaki/service/PostService.java:25: 投稿保存 create 追加
  - src/test/java/com/example/tsubuyaki/controller/PostControllerTest.java:119: 成功・空白のみエラーの Controller テスト追加
  - src/test/java/com/example/tsubuyaki/service/PostServiceTest.java:44: 保存処理の Service テスト追加
検証:
  ./mvnw -B -Ph2 test 成功。Tests run: 16, Failures: 0, Errors: 0, Skipped: 0 です。Surefire の fork JVM 終了待ち警告は出ていますが、最終結果は BUILD SUCCESS / exit code 0 でした。

**振り返り**:
  30文字しか入力できない仕様になっていたため、30文字以上のエラーの確認ができなかった

---

## プロンプト 4

**フェーズ**:投稿登録画面

**プロンプト本文**:

```

■ 投稿登録画面

あなたはSpring Boot（MVC + Thymeleaf + Bean Validation）の開発者です。
現在、HTML側の入力制限（maxlength / required）により、サーバー側バリデーション（@Valid）の動作検証ができない状態です。

そのため、バックエンドのバリデーションを正しくテストできるようにフロントエンドの制約を撤廃してください。

■ 目的
サーバーサイドバリデーション（@Valid）の動作確認を可能にする
HTML側の制限に依存しない設計にする
不正入力をバックエンドで検知できる状態にする


■ バックエンド条件（再確認）

PostFormには以下が存在すること：

@NotBlank
@Size(max=30)
@Size(max=280)

Controllerは以下：

@Valid + BindingResult を使用

■ 期待する動作
成功時
302リダイレクト /posts
失敗時（バリデーションエラー）
200で posts/form 再表示
エラーメッセージ表示
入力値保持

■ 完成状態
フロント：制限なし入力フォーム
バックエンド：Springで厳密チェック
テスト：31文字入力でエラー確認可能


完了条件は ./mvnw -B -Ph2 test が緑になることです。

```

**結果**: 効いた
差分
 • Edited 2 files (+33 -3)
  └ src/main/resources/templates/posts/form.html (+3 -3)
    18              <label for="author">投稿者</label>
    19 -            <input type="text" id="author" th:field="*{author}" maxlength="30" required>
    19 +            <input type="text" id="author" th:field="*{author}">
    20              <p class="error" th:if="${#fields.hasErrors('author')}" th:errors="*{author}">エラー</p>
       ⋮
    22          <div>
    23 -            <label for="body">本文 (280 文字まで)</label>
    24 -            <textarea id="body" th:field="*{body}" maxlength="280" rows="4" required></textarea>
    23 +            <label for="body">本文</label>
    24 +            <textarea id="body" th:field="*{body}" rows="4"></textarea>
    25              <p class="error" th:if="${#fields.hasErrors('body')}" th:errors="*{body}">エラー</p>

  └ src/test/java/com/example/tsubuyaki/controller/PostControllerTest.java (+30 -0)
    131      @Test
    133 +    void newForm_HTML入力制限を出力しない() throws Exception {
    134 +        MvcResult result = mockMvc.perform(get("/posts/new"))
    135 +                .andExpect(status().isOk())
    136 +                .andExpect(view().name("posts/form"))
    137 +                .andReturn();
    139 +        String html = result.getResponse().getContentAsString();
    140 +        assertThat(html).doesNotContain("maxlength=");
    141 +        assertThat(html).doesNotContain(" required");
    142 +    }
    143 +
    144 +    @Test
    145      @DisplayName("投稿登録_空白のみのとき_入力値を保持してフォームを再表示する")
        ⋮
    160
    161 +    @Test
    162 +    @DisplayName("投稿登録_投稿者名31文字のとき_エラーを表示して入力値を保持する")
    163 +    void create_投稿者名31文字のとき_エラーを表示して入力値を保持する() throws Exception {
    164 +        String author = "a".repeat(31);
    165 +
    166 +        mockMvc.perform(MockMvcRequestBuilders.post("/posts")
    167 +                        .param("author", author)
    168 +                        .param("body", "本日の共有です"))
    169 +                .andExpect(status().isOk())
    170 +                .andExpect(view().name("posts/form"))
    171 +                .andExpect(model().attributeHasFieldErrors("postForm", "author"))
    172 +                .andExpect(content().string(containsString("投稿者名は 1 文字以上 30 文字以内で入力してください")))
    173 +                .andExpect(content().string(containsString("value=\"" + author + "\"")));
    174 +
    175 +        verify(postService, never()).create(author, "本日の共有です");
    176 +    }
    177 +
    178      private static Post post(String author, String body, Instant createdAt) {

  根拠:

  - src/main/resources/templates/posts/form.html の maxlength / required を削除
  - 本文 (280 文字まで) の表示も 本文 に変更し、フロント側の制限表現を除去
  - src/test/java/com/example/tsubuyaki/controller/PostControllerTest.java に以下を追加
      - フォームHTMLに maxlength= / required が出ないこと
      - 投稿者名31文字で 200、posts/form 再表示、エラー表示、入力値保持、Service未呼び出しになること

  検証:

  - ./mvnw -B -Ph2 test
  - 結果: Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
  - 最終結果: BUILD SUCCESS

**振り返り**:
  成功: 302 で /posts にリダイレクト
　失敗: 200 で posts/form を再表示しエラー表示　できることを確認した。

---

## プロンプト　5

**フェーズ**: 詳細一覧

**プロンプト本文**:

```
あなたはSpring Boot（MVC + Service層 + Thymeleaf）の開発者です。
以下の仕様に従って「投稿詳細表示機能（M4）」を実装してください。

■ 機能概要
エンドポイント：GET /posts/{id}
目的：指定した投稿IDの詳細を表示する
画面：posts/detail.html
■ 表示仕様
正常系（存在するID）
投稿データを取得する
posts/detail.html に表示する
表示項目：
author（投稿者）
body（本文）
createdAt（投稿日時）
■ 異常系（存在しないID）
該当データが存在しない場合
HTTPステータス：404 Not Found
エラーページまたはSpring標準の404を返す
■ Controller実装要件
@GetMapping("/posts/{id}") を使用
@PathVariable Long id を使用
Service層を経由してデータ取得すること
Optionalやnullチェックを適切に行うこと
■ Service要件
findById(id) のようなメソッドを使用
データが存在しない場合の扱いはControllerに委譲する
■ View（Thymeleaf）
作成必須
templates/posts/detail.html を作成すること
表示内容
投稿者（author）
本文（body）
投稿日時（createdAt）
■ 注意事項
Serviceでnullを返す設計は禁止ではないが、Optional推奨
Controllerで必ず404制御を行うこと
HTMLは新規作成すること（既存流用不可）
■ ゴール
投稿IDを指定して詳細画面を表示し、存在しない場合は正しく404を返すこと
```

**結果**: 効いた

```
  差分:

  - src/main/java/com/example/tsubuyaki/service/PostService.java に Optional<Post> findById(Long id) を追加
  - src/main/java/com/example/tsubuyaki/controller/PostController.java に GET /posts/{id} を追加
      - 存在するID: posts/detail を表示
      - 存在しないID: 404 Not Found

  - src/main/resources/templates/posts/detail.html を新規作成
      - author
      - body
      - createdAt
        を th:text / time で表示

  - src/test/java/com/example/tsubuyaki/controller/PostControllerTest.java に正常系・404系を追加
  - src/test/java/com/example/tsubuyaki/service/PostServiceTest.java に findById の委譲テストを追加

  検証:

  - ./mvnw -B -Ph2 test
  - 結果: Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
  - BUILD SUCCESS

```

**振り返り**:





## プロンプト6

**フェーズ**: S2 キーワード検索

**プロンプト本文**:

```
タスク:  S2: キーワード検索を TDD で実装してください。

順序:
1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
2. その失敗テストを通す最小実装を書きます (Green)。
3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

受入基準:
GET /posts?q=xxx で本文 LIKE 検索
一覧画面を再利用 (検索ボックスを上部に追加)

制約:
- AGENTS.md と .codex/instructions.md を参照すること。
- このタスクのスコープ外のファイルは触らないこと。
- テストを書かずに本体コードを書き始めないこと。
- 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
- 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。
```

**結果**: 効いた

```
• Edited 4 files (+22 -1)
  └ src/main/java/com/example/tsubuyaki/controller/PostController.java (+12 -1)
     14  import org.springframework.web.bind.annotation.PostMapping;
     15 +import org.springframework.web.bind.annotation.RequestParam;
     16  import org.springframework.web.server.ResponseStatusException;
        ⋮
     32      @GetMapping({ "/", "/posts", "/posts/" })
     32 -    public String list(Model model) {
     33 +    public String list(@RequestParam(name = "q", required = false) String query, Model model) {
     34 +        if (hasText(query)) {
     35 +            model.addAttribute("posts", postService.searchByBodyContaining(query));
     36 +            model.addAttribute("q", query);
     37 +            return "posts/list";
     38 +        }
     39          model.addAttribute("posts", postService.findLatest50());
     40 +        model.addAttribute("q", "");
     41          return "posts/list";
        ⋮
     95      }
     96 +
     97 +    private static boolean hasText(String value) {
     98 +        return value != null && !value.isBlank();
     99 +    }
    100  }

  └ src/main/java/com/example/tsubuyaki/repository/PostRepository.java (+2 -0)
    10      List<Post> findTop50ByOrderByCreatedAtDesc();
    12 +    List<Post> findTop50ByBodyContainingOrderByCreatedAtDesc(String keyword);
    13  }

  └ src/main/java/com/example/tsubuyaki/service/PostService.java (+4 -0)
    31
    33 +        return repository.findTop50ByBodyContainingOrderByCreatedAtDesc(keyword);
    34 +    }
    35 +
    36      public Optional<Post> findById(Long id) {

  └ src/main/resources/templates/posts/list.html (+4 -0)
    19      </form>
    20 +    <form class="post-list__search" method="get" th:action="@{/posts}">
    21 +        <input type="search" name="q" th:value="${q}" placeholder="本文を検索">
    22 +        <button type="submit">検索</button>
    23 +    </form>

```

**振り返り**:
  検索欄が作成され、条件抽出ができた。
  今回は初めてテンプレートのプロンプトを使用して実装を行った。自作のプロンプトと比べて余計な出力が少なく、細かく効率的に実装を進めることができたため、今後もこの形式を活用していきたい。
  また、実装した機能については問題なく動作することを確認できた。一方で、コミットを細かい単位で行えていなかったことに気付いたため、今後は機能ごとやキリの良いタイミングでコミットすることを意識して作業を進めたい。


## プロンプト 7

**フェーズ**: 投稿者名フィールド拡張

**プロンプト本文**:

```
› タスク:  S3: 投稿者名フィールド拡張を TDD で実装してください。

  順序:
  1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
  2. その失敗テストを通す最小実装を書きます (Green)。
  3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
  4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
  5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

  受入基準:
  投稿者名を必須にし、フォームに任意のアバター色を選択させる (簡易)

  制約:
  - AGENTS.md と .codex/instructions.md を参照すること。
  - このタスクのスコープ外のファイルは触らないこと。
  - テストを書かずに本体コードを書き始めないこと。
  - 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
```

**結果**: 部分的に効いた

```

  • Edited 2 files (+29 -0)
  └ src/main/java/com/example/tsubuyaki/web/dto/PostForm.java (+10 -0)
    15
    16 +    private String avatarColor;
    17 +
    18      public PostForm() {
       ⋮
    35      }
    36 +
    37 +    public String getAvatarColor() {
    38 +        return avatarColor;
    39 +    }
    40 +
    41 +    public void setAvatarColor(String avatarColor) {
    42 +        this.avatarColor = avatarColor;
    43 +    }
    44  }

  └ src/main/resources/templates/posts/form.html (+19 -0)
    21          </div>
    22 +        <fieldset>
    24 +            <label>
    25 +                <input type="radio" th:field="*{avatarColor}" value="">
    26 +                指定なし
    27 +            </label>
    28 +            <label>
    30 +                青
    31 +            </label>
    32 +            <label>
    33 +                <input type="radio" th:field="*{avatarColor}" value="green">
    34 +                緑
    35 +            </label>
    36 +            <label>
    37 +                <input type="radio" th:field="*{avatarColor}" value="pink">
    38 +                ピンク
    39 +            </label>
    40 +        </fieldset>
    41          <div>

```

**振り返り**:
  アバターの色は指定できるようになったが、実際にUIは変わらなかった。
  一覧で選択した色で出力されるように依頼分を投げる

## プロンプト8

**フェーズ**: 投稿者名フィールド拡張

**プロンプト本文**:

```
  アバターの色は指定できますが、一覧で選択した色になるようにしてください
  制約:
  - AGENTS.md と .codex/instructions.md を参照すること。
  - このタスクのスコープ外のファイルは触らないこと。
  - テストを書かずに本体コードを書き始めないこと。
  - 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
```

**結果**: 効いた

```
• Edited 5 files (+28 -3)
  └ src/main/java/com/example/tsubuyaki/controller/PostController.java (+1 -1)
    73          }
    74 -        postService.create(form.getAuthor(), form.getBody());
    74 +        postService.create(form.getAuthor(), form.getBody(), form.getAvatarColor());
    75          return "redirect:/posts";

  └ src/main/java/com/example/tsubuyaki/domain/Post.java (+12 -0)
    28
    29 +    @Column(name = "avatar_color", length = 20)
    30 +    private String avatarColor;
    31 +
    32      @Column(name = "created_at", nullable = false)
       ⋮
    39      public Post(String author, String body, Instant createdAt) {
    40 +        this(author, body, createdAt, null);
    41 +    }
    42 +
    43 +    public Post(String author, String body, Instant createdAt, String avatarColor) {
    44          this.author = author;
       ⋮
    46          this.createdAt = createdAt;
    47 +        this.avatarColor = avatarColor;
    48      }
       ⋮
    61
    62 +    public String getAvatarColor() {
    63 +        return avatarColor;
    64 +    }
    65 +
    66      public Instant getCreatedAt() {

  └ src/main/java/com/example/tsubuyaki/service/PostService.java (+6 -1)
    58      public void create(String author, String body) {
    59 -        repository.save(new Post(author, body, Instant.now()));
    59 +        create(author, body, null);
    60 +    }
    61 +
    62 +    @Transactional
    63 +    public void create(String author, String body, String avatarColor) {
    64 +        repository.save(new Post(author, body, Instant.now(), avatarColor));
    65      }

  └ src/main/resources/db/migration/V2__add_avatar_color_to_posts.sql (+6 -0)
    1 +-- =========================================================================
    2 +-- 社内つぶやきボード V2: POSTS にアバター色を追加
    3 +-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
    4 +-- =========================================================================
    5 +
    6 +ALTER TABLE posts ADD avatar_color VARCHAR2(20 CHAR);

  └ src/main/resources/templates/posts/list.html (+3 -1)
    27          <article class="post" th:each="post : ${posts}">
    28 -            <p class="post__author" th:text="${post.author}">投稿者</p>
    28 +            <p class="post__author"
    29 +               th:style="${#strings.isEmpty(post.avatarColor)} ? null : |color: ${post.avatarColor}|"
    30 +               th:text="${post.author}">投稿者</p>
    31              <p class="post__body" th:text="${post.body}">本文</p>

```

**振り返り**:

  一覧で選択した色で出力されるようになった。
  余裕があれば、詳細画面などでも反映したい。


## プロンプト 9

**フェーズ**: C2: 投稿削除

**プロンプト本文**:

```
› タスク:  C2: 投稿削除を TDD で実装してください。

  順序:
  1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
  2. その失敗テストを通す最小実装を書きます (Green)。
  3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
  4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
  5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

  受入基準:
  論理削除 (deleted_at カラム)、論理削除済みは一覧で非表示。
  削除ボタンは詳細画面に作ること

  制約:
  - AGENTS.md と .codex/instructions.md を参照すること。
  - このタスクのスコープ外のファイルは触らないこと。
  - テストを書かずに本体コードを書き始めないこと。
  - 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
  - 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。

  完了条件は ./mvnw -B -Ph2 test
  ./mvnw -B -Ph2 -Pcoverage-day3 -Pstrict verify が緑になることです。
```

**結果**: 効いた

```
  • Edited src/main/java/com/example/tsubuyaki/domain/Post.java (+11 -0)
    34
    35 +    @Column(name = "deleted_at")
    36 +    private Instant deletedAt;
    37 +
    38      protected Post() {
       ⋮
    72
    73 +    public Instant getDeletedAt() {
    74 +        return deletedAt;
    75 +    }
    76 +
    77 +    public void markDeleted(Instant deletedAt) {
    78 +        this.deletedAt = deletedAt;
    79 +    }
    80 +
    81      @Override

• Edited src/main/java/com/example/tsubuyaki/repository/PostRepository.java (+5 -2)
     6  import java.util.List;
     7 +import java.util.Optional;
     8
       ⋮
    10
    10 -    List<Post> findTop50ByOrderByCreatedAtDesc();
    11 +    List<Post> findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    12
    12 -    List<Post> findTop50ByBodyContainingOrderByCreatedAtDesc(String keyword);
    13 +    List<Post> findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc(String keyword);
    14 +
    15 +    Optional<Post> findByIdAndDeletedAtIsNull(Long id);
    16  }

• Edited src/main/java/com/example/tsubuyaki/service/PostService.java (+9 -3)
    28      public List<Post> findLatest50() {
    29 -        return repository.findTop50ByOrderByCreatedAtDesc();
    29 +        return repository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    30      }
       ⋮
    32      public List<Post> searchByBodyContaining(String keyword) {
    33 -        return repository.findTop50ByBodyContainingOrderByCreatedAtDesc(keyword);
    33 +        return repository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc(keyword);
    34      }
       ⋮
    36      public Optional<Post> findById(Long id) {
    37 -        return repository.findById(id);
    37 +        return repository.findByIdAndDeletedAtIsNull(id);
    38      }
       ⋮
    65      }
    66 +
    67 +    @Transactional
    68 +    public void delete(Long id) {
    69 +        repository.findByIdAndDeletedAtIsNull(id)
    70 +                .ifPresent(post -> post.markDeleted(Instant.now()));
    71 +    }
    72  }

• Edited src/main/java/com/example/tsubuyaki/controller/PostController.java (+6 -0)
    68
    69 +    @PostMapping("/posts/{id}/delete")
    70 +    public String delete(@PathVariable Long id) {
    71 +        postService.delete(id);
    72 +        return "redirect:/posts";
    73 +    }
    74 +
    75      @PostMapping("/posts")

• Added src/main/resources/db/migration/V3__add_deleted_at_to_posts.sql (+8 -0)
    1 +-- =========================================================================
    2 +-- 社内つぶやきボード V3: POSTS に論理削除日時を追加
    3 +-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
    4 +-- =========================================================================
    5 +
    6 +ALTER TABLE posts ADD deleted_at TIMESTAMP(6);
    7 +
    8 +CREATE INDEX posts_deleted_at_created_at_idx ON posts (deleted_at, created_at);

```

**振り返り**:
  削除ボタン押下で、一覧に出力されないようになったが、論理削除されているのか確認が必要




## プロンプト 1(サンプル)

**フェーズ**: 一覧画面

**プロンプト本文**:

```
タスク:  詳細画面遷移ボタンを TDD で実装してください。

  順序:
  1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
  2. その失敗テストを通す最小実装を書きます (Green)。
  3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
  4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
  5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

  受入基準:
  一覧画面から投稿の詳細に遷移できるように変更

  制約:
  - AGENTS.md と .codex/instructions.md を参照すること。
  - このタスクのスコープ外のファイルは触らないこと。
  - テストを書かずに本体コードを書き始めないこと。
  - 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
  - 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。

  完了条件は ./mvnw -B -Ph2 test
  ./mvnw -B -Ph2 -Pcoverage-day3 -Pstrict verify が緑になることです。

```

**結果**: 効いた

```
• Edited src/main/resources/templates/posts/list.html (+1 -0)
    34                    th:text="${#temporals.format(post.createdAt, 'yyyy-MM-dd HH:mm')}">2026-05-23 09:00</time>
    35 +            <a th:href="@{/posts/{id}(id=${post.id})}">詳細</a>

```

**振り返り**:
  一覧から詳細画面へ遷移することができた。


## プロンプト

**フェーズ**:C1:タグ機能

**プロンプト本文**:

```
タスク:  C1：タグ機能を TDD で実装してください。

順序:
1. まず受入基準を満たすための失敗テストを 1 本だけ書きます (Red)。
2. その失敗テストを通す最小実装を書きます (Green)。
3. 重複・命名・抽象度の観点でリファクタリングします (Refactor)。
4. `./mvnw -B -Ph2 test` で全テストが緑であることを確認します。
5. Conventional Commits でコミットします (`feat(<scope>): <要約>`)。

受入基準:
新規登録する際に各投稿にタグを設定できるようにします。
タグを管理するテーブルを作成
以下の Entity 定義に基づいて JPA レイヤを生成してください。

テーブル設計：
正規化を意識し、冗長性のない構成としてください。要件を満たす範囲で適切なテーブル分割・リレーションを設計してください。
- ...

生成するもの:
1. `@Entity` クラス (パッケージ: `com.example.tsubuyaki.domain`)
2. `JpaRepository<EntityName, Long>` インタフェース
3. `@DataJpaTest` のテストクラス (正常系 1 本以上 + 異常系 1 本以上)
4. Flyway マイグレーション `V<n>__<entity>.sql` (Oracle / H2(MODE=Oracle) 両方で動く SQL)

制約:
- ID は `@GeneratedValue(strategy = GenerationType.SEQUENCE)` + `@SequenceGenerator(allocationSize = 1)` で統一。
- 日時カラムは `java.time.Instant` を使う。`java.util.Date` は禁止。
- 文字列カラムには `@Column(length = ..., nullable = ...)` を明示。
- DDL は `NUMBER(19)`, `VARCHAR2(... CHAR)`, `TIMESTAMP(6)` を使い、H2(MODE=Oracle) でも動くように書く。
- リポジトリのメソッド名は派生クエリ (`findTop50ByOrderByCreatedAtDesc`) を優先し、`@Query` を使うときは bind 変数を必ず使う (`?1` ではなく `:name` 推奨)。

本文中の #tag をパースして Tag テーブルに保存、GET /tags/{name} で関連投稿一覧。



制約:
- AGENTS.md と .codex/instructions.md を参照すること。
- このタスクのスコープ外のファイルは触らないこと。
- テストを書かずに本体コードを書き始めないこと。
- 失敗テストを通すために `@Disabled` を貼ることは絶対に禁止。
- 完了後にどの 3 ファイル前後を変更したかを要約して報告すること。

```

**結果**: 部分的に聞いた

```
 Edited src/main/java/com/example/tsubuyaki/controller/PostController.java (+9 -0)
    44
    45 +    @GetMapping("/tags/{name}")
    46 +    public String listByTag(@PathVariable String name, Model model) {
    47 +        String tagName = normalizeQuery(name).toLowerCase();
    48 +        model.addAttribute("posts", postService.findByTagName(tagName));
    49 +        model.addAttribute("q", "");
    50 +        model.addAttribute("tagName", tagName);
    54      @GetMapping("/posts/new")

• Edited src/main/java/com/example/tsubuyaki/controller/PostController.java (+2 -1)
    20  import java.security.NoSuchAlgorithmException;
    21 +import java.util.Locale;
    22  import java.util.HexFormat;
       ⋮
    47      public String listByTag(@PathVariable String name, Model model) {
    47 -        String tagName = normalizeQuery(name).toLowerCase();
    48 +        String tagName = normalizeQuery(name).toLowerCase(Locale.ROOT);
    49          model.addAttribute("posts", postService.findByTagName(tagName));

```

**振り返り**:
テーブルレイアウトからすべてお任せでやってみた。中間テーブルを作成し、多対多になる設計がされていたので簡単なものであればお任せでやってほうが早いと感じた。
しかし、一覧画面が表示されなくなった。


## プロンプト 1(サンプル)

**フェーズ**: C1:タグ機能

**プロンプト本文**:

```
一覧画面が表示されませんエラーを解決、または調査を行ってください
```

**結果**: 効いた

```


```

**振り返り**:
設計の続きだったため短い文章で送ってみた。
テスト検証でループになり
■ Quota exceeded. Check your plan and billing details.
と出力され終了した。



## プロンプト 1(サンプル)

**フェーズ**:

**プロンプト本文**:

```

```

**結果**: 効いた

```


```

**振り返り**:
