-- Adds optional color or image avatar information to posts.
ALTER TABLE posts ADD avatar_color VARCHAR2(7 CHAR);
ALTER TABLE posts ADD avatar_image_content_type VARCHAR2(100 CHAR);
ALTER TABLE posts ADD avatar_image_data BLOB;
