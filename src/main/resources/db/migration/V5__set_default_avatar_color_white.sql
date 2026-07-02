-- =========================================================================
-- 社内つぶやきボード V5: アバター色のデフォルトを白に変更
-- Oracle XE 21c および H2(MODE=Oracle) の双方で動く DDL
-- =========================================================================

ALTER TABLE posts MODIFY avatar_color VARCHAR2(7 CHAR) DEFAULT '#ffffff';

UPDATE posts
SET avatar_color = '#ffffff'
WHERE avatar_color = '#cccccc';
