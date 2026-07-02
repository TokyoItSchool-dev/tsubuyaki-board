-- =========================================================================
-- 社内つぶやきボード V5: 投稿へのリプライツリー
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
-- =========================================================================

CREATE SEQUENCE replies_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE replies (
    id               NUMBER(19)          NOT NULL,
    post_id          NUMBER(19)          NOT NULL,
    parent_reply_id  NUMBER(19),
    author           VARCHAR2(30 CHAR)   NOT NULL,
    body             VARCHAR2(1000 CHAR) NOT NULL,
    created_at       TIMESTAMP(6)        NOT NULL,
    read_at          TIMESTAMP(6),
    CONSTRAINT replies_pk PRIMARY KEY (id),
    CONSTRAINT replies_post_fk FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT replies_parent_fk FOREIGN KEY (parent_reply_id) REFERENCES replies (id)
);

CREATE INDEX replies_post_created_at_idx ON replies (post_id, created_at, id);
CREATE INDEX replies_parent_reply_id_idx ON replies (parent_reply_id);
