USE aiart_codex_platform;

CREATE TABLE IF NOT EXISTS tag_combination_stat (
  id BIGINT PRIMARY KEY,
  tag_a_id BIGINT NOT NULL,
  tag_b_id BIGINT NOT NULL,
  usage_count BIGINT NOT NULL DEFAULT 0,
  last_used_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_tag_combination (tag_a_id, tag_b_id),
  INDEX idx_tag_combination_usage (usage_count, last_used_at),
  CONSTRAINT fk_tag_combination_a FOREIGN KEY (tag_a_id) REFERENCES tag(id),
  CONSTRAINT fk_tag_combination_b FOREIGN KEY (tag_b_id) REFERENCES tag(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tag_search_log (
  id BIGINT PRIMARY KEY,
  category_id BIGINT,
  keyword VARCHAR(120) NOT NULL,
  result_count BIGINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_tag_search_keyword_time (keyword, created_at),
  INDEX idx_tag_search_result_time (result_count, created_at),
  CONSTRAINT fk_tag_search_category FOREIGN KEY (category_id) REFERENCES tag_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
