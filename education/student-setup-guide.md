# 受講生向けセットアップガイド

AI 駆動開発研修 3 日コースで使う「社内つぶやきボード」演習リポジトリの初期セットアップ手順です。
**研修 0-1 時間目で「アプリが空起動できる状態」にする**ことがゴール。

詳細な進め方は [ONBOARDING.md](./ONBOARDING.md) を、つまずいたら [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) を参照してください。

---

## 0. このガイドの読み方

- **コマンドは上から順に実行**。前ステップが成功してから次へ。
- 期待出力と違うものが出たら、即座に隣の人か講師に確認。**自己流に進めない**。
- 詳細解説は本ガイドではしません。コマンドのみ。背景は ONBOARDING.md / README.md / TROUBLESHOOTING.md を読んでください。

---

## 1. 必要なアカウント（前日までに準備）

| 項目 | 用途 | 確認方法 |
|---|---|---|
| GitHub アカウント | Classroom リポへの push、PR | https://github.com にログインできること |
| OpenAI アカウント | Codex CLI の認証 | API キー (`sk-...`) を発行済、課金（クレジット残高）あり |

> OpenAI API キーは研修運営から配布されます。

---

## 2. 講師から受け取るもの

研修初日に講師から以下を受け取ります：

- **GitHub Classroom Assignment 招待 URL**（`https://classroom.github.com/a/xxxxxxxx` 形式）
- **Pleiades (Eclipse + 日本語パック) の配布媒体**（USB / 共有ドライブ）
- **Organization 名**（clone 時に URL に含まれます）

---

## 3. GitHub Classroom Assignment に参加

1. 講師から受け取った Classroom Assignment URL をブラウザで開く。
2. GitHub アカウントでサインイン（未認証なら）。
3. 「Accept this assignment」をクリック。
4. 数十秒待つと、Organization 配下に**自分専用の private リポジトリ**が自動生成されます。
   - URL 形式: `https://github.com/<org>/<assignment-name>-<your-github-id>`
5. ブラウザで自分のリポジトリを開き、ファイル一覧（`AGENTS.md`、`ONBOARDING.md` など）が見えることを確認。

> 💡 リポジトリ生成に 1 分以上かかる場合は、ブラウザをリロード。

---

## 4. Windows キッティング（管理者 PowerShell）

> ⚠️ **管理者として実行**してください。Pleiades は別途配布媒体から手動配置済の想定です。

1. **Pleiades を配置**: `C:\Pleiades` 配下に展開（配布媒体の zip を解凍）。
2. **作業フォルダ作成**: エクスプローラで `C:\workspace` を新規作成。
3. **管理者 PowerShell を起動**し、リポジトリ取得（仮配置）：

```powershell
cd C:\workspace
git clone https://github.com/<org>/<assignment-name>-<your-github-id>.git
cd <assignment-name>-<your-github-id>
```

4. キッティングスクリプトを実行：

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\scripts\setup.ps1
```

このスクリプトが以下を `winget` 経由で導入します：

- WSL2 機能の有効化
- Ubuntu 22.04 ディストロ
- Podman Desktop
- Git for Windows
- Windows Terminal

**所要時間: 5〜15 分**。完了後、画面の指示に従って**PC を再起動**してください。

> 📌 詳細な対処は [TROUBLESHOOTING.md Q1](./TROUBLESHOOTING.md) を参照。

---

## 5. WSL キッティング

再起動後、Ubuntu 22.04 の初回起動でユーザー名・パスワードを設定。その後 WSL ターミナルで：

```bash
cd /mnt/c/workspace/<assignment-name>-<your-github-id>
bash scripts/setup-wsl.sh
```

このスクリプトが以下を `apt` で導入します：

- Temurin JDK 21
- Maven
- Podman + podman-compose
- Node.js
- `gh` CLI、ripgrep、fd-find、jq

**所要時間: 5〜10 分**。

---

## 6. feature ブランチを切る

main 直 push は CI のブランチ保護で拒否されます。**必ず feature ブランチで開発**します。

```bash
git switch -c feature/<your-name>-m1
```

例: `git switch -c feature/yamada-m1`

> 📌 fork は不要です。push 先は自分の Classroom リポジトリ（origin）です。

---

## 7. `.env` と `OPENAI_API_KEY` を設定

```bash
# .env を雛形からコピー
cp dotenv.example .env

