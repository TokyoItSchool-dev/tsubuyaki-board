# =========================================================================
# セットアップ 手順1 : Windows の準備 (管理者権限が必要)
#   WSL2 / Ubuntu / Podman Desktop / Git for Windows を導入する。
#   実処理は scripts\setup.ps1 が担う。本スクリプトは昇格と案内のみ。
# =========================================================================
. "$PSScriptRoot\_common.ps1"

# 管理者でなければ自己昇格して再実行する
$identity = [Security.Principal.WindowsIdentity]::GetCurrent()
$principal = New-Object Security.Principal.WindowsPrincipal($identity)
if (-not $principal.IsInRole([Security.Principal.WindowsBuiltinRole]::Administrator)) {
    Write-Host "管理者権限が必要です。確認画面が出たら「はい」を押してください..."
    Start-Process powershell -Verb RunAs -ArgumentList @(
        "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", "`"$PSCommandPath`""
    )
    return
}

Write-Banner -Lines @(
    " Windows の準備を始めます。しばらくお待ちください。",
    " （WSL2 / Ubuntu / Podman Desktop / Git を入れます）"
)

$repo = Get-RepoRoot
& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo "scripts\setup.ps1")
$rc = $LASTEXITCODE

if ($rc -ne 0) {
    Write-Banner -Color "Red" -Lines @(
        " [失敗] エラーが発生しました。",
        " 詳しい記録は C:\workspace\.kitting\ の setup-日付.log にあります。",
        " 解決しない場合は、この画面とそのログを講師にお見せください。"
    )
    Wait-Enter
    exit 1
}

Write-Banner -Lines @(
    " 手順1 はここまでです。",
    "",
    " この後の流れ:",
    "   1) いったん PC を再起動してください。（とても重要です）",
    "   2) スタートメニューから「Ubuntu」を起動し、新しいユーザー名とパスワードを決めて入力してください。",
    "   3) 「セットアップ2_Ubuntu準備.bat」をダブルクリックしてください。"
)
Wait-Enter
