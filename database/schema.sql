CREATE DATABASE IF NOT EXISTS aiart_codex_platform
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE aiart_codex_platform;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(120) NOT NULL,
  display_name VARCHAR(80) NOT NULL,
  avatar_url VARCHAR(512),
  role VARCHAR(32) NOT NULL DEFAULT 'USER',
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
  point_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS point_account (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  frozen_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_point_account_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS point_transaction (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  amount DECIMAL(18,2) NOT NULL,
  direction VARCHAR(16) NOT NULL,
  reason VARCHAR(120) NOT NULL,
  reference_type VARCHAR(64),
  reference_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_point_transaction_user_time (user_id, created_at),
  CONSTRAINT fk_point_transaction_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tag_category (
  id BIGINT PRIMARY KEY,
  name VARCHAR(80) NOT NULL,
  slug VARCHAR(80) NOT NULL UNIQUE,
  parent_id BIGINT NOT NULL DEFAULT 0,
  sort_order INT NOT NULL DEFAULT 0,
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_tag_category_parent (parent_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tag (
  id BIGINT PRIMARY KEY,
  category_id BIGINT NOT NULL,
  name VARCHAR(80) NOT NULL,
  prompt_text VARCHAR(255) NOT NULL,
  negative_prompt_text VARCHAR(255),
  preview_image_url VARCHAR(512),
  weight DECIMAL(5,2) NOT NULL DEFAULT 1.00,
  usage_count INT NOT NULL DEFAULT 0,
  visibility VARCHAR(32) NOT NULL DEFAULT 'PUBLIC',
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_tag_category (category_id),
  INDEX idx_tag_status_usage (status, usage_count),
  CONSTRAINT fk_tag_category FOREIGN KEY (category_id) REFERENCES tag_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_generation_job (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  provider VARCHAR(40) NOT NULL DEFAULT 'SD_WEBUI',
  prompt_text TEXT NOT NULL,
  negative_prompt TEXT,
  params_json JSON,
  status VARCHAR(32) NOT NULL DEFAULT 'QUEUED',
  image_url VARCHAR(512),
  error_message TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at DATETIME,
  INDEX idx_generation_user_time (user_id, created_at),
  INDEX idx_generation_job_created (created_at),
  INDEX idx_generation_status (status),
  CONSTRAINT fk_generation_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS artwork (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(120) NOT NULL,
  prompt_text TEXT NOT NULL,
  negative_prompt TEXT,
  image_url VARCHAR(512) NOT NULL,
  thumbnail_url VARCHAR(512),
  generation_params_json JSON,
  source_job_id BIGINT,
  visibility VARCHAR(32) NOT NULL DEFAULT 'PRIVATE',
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_artwork_user_time (user_id, created_at),
  INDEX idx_artwork_visibility_status (visibility, status),
  CONSTRAINT fk_artwork_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS artwork_tag (
  id BIGINT PRIMARY KEY,
  artwork_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_artwork_tag (artwork_id, tag_id),
  INDEX idx_artwork_tag_tag (tag_id, created_at),
  INDEX idx_artwork_tag_user (user_id, created_at),
  CONSTRAINT fk_artwork_tag_artwork FOREIGN KEY (artwork_id) REFERENCES artwork(id),
  CONSTRAINT fk_artwork_tag_tag FOREIGN KEY (tag_id) REFERENCES tag(id),
  CONSTRAINT fk_artwork_tag_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS style_package (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  description TEXT,
  cover_image_url VARCHAR(512),
  prompt_template TEXT,
  negative_prompt_template TEXT,
  price_points DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_style_package_user (user_id),
  INDEX idx_style_package_status (status),
  CONSTRAINT fk_style_package_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS style_package_version (
  id BIGINT PRIMARY KEY,
  style_package_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  version_number INT NOT NULL,
  name VARCHAR(120) NOT NULL,
  description TEXT,
  cover_image_url VARCHAR(512),
  prompt_template TEXT,
  negative_prompt_template TEXT,
  price_points DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  change_note VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_style_package_version (style_package_id, version_number),
  INDEX idx_style_version_package_time (style_package_id, created_at),
  INDEX idx_style_version_user_time (user_id, created_at),
  CONSTRAINT fk_style_version_package FOREIGN KEY (style_package_id) REFERENCES style_package(id),
  CONSTRAINT fk_style_version_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS style_package_review (
  id BIGINT PRIMARY KEY,
  style_package_id BIGINT NOT NULL,
  reviewer_id BIGINT NOT NULL,
  rating INT NOT NULL,
  comment TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_style_review_user (style_package_id, reviewer_id),
  INDEX idx_style_review_package_time (style_package_id, created_at),
  INDEX idx_style_review_reviewer_time (reviewer_id, created_at),
  CONSTRAINT fk_style_review_package FOREIGN KEY (style_package_id) REFERENCES style_package(id),
  CONSTRAINT fk_style_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id),
  CONSTRAINT chk_style_review_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS style_package_access (
  id BIGINT PRIMARY KEY,
  style_package_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  paid_points DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_style_package_access_user (style_package_id, user_id),
  INDEX idx_style_package_access_user (user_id),
  CONSTRAINT fk_style_package_access_package FOREIGN KEY (style_package_id) REFERENCES style_package(id),
  CONSTRAINT fk_style_package_access_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS style_package_submission (
  id BIGINT PRIMARY KEY,
  style_package_id BIGINT NOT NULL,
  submitter_id BIGINT NOT NULL,
  artwork_id BIGINT NOT NULL,
  note TEXT,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  review_comment TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  reviewed_at DATETIME,
  INDEX idx_style_submission_package (style_package_id, created_at),
  INDEX idx_style_submission_submitter (submitter_id, created_at),
  INDEX idx_style_submission_status (status, created_at),
  CONSTRAINT fk_style_submission_package FOREIGN KEY (style_package_id) REFERENCES style_package(id),
  CONSTRAINT fk_style_submission_submitter FOREIGN KEY (submitter_id) REFERENCES users(id),
  CONSTRAINT fk_style_submission_artwork FOREIGN KEY (artwork_id) REFERENCES artwork(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS enterprise_task (
  id BIGINT PRIMARY KEY,
  publisher_id BIGINT NOT NULL,
  title VARCHAR(140) NOT NULL,
  description TEXT,
  requirements_json JSON,
  budget_points DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  deadline DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_enterprise_task_status (status),
  INDEX idx_enterprise_task_publisher (publisher_id),
  CONSTRAINT fk_enterprise_task_publisher FOREIGN KEY (publisher_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS enterprise_task_submission (
  id BIGINT PRIMARY KEY,
  task_id BIGINT NOT NULL,
  submitter_id BIGINT NOT NULL,
  artwork_id BIGINT NOT NULL,
  note TEXT,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  reward_points DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  review_comment TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  reviewed_at DATETIME,
  INDEX idx_task_submission_task (task_id, created_at),
  INDEX idx_task_submission_submitter (submitter_id, created_at),
  INDEX idx_task_submission_status (status, created_at),
  CONSTRAINT fk_task_submission_task FOREIGN KEY (task_id) REFERENCES enterprise_task(id),
  CONSTRAINT fk_task_submission_submitter FOREIGN KEY (submitter_id) REFERENCES users(id),
  CONSTRAINT fk_task_submission_artwork FOREIGN KEY (artwork_id) REFERENCES artwork(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS content_audit (
  id BIGINT PRIMARY KEY,
  content_type VARCHAR(40) NOT NULL,
  content_id BIGINT NOT NULL,
  submitter_id BIGINT NOT NULL,
  auditor_id BIGINT,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  comment_text TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  audited_at DATETIME,
  INDEX idx_content_audit_status (status, created_at),
  INDEX idx_content_audit_content (content_type, content_id),
  CONSTRAINT fk_content_audit_submitter FOREIGN KEY (submitter_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO tag_category (id, name, slug, parent_id, sort_order) VALUES
  (1001, 'Style', 'style', 0, 1),
  (1002, 'Subject', 'subject', 0, 2),
  (1003, 'Lighting', 'lighting', 0, 3),
  (1004, 'Composition', 'composition', 0, 4),
  (1005, 'Quality', 'quality', 0, 5);

INSERT IGNORE INTO tag (id, category_id, name, prompt_text, negative_prompt_text, preview_image_url, weight, usage_count) VALUES
  (2001, 1001, 'cinematic', 'cinematic style', NULL, NULL, 1.20, 0),
  (2002, 1001, 'watercolor', 'watercolor illustration', NULL, NULL, 1.00, 0),
  (2003, 1001, 'cyberpunk', 'cyberpunk aesthetic', NULL, NULL, 1.10, 0),
  (2004, 1002, 'character', 'detailed character portrait', NULL, NULL, 1.10, 0),
  (2005, 1002, 'landscape', 'wide landscape scene', NULL, NULL, 1.00, 0),
  (2006, 1003, 'soft light', 'soft diffused lighting', NULL, NULL, 1.00, 0),
  (2007, 1003, 'rim light', 'dramatic rim lighting', NULL, NULL, 1.10, 0),
  (2008, 1004, 'close-up', 'close-up composition', NULL, NULL, 1.00, 0),
  (2009, 1004, 'wide shot', 'wide angle composition', NULL, NULL, 1.00, 0),
  (2010, 1005, 'high detail', 'highly detailed, sharp focus', 'low quality, blurry', NULL, 1.30, 0);
