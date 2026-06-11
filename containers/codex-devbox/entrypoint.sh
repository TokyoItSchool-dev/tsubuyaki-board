#!/usr/bin/env bash
# Codex devbox entrypoint.
# 起動時に環境を検証し、対話シェルもしくは指定コマンドを起動する。
set -euo pipefail

# $'...' (ANSI-C quoting) で実際のエスケープ文字を格納する。
# '\033...' のリテラルだと echo -e では色になるが、起動バナーの heredoc (cat <<EOF)
# はバックスラッシュを解釈しないため、生の「\033[0;32m」が画面に出てしまう。
RED=$'\033[0;31m'
GREEN=$'\033[0;32m'
YELLOW=$'\033[1;33m'
CYAN=$'\033[0;36m'
RESET=$'\033[0m'

# --- 1. OPENAI_API_KEY 検査 ----------------------------------------------
if [[ -z "${OPENAI_API_KEY:-}" ]]; then
    echo -e "${RED}[codex-devbox] OPENAI_API_KEY が未設定です。${RESET}" >&2
    echo -e "${YELLOW}WSL 側で education/student-setup-guide.md §7-2 の手順を実施してから run-codex.sh を呼んでください。${RESET}" >&2
    exit 1
fi

if [[ "${OPENAI_API_KEY}" != sk-* ]]; then
    echo -e "${YELLOW}[codex-devbox] WARN: OPENAI_API_KEY が 'sk-' で始まっていません。形式を再確認してください。${RESET}" >&2
fi

# --- 2. Codex 認証 (auth.json 生成) ---------------------------------------
# 現行 Codex CLI は OPENAI_API_KEY 環境変数を直接は認証に使わない。
# stdin 経由で `codex login --with-api-key` に渡し、$CODEX_HOME/auth.json を生成する。
# 毎起動で実行する (キーをローカルに書き出すだけなので冪等。キー差し替えにも追従する)。
CODEX_HOME_DIR="${CODEX_HOME:-${HOME}/.codex}"
mkdir -p "${CODEX_HOME_DIR}"
if printenv OPENAI_API_KEY | codex login --with-api-key >/dev/null 2>&1; then
    AUTH_STATUS="${GREEN}auth.json 生成済み (API キー認証)${RESET}"
else
    AUTH_STATUS="${RED}失敗${RESET} (手動実行: printenv OPENAI_API_KEY | codex login --with-api-key)"
    echo -e "${YELLOW}[codex-devbox] WARN: codex login に失敗しました。コンテナ内で上記コマンドを再実行してください。${RESET}" >&2
fi

# --- 3. 研修用プロジェクト設定の有効化 (trust 付与) ------------------------
# リポ直下の .codex/config.toml (プロジェクトローカル設定) は、Codex CLI が
# プロジェクトを trusted と認識している場合のみ読み込まれる。
# 研修専用 CODEX_HOME の config.toml に /workspace の trust を 1 回だけ追記する。
if ! grep -qs 'projects."/workspace"' "${CODEX_HOME_DIR}/config.toml"; then
    cat >> "${CODEX_HOME_DIR}/config.toml" <<'TRUST'

# tsubuyaki-board 研修: /workspace の .codex/config.toml を有効化 (entrypoint.sh が自動追記)
[projects."/workspace"]
trust_level = "trusted"
TRUST
fi

# --- 4. 共通プロンプトをスラッシュコマンドとして同期 -----------------------
# $CODEX_HOME/prompts/*.md は codex 内で /<ファイル名> として呼び出せる。
# (Codex CLI はリポ側 .codex/prompts/ を自動では読まないため、起動時に同期する)
if compgen -G "/workspace/.codex/prompts/*.md" >/dev/null 2>&1; then
    mkdir -p "${CODEX_HOME_DIR}/prompts"
    cp -f /workspace/.codex/prompts/*.md "${CODEX_HOME_DIR}/prompts/" 2>/dev/null || true
fi

# --- 5. git safe.directory の保険登録 (uid 不一致対策) -------------------
# 注: PATH 先頭の codex-guard が git config --global を reject するため、
#     ここでは実体パスを直接呼ぶ。これは entrypoint (= 起動時の正規セットアップ)
#     なので、Codex が走り出す前に必要な操作。
# 注: scripts/run-codex.sh 経由の起動では ~/.gitconfig が /dev/null:ro で
#     マスクされるため、この書き込みは失敗して no-op になる (|| true で握る)。
#     その経路は --userns=keep-id で uid が一致し safe.directory 自体が不要。
#     マスク無しで直接 podman run された場合のための保険として残している。
REAL_GIT=/usr/bin/git
[[ -x "${REAL_GIT}" ]] || REAL_GIT=/bin/git
if [[ -x "${REAL_GIT}" ]]; then
    "${REAL_GIT}" config --global --add safe.directory /workspace >/dev/null 2>&1 || true
    "${REAL_GIT}" config --global --add safe.directory '/workspace/*' >/dev/null 2>&1 || true
fi

# --- 6. 起動バナー -------------------------------------------------------
CODEX_VERSION_INFO="$(codex --version 2>/dev/null || echo 'unknown')"
JAVA_LINE="$(java -version 2>&1 | head -n 1)"
MAVEN_LINE="$(mvn -v 2>/dev/null | head -n 1 || echo 'mvn not found')"

GUARD_STATUS="無効"
if [[ -x /opt/codex-guard/bin/rm ]]; then
    GUARD_STATUS="${GREEN}有効${RESET} (rm / git / chmod / chown / dd / sudo を wrapper で監査)"
fi

# .env が /dev/null マウントで上書きされているかを検査
ENV_STATUS="読み取り可"
if [[ -e /workspace/.env ]] && [[ ! -s /workspace/.env ]]; then
    # サイズ 0 = /dev/null overlay されている可能性大
    ENV_STATUS="${GREEN}/dev/null 上書きマウント済 (機密値は到達不能)${RESET}"
fi

cat <<EOF
${GREEN}╔══════════════════════════════════════════════════════════╗${RESET}
${GREEN}║  社内つぶやきボード Codex devbox                          ║${RESET}
${GREEN}╚══════════════════════════════════════════════════════════╝${RESET}
  ${CYAN}Codex CLI${RESET}    : ${CODEX_VERSION_INFO}
  ${CYAN}Java${RESET}         : ${JAVA_LINE}
  ${CYAN}Maven${RESET}        : ${MAVEN_LINE}
  ${CYAN}Workspace${RESET}    : $(pwd)
  ${CYAN}API Key${RESET}      : 設定済み (値は表示しません)
  ${CYAN}Codex 認証${RESET}   : ${AUTH_STATUS}
  ${CYAN}研修ハーネス${RESET} : ${GUARD_STATUS}
  ${CYAN}.env${RESET}         : ${ENV_STATUS}

EOF

# --- 7. コマンド実行 -----------------------------------------------------
if [[ $# -eq 0 ]]; then
    exec bash -l
else
    exec "$@"
fi
