-- =========================================================================
-- H2 local seed: POSTS テーブルの初期データ
-- V1__init.sql の posts / posts_seq に依存する H2 専用シーダー。
-- =========================================================================

MERGE INTO posts (id, author, body, created_at) KEY(id)
SELECT
    x,
    CASE MOD(x, 10)
        WHEN 1 THEN 'tanaka'
        WHEN 2 THEN 'suzuki'
        WHEN 3 THEN 'sato'
        WHEN 4 THEN 'ito'
        WHEN 5 THEN 'kobayashi'
        WHEN 6 THEN 'yamada'
        WHEN 7 THEN 'nakamura'
        WHEN 8 THEN 'kato'
        WHEN 9 THEN 'yoshida'
        ELSE 'watanabe'
    END,
    'H2 seed post #' || LPAD(x, 3, '0') || ' - 投稿一覧とページ表示確認用のテストデータです。',
    DATEADD('MINUTE', x * 5, TIMESTAMP '2026-06-26 09:00:00')
FROM SYSTEM_RANGE(1, 100);

ALTER SEQUENCE posts_seq RESTART WITH 101;
