-- =========================================================================
-- H2 local seed: POSTS テーブルの初期データ
-- V1__init.sql の posts / posts_seq に依存する H2 専用シーダー。
-- =========================================================================

MERGE INTO posts (id, author, body, avatar_color, created_at) KEY(id) VALUES
    (1,  'tanaka',  '朝会メモを共有しました。今日の確認事項は投稿一覧の表示順です。', 'red', TIMESTAMP '2026-06-26 09:00:00'),
    (2,  'suzuki',  'H2 プロファイルで軽量に起動できるようにしました。', 'blue', TIMESTAMP '2026-06-26 09:08:00'),
    (3,  'sato',    'レビュー観点: th:text で表示して XSS を防ぐ。', 'green', TIMESTAMP '2026-06-26 09:16:00'),
    (4,  'ito',     'Repository テストは DB を空にしてから最小ケースで進めます。', 'yellow', TIMESTAMP '2026-06-26 09:24:00'),
    (5,  'kobayashi', '投稿本文の上限は 280 文字です。境界値テストを忘れずに。', 'purple', TIMESTAMP '2026-06-26 09:32:00'),
    (6,  'yamada',  'Service は Spring を起動せず Mockito で検証します。', 'red', TIMESTAMP '2026-06-26 09:40:00'),
    (7,  'nakamura', 'Controller は MockMvc でビュー名と model を確認します。', 'blue', TIMESTAMP '2026-06-26 09:48:00'),
    (8,  'kato',    'Flyway の migration は Oracle と H2 の両方で読める構文にします。', 'green', TIMESTAMP '2026-06-26 09:56:00'),
    (9,  'yoshida', '仕上げ前に ./mvnw -B -Ph2 verify を緑にします。', 'yellow', TIMESTAMP '2026-06-26 10:04:00'),
    (10, 'watanabe', 'プロンプト履歴は education/prompts-i-used.md に残します。', 'purple', TIMESTAMP '2026-06-26 10:12:00'),
    (11, 'demo-now-1', '画面確認用: 「たった今」と表示される投稿です。', 'red', CURRENT_TIMESTAMP),
    (12, 'demo-now-2', '画面確認用: 「たった今」と表示される投稿です。', 'blue', DATEADD('SECOND', -20, CURRENT_TIMESTAMP)),
    (13, 'demo-now-3', '画面確認用: 「たった今」と表示される投稿です。', 'green', DATEADD('SECOND', -40, CURRENT_TIMESTAMP)),
    (14, 'demo-min-1', '画面確認用: 「5分前」と表示される投稿です。', 'yellow', DATEADD('MINUTE', -5, CURRENT_TIMESTAMP)),
    (15, 'demo-min-2', '画面確認用: 「15分前」と表示される投稿です。', 'purple', DATEADD('MINUTE', -15, CURRENT_TIMESTAMP)),
    (16, 'demo-min-3', '画面確認用: 「45分前」と表示される投稿です。', 'red', DATEADD('MINUTE', -45, CURRENT_TIMESTAMP)),
    (17, 'demo-hour-1', '画面確認用: 「1時間前」と表示される投稿です。', 'blue', DATEADD('HOUR', -1, CURRENT_TIMESTAMP)),
    (18, 'demo-hour-2', '画面確認用: 「2時間前」と表示される投稿です。', 'green', DATEADD('HOUR', -2, CURRENT_TIMESTAMP)),
    (19, 'demo-hour-3', '画面確認用: 「3時間前」と表示される投稿です。', 'yellow', DATEADD('HOUR', -3, CURRENT_TIMESTAMP)),
    (20, 'demo-yesterday-1', '画面確認用: 「昨日」と表示される投稿です。', 'purple', DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
    (21, 'demo-yesterday-2', '画面確認用: 「昨日」と表示される投稿です。', 'red', DATEADD('DAY', -1, DATEADD('HOUR', -1, CURRENT_TIMESTAMP))),
    (22, 'demo-yesterday-3', '画面確認用: 「昨日」と表示される投稿です。', 'blue', DATEADD('DAY', -1, DATEADD('HOUR', -2, CURRENT_TIMESTAMP))),
    (23, 'demo-absolute-1', '画面確認用: 「yyyy/MM/dd HH:mm」形式で表示される投稿です。', 'green', DATEADD('DAY', -2, CURRENT_TIMESTAMP)),
    (24, 'demo-absolute-2', '画面確認用: 「yyyy/MM/dd HH:mm」形式で表示される投稿です。', 'yellow', DATEADD('DAY', -7, CURRENT_TIMESTAMP)),
    (25, 'demo-absolute-3', '画面確認用: 「yyyy/MM/dd HH:mm」形式で表示される投稿です。', 'purple', DATEADD('DAY', -30, CURRENT_TIMESTAMP));

ALTER SEQUENCE posts_seq RESTART WITH 26;
