# Quality Check

## Standard Check

通常の品質確認では、H2プロファイルで `verify` を実行します。

```bash
wsl ./mvnw -B -Ph2 verify
```

このコマンドで、テスト、Checkstyle、SpotBugs、JaCoCoの既定カバレッジ確認をまとめて実行します。

## Coverage Check

JaCoCoの結果を確認する場合は、`-DforkCount=0` を付けずに実行します。

```bash
wsl ./mvnw -B -Ph2 verify
```

`-DforkCount=0` はテスト調査や中間確認を速くするためには使えますが、JaCoCoの実行データが正しく集計されず、coverage warningが出ることがあります。

## Final Strict Check

仕上げ時は、80%以上のカバレッジとstrict品質ゲートを確認します。

```bash
wsl ./mvnw -B -Ph2 -Pcoverage-day3 -Pstrict verify
```
