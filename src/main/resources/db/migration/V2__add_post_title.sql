-- =========================================================================
-- 社内つぶやきボード V2: POSTS にタイトルを追加
-- 既存投稿は本文先頭をタイトルとして補完する
-- =========================================================================

ALTER TABLE posts ADD (
    title VARCHAR2(100 CHAR)
);

UPDATE posts
SET title = SUBSTR(body, 1, 100)
WHERE title IS NULL;

ALTER TABLE posts MODIFY (
    title VARCHAR2(100 CHAR) NOT NULL
);
