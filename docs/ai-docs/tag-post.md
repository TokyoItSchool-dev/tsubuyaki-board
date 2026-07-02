# タグ機能 + 投稿フォームタグ補完 TDD計画

## 概要

新規投稿画面にタグ入力フォームを追加し、タグを確定したタイミングで Tag テーブルへ保存する。

既存タグはタグ入力フォームで入力中に候補表示し、候補クリックでタグを確定済みタグ一覧へ追加する。

存在しないタグは、タグ確定 API で確定したタイミングで DB に登録する。

`POST /posts` では確定済みタグ名を受け取り、投稿と既存タグを関連付ける。本文中の `#tag` はタグ登録・関連付けの入力元にしない。

`GET /tags/{name}` では既存の投稿一覧画面 `posts/list` を再利用して、関連投稿を新着順で表示する。

## インクリメント分割

**Backend: Tag 永続化モデル**

- 要件: `tags` テーブルを追加し、`tags.name` は必須・重複不可にする。
- 要件: 投稿とタグの関連テーブルを追加し、1 つの投稿に複数タグを関連付けられる。
- 要件: 同一投稿と同一タグの重複関連は DB 制約で失敗する。
- 要件: 既存の投稿一覧、検索、いいね、アバター表示は変更しない。
- Red TODO: `TagRepositoryTest` に `タグ保存_nameでTagを取得できる` を追加する。
- Red TODO: `TagRepositoryTest` に `タグ保存_同一name重複_DB制約で失敗する` を追加する。
- Red TODO: `PostRepositoryTest` または `TagRepositoryTest` に `投稿タグ保存_投稿に複数タグを関連付けられる` を追加する。
- Red TODO: `PostRepositoryTest` または `TagRepositoryTest` に `投稿タグ保存_同一投稿同一タグ重複_DB制約で失敗する` を追加する。
- Green 方針: `Tag` Entity、`TagRepository`、投稿タグ関連、Flyway migration を追加する。

**Backend / Controller: タグ確定 API**

- 要件: `POST /tags` を追加する。
- 要件: リクエストは `name` パラメータでタグ名を受け取る。
- 要件: 先頭の `#` と前後空白は除去して扱う。
- 要件: 既存タグは再利用し、未登録タグはこのタイミングで新規登録する。
- 要件: 空文字または不正なタグ名は `400 Bad Request` を返す。
- 要件: レスポンスは確定したタグ名を JSON で返す。
- 要件: Spring Security の CSRF は有効維持し、JavaScript はフォーム内の CSRF トークンを送る。
- Red TODO: `PostServiceTest` に `タグ確定_未登録タグを作成して返す` を追加する。
- Red TODO: `PostServiceTest` に `タグ確定_既存タグは再利用する` を追加する。
- Red TODO: `PostServiceTest` に `タグ確定_name空_400相当の例外にする` を追加する。
- Red TODO: `PostControllerTest` に `タグ確定_POST_tags_未登録タグを作成してJSONを返す` を追加する。
- Red TODO: `PostControllerTest` に `タグ確定_POST_tags_既存タグを再利用してJSONを返す` を追加する。
- Red TODO: `PostControllerTest` に `タグ確定_POST_tags_name空_400を返す` を追加する。
- Green 方針: Service にタグ確定メソッドを追加し、Controller で JSON API へ接続する。

**Backend: 投稿登録時に確定済みタグを関連付ける**

