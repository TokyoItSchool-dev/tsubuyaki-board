-- =========================================================================
-- 社内つぶやきボード V2: LIKES テーブル
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
-- =========================================================================

CREATE SEQUENCE likes_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE likes (
    id           NUMBER(19)        NOT NULL,
    post_id      NUMBER(19)        NOT NULL,
    client_hash  VARCHAR2(8 CHAR)  NOT NULL,
    CONSTRAINT likes_pk PRIMARY KEY (id),
    CONSTRAINT likes_post_fk FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT likes_post_client_uk UNIQUE (post_id, client_hash)
);

CREATE INDEX likes_post_id_idx ON likes (post_id);
