-- 社内つぶやきボード V4: 投稿者アバター色

ALTER TABLE posts ADD avatar_color VARCHAR2(20 CHAR) DEFAULT 'red' NOT NULL;
