-- =========================================================================
-- 社内つぶやきボード V3: User マスタと POSTS.user_id を追加
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
-- =========================================================================

CREATE SEQUENCE app_users_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE app_users (
    id            NUMBER(19)        NOT NULL,
    name          VARCHAR2(30 CHAR) NOT NULL,
    avatar_color  VARCHAR2(7 CHAR)  DEFAULT '#6b7280' NOT NULL,
    CONSTRAINT app_users_pk PRIMARY KEY (id),
    CONSTRAINT app_users_name_uk UNIQUE (name)
);

INSERT INTO app_users (id, name, avatar_color)
SELECT app_users_seq.NEXTVAL, author, '#6b7280'
FROM (SELECT DISTINCT author FROM posts);

ALTER TABLE posts ADD user_id NUMBER(19);

UPDATE posts p
SET user_id = (
    SELECT u.id
    FROM app_users u
    WHERE u.name = p.author
);

ALTER TABLE posts MODIFY user_id NOT NULL;

ALTER TABLE posts ADD CONSTRAINT posts_user_fk FOREIGN KEY (user_id) REFERENCES app_users (id);

CREATE INDEX posts_user_id_idx ON posts (user_id);

ALTER TABLE posts DROP COLUMN author;