- 要件: `PostForm` に確定済みタグ名リストを追加する。
- 要件: `PostService#create(...)` は本文中の `#タグ名` を抽出しない。
- 要件: 投稿保存時は送信された確定済みタグ名を既存タグとして投稿へ関連付ける。
- 要件: 投稿保存時に未登録タグを新規登録しない。
- 要件: 同一タグ名は 1 回だけ投稿へ関連付ける。
- 要件: タグなし投稿は従来どおり保存できる。
- Red TODO: `PostServiceTest` に `投稿登録_確定済みタグを投稿に関連付ける` を追加する。
- Red TODO: `PostServiceTest` に `投稿登録_同一タグ名は1回だけ関連付ける` を追加する。
- Red TODO: `PostServiceTest` に `投稿登録_本文中のハッシュタグは抽出しない` を追加する。
- Red TODO: `PostServiceTest` に `投稿登録_タグなし本文_従来どおり投稿できる` を追加する。
- Red TODO: `PostControllerTest` に `投稿登録_確定済みタグ名をServiceへ渡す` を追加する。
- Green 方針: Controller から Service へ確定済みタグ名を渡し、Service は既存タグだけを関連付ける。

**Backend / Controller: タグ別一覧**

- 要件: `GET /tags/{name}` を追加する。
- 要件: タグ名に紐づく投稿を新着順・最大 50 件で取得する。
- 要件: Controller は `posts/list` に `posts` と `tagName` を渡す。
- 要件: 存在しないタグは空一覧として表示する。
- Red TODO: `PostServiceTest` に `タグ別一覧_タグ名で関連投稿を新着順に返す` を追加する。
- Red TODO: `PostControllerTest` に `タグ別一覧_GET_tags_name_関連投稿をposts_listへ渡す` を追加する。
- Red TODO: `PostControllerTest` に `タグ別一覧_存在しないタグ_空一覧を表示する` を追加する。
- Green 方針: Repository / Service にタグ名検索を追加し、Controller で既存一覧ビューへ接続する。

**Backend / Controller: タグ候補 API**

- 要件: `GET /tags/suggestions?q=xxx` を追加する。
- 要件: 既存タグ名を前方一致で最大 10 件返す。
- 要件: `q` が空の場合は、名前順の既存タグを最大 10 件返す。
- 要件: タグ確定 API で登録した新規タグも、次回以降の補完候補に出る。
- Red TODO: `PostServiceTest` に `タグ候補_前方一致で最大10件返す` を追加する。
- Red TODO: `PostControllerTest` に `タグ候補_GET_tags_suggestions_前方一致のタグ名を返す` を追加する。
- Red TODO: `PostControllerTest` に `タグ候補_GET_tags_suggestions_q空_タグ名を最大10件返す` を追加する。
- Green 方針: JSON でタグ名配列を返す最小 API を追加する。

**Frontend: 投稿フォームのタグ入力と補完**

- 要件: 新規登録画面に本文 textarea とは別のタグ入力フォームを作成する。
- 要件: タグ入力フォームで入力中の文字列を使って候補リストを表示する。
- 要件: 候補クリックでタグ確定 API を呼び、成功したタグを確定済みタグ一覧に表示する。
- 要件: 未登録タグは Enter またはスペースでタグ確定 API を呼び、成功後に確定済みタグ一覧へ表示する。
- 要件: 確定済みタグは hidden input として投稿フォームに含める。
- 要件: 確定済みタグ一覧はリンク色で表示するが、HTML 上は `a` タグを使わない。
- 要件: 同じタグは確定済みタグ一覧に重複表示しない。
- Red TODO: `PostControllerTest` に `投稿作成フォーム_タグ入力フォームと候補リストを表示する` を追加する。
- Red TODO: `PostControllerTest` に `投稿作成フォーム_確定済みタグ一覧領域を表示する` を追加する。
- Red TODO: JavaScript 手動確認項目として、候補クリック、スペース確定、Enter 確定でタグ確定 API が呼ばれることを追加する。
- Red TODO: JavaScript 手動確認項目として、確定済みタグが hidden input として投稿されることを追加する。
- Green 方針: textarea 下ではなくタグ入力欄の近くに候補リストと確定済みタグ一覧を配置する。
- Green 方針: 静的 JavaScript からタグ候補 API とタグ確定 API を呼び出す。
- Green 方針: `.tag-selected-tags` と `.tag-selected-tags__item` を追加し、`.post__tags a` と近い見た目にする。

**Frontend: 詳細画面のタグリンク**

