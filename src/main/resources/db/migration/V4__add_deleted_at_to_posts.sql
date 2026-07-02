-- =========================================================================
-- V4: 投稿の論理削除日時
-- deleted_at が NULL の投稿だけを通常表示対象にする。
-- =========================================================================

ALTER TABLE posts ADD deleted_at TIMESTAMP(6);

CREATE INDEX posts_deleted_at_created_at_idx ON posts (deleted_at, created_at);
