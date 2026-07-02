CREATE SEQUENCE tags_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE tags (
    id       NUMBER(19)        NOT NULL,
    post_id  NUMBER(19)        NOT NULL,
    name     VARCHAR2(64 CHAR) NOT NULL,
    CONSTRAINT tags_pk PRIMARY KEY (id),
    CONSTRAINT tags_post_fk FOREIGN KEY (post_id) REFERENCES posts (id),
    CONSTRAINT tags_post_name_uk UNIQUE (post_id, name)
);

CREATE INDEX tags_name_idx ON tags (name);
CREATE INDEX tags_post_id_idx ON tags (post_id);
