ALTER TABLE posts ADD deleted_at TIMESTAMP(6);

CREATE INDEX posts_deleted_created_at_idx ON posts (deleted_at, created_at);
