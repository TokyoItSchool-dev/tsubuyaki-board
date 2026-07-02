-- =========================================================================
-- 社内つぶやきボード V5: コメントテーブルを追加
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
-- =========================================================================

CREATE SEQUENCE comments_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE comments (
    id          NUMBER(19)          NOT NULL,
    post_id     NUMBER(19)          NOT NULL,
    body        VARCHAR2(280 CHAR)  NOT NULL,
    created_at  TIMESTAMP(6)        NOT NULL,
    CONSTRAINT comments_pk PRIMARY KEY (id),
    CONSTRAINT comments_post_fk FOREIGN KEY (post_id) REFERENCES posts (id)
);

CREATE INDEX comments_post_created_at_idx ON comments (post_id, created_at);
