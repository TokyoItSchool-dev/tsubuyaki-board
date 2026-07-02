-- =========================================================================
-- 社内つぶやきボード V5: POSTS テーブルに削除日時と投稿者ハッシュを追加
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
-- =========================================================================

ALTER TABLE posts ADD clientHash VARCHAR2(30 CHAR) DEFAULT 'legacy' NOT NULL;
ALTER TABLE posts ADD deleted_at TIMESTAMP(6);

CREATE INDEX posts_deleted_at_created_at_idx ON posts (deleted_at, created_at);