- 要件: 投稿詳細画面にタグリンクを `#tag` 形式で表示する。
- 要件: リンク先は `/tags/{name}` にする。
- 要件: タグ名表示は `th:text` を使う。
- Red TODO: `PostControllerTest` に `投稿詳細_タグリンクを表示する` を追加する。
- Green 方針: 詳細画面で `post.tags` を表示し、タグ名を URL パスとして渡す。

**Frontend: タグ表示の統一**

- 要件: 詳細画面では、投稿に関連付いたタグを本文下のリンクテキストとして表示する。
- 要件: 本文中に `#tag` が書かれていても、それだけでは詳細画面のタグリンクにしない。
- 要件: 投稿本文の保存内容は変えない。
- 要件: 新規登録画面では、確定済みタグをタグ入力フォーム下にリンクテキスト風で表示する。
- Red TODO: `PostControllerTest` に `投稿詳細_関連タグだけをタグリンクとして表示する` を追加する。
- Red TODO: `PostControllerTest` に `投稿作成フォーム_確定タグ一覧領域を表示する` を追加する。
- Green 方針: 詳細画面は投稿に関連付いた `post.tags` だけをタグリンク表示の正とする。
- Green 方針: `tag-suggestions.js` に確定済みタグ一覧と hidden input の更新処理を追加する。

**Frontend: 確定済みタグの削除**

- 要件: 新規登録画面で、確定済みタグをクリックすると削除ボタンを表示する。
- 要件: 削除ボタンの表示文言は `削除` とする。
- 要件: 削除ボタンはクリックしたタグに対してだけ表示する。
- 要件: 削除ボタンは吹き出し型のポップとして表示する。
- 要件: 削除ボタンの表示でページ内の他要素のレイアウトを動かさない。
- 要件: 削除ボタンのポップは他の要素の上に重なって表示されてもよい。
- 要件: 吹き出しの外側をクリックしたとき、削除ボタンのポップを閉じる。
- 要件: 削除ボタンを押すと、対象タグを確定済みタグ一覧から削除する。
- 要件: 削除されたタグの hidden input `tagNames` も削除する。
- 要件: 削除後、同じタグを再度タグ入力フォームから確定できる。
- 要件: 削除は作成中フォーム上の確定済みタグから外す操作だけとし、DB の `tags` レコードは削除しない。
- 要件: 削除ボタンは `type="button"` とし、投稿フォームを submit しない。
- 要件: 削除ボタンには対象タグ名が分かる `aria-label` を付ける。
- Red TODO: JavaScript 手動確認項目として、確定済みタグクリックで削除ボタンが表示されることを追加する。
- Red TODO: JavaScript 手動確認項目として、削除ボタンが吹き出し型ポップで表示され、周辺レイアウトが動かないことを追加する。
- Red TODO: JavaScript 手動確認項目として、吹き出しの外側クリックで削除ボタンのポップが消えることを追加する。
- Red TODO: JavaScript 手動確認項目として、削除ボタン押下で確定済みタグ一覧と hidden input からタグが消えることを追加する。
- Red TODO: JavaScript 手動確認項目として、削除後に同じタグを再度確定できることを追加する。
- Red TODO: JavaScript 手動確認項目として、削除時に DB 削除 API を呼ばないことを追加する。
- Green 方針: `tag-suggestions.js` に選択中タグの状態と削除処理を追加する。
- Green 方針: `.tag-selected-tags__popover` と `.tag-selected-tags__delete` を追加し、確定済みタグの近くに削除ボタンを絶対配置で表示する。

**結合確認とリファクタ**

- 要件: 既存の投稿一覧、検索、いいね、アバター表示が壊れていないことを確認する。
- 要件: タグ確定、タグ候補、投稿タグ関連付けの責務を整理する。
- Red TODO: 既存機能の回帰がないことを `./mvnw -B -Ph2 verify` で確認する。
- Green 方針: 必要に応じてタグ入力、タグ確定、タグ別一覧の命名と責務を整理する。

## テスト計画

