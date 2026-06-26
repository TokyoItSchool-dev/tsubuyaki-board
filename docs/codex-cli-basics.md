# Codex CLI 基本操作ガイド

この文書は、研修中に Codex CLI を使って開発を進めるための操作メモです。
初回セットアップは [受講生向けセットアップガイド](../education/student-setup-guide.md) を正本とし、
ここでは `codex-shell` が使える状態になった後の操作を扱います。

## 1. 起動する

Codex は WSL Ubuntu から起動します。Windows の PowerShell ではなく、Ubuntu ターミナルを使ってください。

```bash
# 🐧 WSL Ubuntu
cd /mnt/c/workspace/tsubuyaki-board
codex-shell
```

成功すると、プロンプトが Codex devbox コンテナ内に変わります。

```text
codex@a3f5e7c2:/workspace$
```

コンテナに入ったら、リポジトリのルートで `codex` を起動します。

```bash
# 📦 Codex コンテナ内
codex
```

CLI 自体のヘルプを見たいときは、対話セッションに入る前に次を実行します。

```bash
codex --help
```

## 2. モデルと effort

この研修リポジトリでは `.codex/config.toml` に既定モデルと reasoning effort が設定済みです。
通常は変更せず、そのまま使います。

```toml
model = "gpt-5.5"
model_reasoning_effort = "medium"
```

一時的に別モデルで起動したいときは `-m` を使います。

```bash
codex -m gpt-5.5
codex -m gpt-5.4-mini
```

一時的に effort も変えたいときは、`-c` で設定を上書きします。

```bash
codex -c 'model_reasoning_effort="low"'
codex -c 'model_reasoning_effort="high"'
```

起動後に切り替える場合は、Codex セッション内で `/model` を使います。対応するモデルでは reasoning effort も選べます。切り替え後は `/status` で現在の設定を確認できます。

| 設定 | 使う場面 |
|---|---|
| `gpt-5.5` | 研修の基本。実装、設計、テスト、レビューを一通り任せるとき |
| `gpt-5.4-mini` | 軽い調査、読み取り中心の確認、速さを優先したいとき |
| `medium` effort | 研修の基本。通常の TDD 実装、リファクタ、レビュー |
| `low` effort | 単純な説明、短い確認、差分の要約など |
| `high` effort | 複雑なバグ調査、設計判断、境界値やセキュリティ観点のレビュー |
| `xhigh` effort | 講師指示がある深い調査や、仕上げ前の重要レビューに限定 |

effort を上げると品質が上がる可能性がありますが、応答時間と token 使用量も増えます。迷ったら `gpt-5.5` + `medium` のまま進めます。

## 3. Plan Mode

実装前に方針を確認したいときは `/plan` を使います。

```text
/plan GET /posts の一覧画面を TDD で実装する計画を作ってください。
```

Plan Mode では、Codex にいきなりファイル編集させず、実装手順・触るファイル・テスト方針を先に整理させます。
次の場面で使うと効果的です。

- 初めて触る機能に入る前
- どのクラスを触るべきか迷うとき
- 変更範囲が Controller / Service / Repository にまたがるとき
- 失敗した実装を立て直すとき

計画に納得したら、通常の依頼として「この計画で実装してください」と続けます。計画に違和感があれば、実装前に修正を指示します。

## 4. resume で前回の続きを開く

Codex は会話履歴を保存します。前回と同じ課題を続けるときは、新しいセッションを始めるより `resume` を優先します。

```bash
# 最近のセッション一覧から選ぶ
codex resume

# 現在のリポジトリで直近のセッションを開く
codex resume --last
```

Codex セッション内にいる場合は `/resume` でも再開できます。

```text
/resume
```

使い分けは次の通りです。

| 操作 | 使う場面 |
|---|---|
| `codex resume` | 前回の続きを選んで再開したい |
| `codex resume --last` | 直前のセッションにすぐ戻りたい |
| `/resume` | Codex 起動中に別の保存済みセッションへ切り替えたい |
| `/new` | 別の課題に移り、前の文脈を引き継ぎたくない |

同じファイルを複数セッションで同時に編集させると差分が衝突しやすくなります。1 つのユースケースは、できるだけ 1 つのセッションで進めてください。

## 5. 基本スラッシュコマンド

Codex セッション内で `/` を入力すると、使えるスラッシュコマンドを検索できます。研修でよく使うものは以下です。

| コマンド | 用途 |
|---|---|
| `/status` | モデル、承認モード、作業ディレクトリ、token 使用状況を確認 |
| `/model` | セッション中にモデルを切り替える |
| `/plan` | 実装前に計画を作らせる |
| `/resume` | 保存済みセッションを再開する |
| `/diff` | Codex が作った差分を確認する |
| `/review` | 作業ツリーをレビューさせる |
| `/compact` | 長くなった会話を要約して文脈を軽くする |
| `/new` | 同じ CLI 内で新しい会話を始める |
| `/quit` | Codex CLI を終了する |
| `/exit` | `/quit` と同じ。Codex CLI を終了する |

## 6. 研修用プロンプト

リポジトリの `.codex/prompts/` には、研修用のプロンプト雛形があります。
Codex コンテナ起動時に同期されるため、Codex セッション内ではスラッシュコマンドとして呼び出せます。

