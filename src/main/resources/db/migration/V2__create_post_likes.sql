-- =========================================================================
-- 社内つぶやきボード V2: POST_LIKES テーブル
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
-- =========================================================================

CREATE TABLE post_likes (
    post_id      NUMBER(19)        NOT NULL,
    client_hash  VARCHAR2(8 CHAR)   NOT NULL,
    created_at   TIMESTAMP(6)       NOT NULL,
    CONSTRAINT post_likes_pk PRIMARY KEY (post_id, client_hash),
    CONSTRAINT post_likes_post_fk FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE
);

CREATE INDEX post_likes_post_id_idx ON post_likes (post_id);