小さい単位で Red / Green を確認する。

```bash
./mvnw -B -Ph2 -Dtest=TagRepositoryTest test
./mvnw -B -Ph2 -Dtest=PostServiceTest test
./mvnw -B -Ph2 -Dtest=PostControllerTest test
```

インクリメント完了ごとに H2 プロファイルで verify を実行する。

```bash
./mvnw -B -Ph2 verify
```

仕上げで strict verify を実行する。

```bash
./mvnw -B -Ph2 -Pstrict verify
```

## タグ入力フォームの追加テスト計画

```bash
./mvnw -B -Ph2 -Dtest=PostControllerTest test
./mvnw -B -Ph2 clean verify
```

- `タグ確定_POST_tags_未登録タグを作成してJSONを返す`
  - Given: `spring` タグが未登録。
  - When: CSRF 付きで `POST /tags` に `name=spring` を送る。
  - Then: HTTP 200 と JSON のタグ名 `spring` を返す。
  - Then: `tags` テーブルに `spring` が保存される。
- `投稿登録_本文中のハッシュタグは抽出しない`
  - Given: 本文に `#spring` を含み、確定済みタグ名は空。
  - When: `POST /posts` を実行する。
  - Then: 投稿は保存される。
  - Then: `spring` タグは新規登録されず、投稿タグ関連も作られない。
- `投稿登録_確定済みタグ名をServiceへ渡す`
  - Given: 新規投稿フォームで `tagNames=spring` を送る。
  - When: `POST /posts` を実行する。
  - Then: Controller は `PostService#create(...)` に確定済みタグ名を渡す。
- `投稿作成フォーム_タグ入力フォームと候補リストを表示する`
  - Given: 新規投稿画面を開く。
  - When: `GET /posts/new` を実行する。
  - Then: 本文 textarea とは別にタグ入力フォームを表示する。
  - Then: タグ候補リスト、確定済みタグ一覧、hidden input の描画先を表示する。

## 手動確認項目

- タグ入力フォームで既存タグ候補が表示される。
- 候補クリックでタグが確定し、hidden input が追加される。
- 未登録タグを Enter またはスペースで確定すると DB 登録 API が呼ばれる。
- 本文 textarea に `#tag` を書いただけではタグ登録・関連付けされない。
- 投稿詳細画面では、投稿に関連付いたタグだけが本文下にリンク表示される。
- 確定済みタグをクリックすると、そのタグの削除ボタンが表示される。
- 削除ボタンは吹き出し型ポップとして表示され、ページ内の他要素のレイアウトを動かさない。
- 吹き出しの外側をクリックすると、削除ボタンのポップが消える。
- 削除ボタンを押すと、確定済みタグ一覧と hidden input から対象タグが消える。
- 削除後、同じタグを再度タグ入力フォームから確定できる。
- 削除時に DB 削除 API は呼ばれない。

## 前提

- タグ名は日本語を許可する。
- `#Java` と `#java` は別タグとして扱う。
- タグ確定 API では、先頭の `#` と前後空白を除去して保存する。
- 既存タグ補完は前方一致・最大 10 件とする。
- 候補 UI はタグ入力フォーム付近の独自リストで実装する。
- 新規タグはタグ確定時だけ DB 登録する。
- 投稿保存時は、送信された確定済みタグ名を投稿へ関連付けるだけにする。
- 投稿保存時に未登録タグは作成しない。
- `/tags/{name}` はブラウザの URL エンコードに任せ、Controller では `@PathVariable String name` として受ける。
- 投稿一覧へのタグ表示は含めず、まず詳細画面のタグリンクだけを作る。
- 新規登録画面の「リンクテキストに変換」は、入力欄内の文字装飾ではなく、入力欄下の確定済みタグ一覧で表現する。
- DB 保存仕様を変更し、タグ登録の入力元は本文ではなくタグ入力フォームにする。
- タグ削除は新規投稿フォーム上の確定済みタグから外す操作であり、DB の `tags` テーブルからは削除しない。
