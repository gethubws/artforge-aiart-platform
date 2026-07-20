USE aiart_codex_platform;

CREATE TABLE IF NOT EXISTS tag_preview (
  id BIGINT PRIMARY KEY,
  tag_id BIGINT NOT NULL,
  image_url VARCHAR(512) NOT NULL,
  preview_type VARCHAR(32) NOT NULL DEFAULT 'EXAMPLE',
  scene_key VARCHAR(32) NOT NULL DEFAULT 'GENERAL',
  title_zh VARCHAR(120),
  prompt_snapshot TEXT,
  sort_order INT NOT NULL DEFAULT 0,
  is_cover TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_tag_preview_image (tag_id, image_url),
  INDEX idx_tag_preview_order (tag_id, is_cover, sort_order),
  CONSTRAINT fk_tag_preview_tag FOREIGN KEY (tag_id) REFERENCES tag(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO tag_preview
  (id, tag_id, image_url, preview_type, scene_key, title_zh, prompt_snapshot, sort_order, is_cover)
SELECT
  tag.id * 1000 + 1,
  tag.id,
  tag.preview_image_url,
  'COVER',
  'GENERAL',
  CONCAT(COALESCE(tag.display_name_zh, tag.name), '封面'),
  tag.prompt_text,
  0,
  1
FROM tag
WHERE tag.preview_image_url IS NOT NULL
  AND tag.preview_image_url <> '';
