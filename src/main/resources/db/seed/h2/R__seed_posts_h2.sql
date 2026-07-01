-- =========================================================================
-- H2 local seed: POSTS テーブルの初期データ
-- V1__init.sql の posts / posts_seq に依存する H2 専用シーダー。
-- =========================================================================

MERGE INTO app_users (id, name, avatar_color) KEY(id) VALUES
    (1,  'tanaka', '#ef4444'),
    (2,  'suzuki', '#22c55e'),
    (3,  'sato', '#3b82f6'),
    (4,  'ito', '#f59e0b'),
    (5,  'kobayashi', '#8b5cf6'),
    (6,  'yamada', '#ec4899'),
    (7,  'nakamura', '#14b8a6'),
    (8,  'kato', '#84cc16'),
    (9,  'yoshida', '#06b6d4'),
    (10, 'watanabe', '#6b7280');

MERGE INTO posts (id, body, created_at, user_id) KEY(id) VALUES
    (1,  '朝会メモを共有しました。今日の確認事項は投稿一覧の表示順です。',
        TIMESTAMP '2026-06-26 09:00:00', 1),
    (2,  'H2 プロファイルで軽量に起動できるようにしました。',
        TIMESTAMP '2026-06-26 09:08:00', 2),
    (3,  'レビュー観点: th:text で表示して XSS を防ぐ。',
        TIMESTAMP '2026-06-26 09:16:00', 3),
    (4,  'Repository テストは DB を空にしてから最小ケースで進めます。',
        TIMESTAMP '2026-06-26 09:24:00', 4),
    (5,  '投稿本文の上限は 280 文字です。境界値テストを忘れずに。',
        TIMESTAMP '2026-06-26 09:32:00', 5),
    (6,  'Service は Spring を起動せず Mockito で検証します。',
        TIMESTAMP '2026-06-26 09:40:00', 6),
    (7,  'Controller は MockMvc でビュー名と model を確認します。',
        TIMESTAMP '2026-06-26 09:48:00', 7),
    (8,  'Flyway の migration は Oracle と H2 の両方で読める構文にします。',
        TIMESTAMP '2026-06-26 09:56:00', 8),
    (9,  '仕上げ前に ./mvnw -B -Ph2 verify を緑にします。',
        TIMESTAMP '2026-06-26 10:04:00', 9),
    (10, 'プロンプト履歴は education/prompts-i-used.md に残します。',
        TIMESTAMP '2026-06-26 10:12:00', 10);

ALTER SEQUENCE app_users_seq RESTART WITH 11;
ALTER SEQUENCE posts_seq RESTART WITH 11;