# OPENAI_API_KEY を恒久設定
echo 'export OPENAI_API_KEY=sk-...' >> ~/.bashrc   # 自分のキーに置換
source ~/.bashrc
```

> 💡 `.env` は `.gitignore` で除外済。コミットには含まれません。

---

## 8. 動作確認 5 点セット

以下 5 つが全て通れば 0-1 時間目完了です。

### 8-1. Doctor

```bash
bash scripts/doctor.sh --quick
```

期待出力: 全行が `[ OK ]` または `[WARN]`。`[ NG ]` があれば TROUBLESHOOTING.md を参照。

### 8-2. Oracle XE 起動

```bash
bash scripts/start-oracle.sh
```

期待出力: コンテナ `oracle-xe` が `Up (healthy)` 状態に到達。**初回は 5〜10 分かかります**（イメージ pull とスキーマ初期化）。

### 8-3. ビルド & テスト

```bash
./mvnw -B -Ph2 verify
```

期待出力: `BUILD SUCCESS`、JUnit テストが全て緑、JaCoCo がカバレッジレポートを出力。

### 8-4. アプリ起動 & ヘルスチェック

```bash
SPRING_PROFILES_ACTIVE=h2 ./mvnw spring-boot:run
```

別ターミナルで：

```bash
curl -s http://localhost:8080/actuator/health
```

期待出力: `{"status":"UP"}`

確認できたら、起動中のアプリは `Ctrl+C` で停止して構いません。

### 8-5. Codex CLI

```bash
codex-shell
# (コンテナ内で)
codex --help
```

期待出力: Codex CLI のヘルプメッセージが表示されること。

---

## 9. 初回 push と CI 緑化

ここまで完了したら、初回 push で GitHub Actions が動くか確認します。

```bash
git push -u origin feature/<your-name>-m1
```

ブラウザで自分のリポジトリの **Actions タブ**を開き、`Build + Test (H2)` ジョブが緑になることを確認。

> 📌 push が失敗する場合: Classroom リポへの書き込み権限が反映されるまで数分かかることがあります。それでもダメな場合は講師へ連絡。

---

## 10. 次に読むもの

ここまで通ったら、以下の順で読み進めてください：

1. **[ONBOARDING.md](./ONBOARDING.md)** — 演習 3 日間の動き方（21 時間のタイムテーブル、Codex 協働ループ、禁止事項）
2. **[../EXERCISES.md](../EXERCISES.md)** — 機能要件（MUST / SHOULD / COULD）と受入基準
3. **[../AGENTS.md](../AGENTS.md)** — Codex への規範書（Codex が最優先で読むファイル）
4. 詰まったら **[TROUBLESHOOTING.md](./TROUBLESHOOTING.md)** → `bash scripts/doctor.sh`

---

## 完了条件チェックリスト

研修 0-1 時間目終了時点で、以下が全て ✓ なら OK。

- [ ] Classroom Assignment URL から自分の private リポを生成済
- [ ] `git clone` 成功、`C:\workspace\<repo-name>` に配置
- [ ] `setup.ps1` → `setup-wsl.sh` 完走
- [ ] `feature/<your-name>-m1` ブランチを作成
- [ ] `OPENAI_API_KEY` を `~/.bashrc` に設定
- [ ] `doctor.sh --quick` 全行緑
- [ ] `./mvnw -B -Ph2 verify` BUILD SUCCESS
- [ ] `curl /actuator/health` が `{"status":"UP"}`
- [ ] `codex-shell` → `codex --help` 表示
- [ ] 初回 push で Actions タブの `Build + Test (H2)` が緑

すべて ✓ になったら、`ONBOARDING.md` の **1-2 時間目（仕様読解＋プロンプト準備）** に進んでください。
