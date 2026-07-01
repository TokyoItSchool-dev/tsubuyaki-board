-- Soft delete support for moving posts to the trash without deleting rows.
ALTER TABLE posts ADD deleted_at TIMESTAMP(6);

CREATE INDEX posts_deleted_at_idx ON posts (deleted_at);
