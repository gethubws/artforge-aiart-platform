SET @db = DATABASE();

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'style_package' AND column_name = 'style_statement'
  ),
  'SELECT 1',
  'ALTER TABLE style_package ADD COLUMN style_statement TEXT AFTER cover_image_url'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'style_package' AND column_name = 'prompt_guide'
  ),
  'SELECT 1',
  'ALTER TABLE style_package ADD COLUMN prompt_guide TEXT AFTER style_statement'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'style_package' AND column_name = 'negative_prompt_guide'
  ),
  'SELECT 1',
  'ALTER TABLE style_package ADD COLUMN negative_prompt_guide TEXT AFTER prompt_guide'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'style_package' AND column_name = 'featured_artwork_id'
  ),
  'SELECT 1',
  'ALTER TABLE style_package ADD COLUMN featured_artwork_id BIGINT NULL AFTER negative_prompt_guide'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'style_package' AND column_name = 'artwork_count'
  ),
  'SELECT 1',
  'ALTER TABLE style_package ADD COLUMN artwork_count INT NOT NULL DEFAULT 0 AFTER featured_artwork_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'style_package_version' AND column_name = 'style_statement'
  ),
  'SELECT 1',
  'ALTER TABLE style_package_version ADD COLUMN style_statement TEXT AFTER cover_image_url'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'style_package_version' AND column_name = 'prompt_guide'
  ),
  'SELECT 1',
  'ALTER TABLE style_package_version ADD COLUMN prompt_guide TEXT AFTER style_statement'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'style_package_version' AND column_name = 'negative_prompt_guide'
  ),
  'SELECT 1',
  'ALTER TABLE style_package_version ADD COLUMN negative_prompt_guide TEXT AFTER prompt_guide'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'style_package_version' AND column_name = 'featured_artwork_id'
  ),
  'SELECT 1',
  'ALTER TABLE style_package_version ADD COLUMN featured_artwork_id BIGINT NULL AFTER negative_prompt_guide'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'style_package_version' AND column_name = 'artwork_count'
  ),
  'SELECT 1',
  'ALTER TABLE style_package_version ADD COLUMN artwork_count INT NOT NULL DEFAULT 0 AFTER featured_artwork_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'tag' AND column_name = 'display_name_zh'
  ),
  'SELECT 1',
  'ALTER TABLE tag ADD COLUMN display_name_zh VARCHAR(120) NULL AFTER name'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = @db AND table_name = 'tag' AND column_name = 'description_zh'
  ),
  'SELECT 1',
  'ALTER TABLE tag ADD COLUMN description_zh VARCHAR(255) NULL AFTER display_name_zh'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS style_package_tag (
  id BIGINT PRIMARY KEY,
  style_package_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_style_package_tag (style_package_id, tag_id),
  INDEX idx_style_package_tag_tag (tag_id, created_at),
  CONSTRAINT fk_style_package_tag_package FOREIGN KEY (style_package_id) REFERENCES style_package(id),
  CONSTRAINT fk_style_package_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS style_package_collaborator (
  id BIGINT PRIMARY KEY,
  style_package_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role VARCHAR(40) NOT NULL DEFAULT 'CONTRIBUTOR',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_style_package_collaborator (style_package_id, user_id),
  INDEX idx_style_package_collaborator_user (user_id, created_at),
  CONSTRAINT fk_style_package_collaborator_package FOREIGN KEY (style_package_id) REFERENCES style_package(id),
  CONSTRAINT fk_style_package_collaborator_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