| コマンド | 用途 |
|---|---|
| `/tdd-cycle` | RED → GREEN → REFACTOR を 1 ユースケースで回す |
| `/controller-skeleton` | Controller / Service / Repository の雛形を作る |
| `/jpa-entity` | JPA Entity / Repository / Flyway / Repository テストを作る |
| `/review` | push 前に XSS / SQLi / ハードコード観点でセルフレビューする |

穴埋めが必要なプロンプトは、`.codex/prompts/*.md` の中身を読んでから使います。`/review` は穴埋めなしでも使えます。

## 7. `@` と `/mention` でファイルを指定する

Codex CLI では、IDE のように開いているファイルが自動で全部伝わるわけではありません。
読んでほしいファイルは `@` または `/mention` で明示します。

```text
@education/EXERCISES.md の M1 を読み、@src/main/java/com/example/tsubuyaki/controller/PostController.java に必要な変更を提案してください。
```

```text
/mention src/main/resources/templates/posts/index.html
```

使い方の目安です。

- 要件を読ませる: `@education/EXERCISES.md`
- 既存実装を読ませる: `@src/main/java/...`
- テンプレートを確認させる: `@src/main/resources/templates/...`
- エラー全文が長い: まずエラーを貼り、必要なファイルを `@` で添える

## 8. 良い依頼文の型

Codex への指示は、次の 4 点を入れると安定します。

- **ゴール**: 何を実現したいか
- **対象**: どの要件・ファイル・画面を扱うか
- **制約**: TDD、H2、XSS 禁止、`th:text` 使用など
- **完了条件**: どのテストやコマンドが緑なら完了か

例:

```text
GET /posts の一覧画面を TDD で実装してください。
@education/EXERCISES.md の M1 を受入基準とし、
まず @WebMvcTest + MockMvc で失敗するテストを 1 本書いてください。
Thymeleaf では th:utext を使わず th:text を使ってください。
完了条件は ./mvnw -B -Ph2 test が緑になることです。
```

避ける指示:

```text
いい感じに作って
テストも書いて
全部直して
```

## 9. 1 フェーズあたりの基本ループ

演習中は、1 ユースケースを次の流れで進めます。

```text
1. EXERCISES.md でこのフェーズの受入基準を読む
2. 自分の作業ブランチ <github-id> にいることを確認する
3. codex-shell でコンテナに入り、Codex を起動する
4. /plan または /tdd-cycle で TDD の進め方を固める
5. Codex が RED → GREEN → REFACTOR を回す
6. 生成されたコードを受講生が読む
7. ./mvnw -B -Ph2 test を回して緑を確認する
8. /review でセルフレビューする
9. ./mvnw -B -Ph2 verify を回して緑を確認する
10. git add / commit / push する
```

ブランチは [受講生向けセットアップガイド §6](../education/student-setup-guide.md) で作成した `<github-id>` ブランチを使います。
課題ごとに分けたい人だけ、任意で `<github-id>/m1-post-list` のようなサブブランチを切ります。

## 10. git 操作の基本

コミット履歴は受講生自身の名前で残します。Codex に任せきりにせず、差分を読んでからコミットしてください。

```bash
# 🐧 Ubuntu または 📦 Codex コンテナ内のリポジトリルート
git status
git diff
```

内容を説明できることを確認してから、ファイル単位でステージングします。

```bash
git add src/main/java/com/example/tsubuyaki/controller/PostController.java
git add src/test/java/com/example/tsubuyaki/controller/PostControllerTest.java
git diff --staged
```

コミットメッセージは Conventional Commits 形式にします。

```bash
git commit -m "feat(post): GET /posts で最新50件を新着順に返す"
```

push 前には必ず `verify` を緑にします。

```bash
./mvnw -B -Ph2 verify
git push
```

自分の fork のブランチに push した時点で 1 ユースケース完了です。
push すると、初回セットアップ時に作成した講師レビュー用 PR の差分が自動更新されます。
upstream の `main` にはマージしません。

## 11. コミット前と PR のセルフチェック

各ユースケースを push する前に、以下を確認します。

- 実装した MUST / SHOULD / COULD の受入基準を説明できる
- `./mvnw -B -Ph2 verify` が緑
- JaCoCo カバレッジの現在値を把握している
- 主要プロンプトを `docs/prompts-i-used.md` に控えている
- XSS / SQLi / ハードコード観点で `/review` 済み
- 次にやることを 1〜3 行で説明できる

PR の「Files changed」タブを開くと、スターターからの自分の全変更を確認できます。
相互レビューではこの PR 画面を共有し、各行・各コミットにコメントを付け合います。

## 12. 停止する

Codex CLI を終了するときは、セッション内で次のどちらかを入力します。

```text
/quit
/exit
```

Codex が処理中で止めたい場合は `Ctrl+C` を使います。停止後は、必要なら `git status` で途中差分を確認します。

Codex devbox コンテナから Ubuntu に戻るには、コンテナのシェルで次を実行します。

```bash
exit
# または Ctrl+D
```

## 13. 困ったとき

まず状態を確認します。

```text
/status
```

Codex CLI 自体の使い方を確認します。

```bash
codex --help
```

環境異常を疑うときは、WSL Ubuntu のリポジトリルートで Doctor を実行します。

```bash
bash scripts/doctor.sh
```

エラーが解消しない場合は、[TROUBLESHOOTING.md](../education/TROUBLESHOOTING.md) を確認し、講師に次の 4 点を伝えます。

- 何をしようとしているか
- 何を試したか
- 何が起きたか
- どのファイル / どの行か
