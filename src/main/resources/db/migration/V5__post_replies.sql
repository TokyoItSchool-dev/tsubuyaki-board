-- =========================================================================
-- 社内つぶやきボード V5: POST_REPLIES テーブル
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
-- =========================================================================

CREATE SEQUENCE post_replies_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE post_replies (
    id            NUMBER(19)         NOT NULL,
    post_id       NUMBER(19)         NOT NULL,
    author        VARCHAR2(30 CHAR)  NOT NULL,
    body          VARCHAR2(280 CHAR) NOT NULL,
    avatar_color  VARCHAR2(20 CHAR)  DEFAULT 'gray' NOT NULL,
    created_at    TIMESTAMP(6)       NOT NULL,
    CONSTRAINT post_replies_pk PRIMARY KEY (id),
    CONSTRAINT post_replies_post_fk FOREIGN KEY (post_id) REFERENCES posts (id)
);

CREATE INDEX post_replies_post_created_at_idx ON post_replies (post_id, created_at);
