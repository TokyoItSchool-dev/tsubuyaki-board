-- =========================================================================
-- 社内つぶやきボード V2: POSTS_LIKE テーブル
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
-- =========================================================================

CREATE SEQUENCE seq_like_id START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE posts_like (
    id          NUMBER(19)        NOT NULL,
    post_id     NUMBER(19)        NOT NULL,
    created_at  TIMESTAMP(6)      NOT NULL,
    clientHash  VARCHAR2(30 CHAR) NOT NULL,
    CONSTRAINT posts_like_pk PRIMARY KEY (id)
);

CREATE INDEX posts_like_post_id_idx ON posts_like (post_id);
CREATE INDEX posts_like_post_client_idx ON posts_like (post_id, clientHash);
