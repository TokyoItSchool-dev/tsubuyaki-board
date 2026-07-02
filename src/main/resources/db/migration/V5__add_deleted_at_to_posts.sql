ALTER TABLE posts ADD (
    deleted_at NUMBER(1) DEFAULT 0 NOT NULL
);

CREATE INDEX posts_deleted_at_created_at_idx ON posts (deleted_at, created_at);
