# =========================================================================
# かんたんセットアップ 共通ヘルパー (PowerShell)
#
# 受講生向けバッチ (かんたんセットアップ\*.bat) は ASCII のみのブートストラップ
# にして、日本語メッセージとロジックはすべてこの bin\*.ps1 側に置く。
#
# 理由: cmd.exe は UTF-8 のバッチファイルを正しく解釈できない。codepage 65001
# では日本語行のバイト境界でファイル読み取り位置がずれ、行の途中からコマンドと
# して誤実行される (例: 「JDK 21 / Maven ...」-> 'Maven' is not recognized)。
# UTF-8 BOM を付けても @echo off が壊れて全コード行がエコーされるだけで解決
# しない。PowerShell は UTF-8(BOM) を完全サポートするため、日本語を含む処理を
# cmd のパーサに一切通さないことで根本的に回避する。
# =========================================================================
$ErrorActionPreference = "Stop"

# コンソールと wsl/bash の UTF-8 出力が文字化けしないようにする
try { $null = & chcp 65001 } catch {}
try {
    [Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
    [Console]::InputEncoding  = [System.Text.UTF8Encoding]::new()
} catch {}
$env:WSL_UTF8 = "1"

$script:Distro = "Ubuntu-22.04"

# bin の 2 つ上 = リポジトリルート (かんたんセットアップ\bin -> かんたんセットアップ -> repo)
function Get-RepoRoot {
    return (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..\..")).Path
}

# logs フォルダ (かんたんセットアップ\logs) にタイムスタンプ付きのログパスを作る
function New-SetupLogPath {
    param([Parameter(Mandatory = $true)][string]$Prefix)
    $logDir = Join-Path $PSScriptRoot "..\logs"
    if (-not (Test-Path -LiteralPath $logDir)) {
        New-Item -ItemType Directory -Force -Path $logDir | Out-Null
    }
    $ts = Get-Date -Format "yyyyMMdd_HHmmss"
    return (Join-Path (Resolve-Path -LiteralPath $logDir).Path "${Prefix}_${ts}.log")
}

# Windows パスを WSL パス (/mnt/c/...) に変換する。失敗したら $null。
function ConvertTo-WslPath {
    param([Parameter(Mandatory = $true)][string]$WindowsPath)
    $converted = & wsl -d $script:Distro wslpath "$WindowsPath" 2>$null
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($converted)) {
        return $null
    }
    return ($converted | Select-Object -First 1).Trim()
}

# 罫線で囲んだ案内メッセージを表示する
function Write-Banner {
    param(
        [Parameter(Mandatory = $true)][string[]]$Lines,
        [string]$Color = "Cyan"
    )
    $bar = ("=" * 60)
    Write-Host ""
    Write-Host $bar -ForegroundColor $Color
    foreach ($line in $Lines) { Write-Host $line }
    Write-Host $bar -ForegroundColor $Color
    Write-Host ""
}

# pause 相当。受講生がウィンドウを閉じる前に表示を読めるようにする。
function Wait-Enter {
    Write-Host ""
    $null = Read-Host "Enter キーを押すと閉じます"
}

# WSL パス変換に失敗したときの共通エラー表示
function Show-WslPathError {
    Write-Banner -Color "Red" -Lines @(
        " [失敗] WSL の場所を特定できませんでした。",
        " WSL / Ubuntu-22.04 が正しく入っているか講師にご確認ください。"
    )
}

# WSL 上のスクリプトを実行する。
#   通常       : 画面にはカラーで表示しつつ tee でログにも記録し、ログからは
#                ANSI エスケープと CR を除去する。終了コードはスクリプトのものを伝播。
#   -Quiet     : 画面には出さず、技術的な出力はすべてログファイルへ。
# 戻り値: スクリプトの終了コード。WSL パス変換に失敗したら $null。
function Invoke-WslLogged {
    param(
        [Parameter(Mandatory = $true)][string]$RepoRoot,
        [Parameter(Mandatory = $true)][string]$LogFile,
        [Parameter(Mandatory = $true)][string]$BashCommand,  # 例: "bash scripts/doctor.sh --quick"
        [switch]$Quiet
    )
    $wrepo = ConvertTo-WslPath $RepoRoot
    if (-not $wrepo) { return $null }
    $wlog = ConvertTo-WslPath $LogFile
    if (-not $wlog) { return $null }

    # ログから ANSI カラーシーケンスと CR を除去する後処理
    $clean = "sed -i -e 's/\x1b\[[0-9;?]*[A-Za-z]//g' -e 's/\r//g' '$wlog' 2>/dev/null"

    if ($Quiet) {
        $cmd = "cd '$wrepo' && $BashCommand > '$wlog' 2>&1; rc=`$?; $clean; exit `$rc"
    } else {
        $cmd = "set -o pipefail; cd '$wrepo' && $BashCommand 2>&1 | tee '$wlog'; rc=`$?; $clean; exit `$rc"
    }

    & wsl -d $script:Distro -- bash -c $cmd
    return $LASTEXITCODE
}
