-- =========================================================================
-- 社内つぶやきボード V7: 累積いいね数
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
-- =========================================================================

ALTER TABLE post_likes DROP CONSTRAINT post_likes_post_client_uk;

ALTER TABLE posts ADD likes_count NUMBER(19) DEFAULT 0 NOT NULL;

UPDATE posts p
SET likes_count = (
    SELECT COUNT(*)
    FROM post_likes pl
    WHERE pl.post_id = p.id
);
