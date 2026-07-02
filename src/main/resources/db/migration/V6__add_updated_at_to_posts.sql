-- =========================================================================
-- 社内つぶやきボード V6: 投稿の更新日時
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
-- =========================================================================

ALTER TABLE posts ADD updated_at TIMESTAMP(6);

UPDATE posts
SET updated_at = created_at
WHERE updated_at IS NULL;

CREATE INDEX posts_updated_at_idx ON posts (updated_at);
