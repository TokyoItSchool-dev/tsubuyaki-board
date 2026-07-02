-- =========================================================================
-- 社内つぶやきボード V4: TAG テーブル
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
-- =========================================================================

CREATE SEQUENCE seq_tag START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE tag (
    id          NUMBER(19)         PRIMARY KEY,
    post_id     NUMBER(19)         NOT NULL,
    tag_name    VARCHAR2(30 CHAR)  NOT NULL,
    created_at  TIMESTAMP(6)       NOT NULL
);

CREATE INDEX tag_post_id_idx ON tag (post_id);
CREATE INDEX tag_name_idx ON tag (tag_name);
