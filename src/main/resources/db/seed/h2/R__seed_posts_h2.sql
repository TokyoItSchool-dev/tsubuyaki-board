-- =========================================================================
-- H2 local seed: POSTS テーブルの初期データ
-- V1__init.sql の posts / posts_seq に依存する H2 専用シーダー。
-- =========================================================================

MERGE INTO posts (id, author, body, created_at) KEY(id)
SELECT
    x,
    'user' || x,
    CASE
        WHEN x IN (100, 101, 102) THEN
            '投稿一覧の本文省略を確認するための長い投稿です。'
            || REPEAT('長文確認', 35)
            || '三点リーダーとツールチップの動作確認にも使います。'
            || '新着50件の表示確認にも使います。No.' || x
        ELSE
            'H2 seed 投稿 No.' || x || '。新着50件の表示確認に使います。'
    END,
    DATEADD('MINUTE', x, TIMESTAMP '2026-06-26 09:00:00')
FROM SYSTEM_RANGE(1, 102);

ALTER SEQUENCE posts_seq RESTART WITH 103;
