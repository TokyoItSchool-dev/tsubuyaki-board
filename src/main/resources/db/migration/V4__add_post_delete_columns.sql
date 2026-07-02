-- =========================================================================
-- 社内つぶやきボード V4: 投稿者判定用 client_hash と論理削除フラグ
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
-- =========================================================================

ALTER TABLE posts ADD client_hash VARCHAR2(8 CHAR);
ALTER TABLE posts ADD deleted_at NUMBER(1) DEFAULT 0 NOT NULL;

CREATE INDEX posts_deleted_created_at_idx ON posts (deleted_at, created_at);
