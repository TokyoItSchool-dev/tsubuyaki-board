-- =========================================================================
-- H2 local seed: POSTS テーブルの初期データ
-- V1__init.sql の posts / posts_seq に依存する H2 専用シーダー。
-- =========================================================================

MERGE INTO posts (id, author, title, body, created_at) KEY(id) VALUES
    (1,  'tanaka',  '朝会メモ', '朝会メモを共有しました。今日の確認事項は投稿一覧の表示順です。', TIMESTAMP '2026-06-26 09:00:00'),
    (2,  'suzuki',  'H2 プロファイル', 'H2 プロファイルで軽量に起動できるようにしました。', TIMESTAMP '2026-06-26 09:08:00'),
    (3,  'sato',    'レビュー観点', 'レビュー観点: th:text で表示して XSS を防ぐ。', TIMESTAMP '2026-06-26 09:16:00'),
    (4,  'ito',     'Repository テスト', 'Repository テストは DB を空にしてから最小ケースで進めます。', TIMESTAMP '2026-06-26 09:24:00'),
    (5,  'kobayashi', '本文上限', '投稿本文の上限は 280 文字です。境界値テストを忘れずに。', TIMESTAMP '2026-06-26 09:32:00'),
    (6,  'yamada',  'Service テスト', 'Service は Spring を起動せず Mockito で検証します。', TIMESTAMP '2026-06-26 09:40:00'),
    (7,  'nakamura', 'Controller テスト', 'Controller は MockMvc でビュー名と model を確認します。', TIMESTAMP '2026-06-26 09:48:00'),
    (8,  'kato',    'Flyway migration', 'Flyway の migration は Oracle と H2 の両方で読める構文にします。', TIMESTAMP '2026-06-26 09:56:00'),
    (9,  'yoshida', 'verify 確認', '仕上げ前に ./mvnw -B -Ph2 verify を緑にします。', TIMESTAMP '2026-06-26 10:04:00'),
    (10, 'watanabe', 'プロンプト履歴', 'プロンプト履歴は education/prompts-i-used.md に残します。', TIMESTAMP '2026-06-26 10:12:00');

ALTER SEQUENCE posts_seq RESTART WITH 11;
