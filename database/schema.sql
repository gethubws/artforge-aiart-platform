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
  display_name_zh VARCHAR(80),
  description_zh VARCHAR(255),
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
  style_statement TEXT,
  prompt_guide TEXT,
  negative_prompt_guide TEXT,
  featured_artwork_id BIGINT,
  artwork_count INT NOT NULL DEFAULT 0,
  price_points DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_style_package_user (user_id),
  INDEX idx_style_package_status (status),
  INDEX idx_style_package_featured_artwork (featured_artwork_id),
  CONSTRAINT fk_style_package_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_style_package_featured_artwork FOREIGN KEY (featured_artwork_id) REFERENCES artwork(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS style_package_version (
  id BIGINT PRIMARY KEY,
  style_package_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  version_number INT NOT NULL,
  name VARCHAR(120) NOT NULL,
  description TEXT,
  cover_image_url VARCHAR(512),
  style_statement TEXT,
  prompt_guide TEXT,
  negative_prompt_guide TEXT,
  featured_artwork_id BIGINT,
  artwork_count INT NOT NULL DEFAULT 0,
  price_points DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  change_note VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_style_package_version (style_package_id, version_number),
  INDEX idx_style_version_package_time (style_package_id, created_at),
  INDEX idx_style_version_user_time (user_id, created_at),
  INDEX idx_style_version_featured_artwork (featured_artwork_id),
  CONSTRAINT fk_style_version_package FOREIGN KEY (style_package_id) REFERENCES style_package(id),
  CONSTRAINT fk_style_version_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_style_version_featured_artwork FOREIGN KEY (featured_artwork_id) REFERENCES artwork(id)
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

CREATE TABLE IF NOT EXISTS user_favorite (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  target_type VARCHAR(32) NOT NULL,
  target_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_favorite_target (user_id, target_type, target_id),
  INDEX idx_user_favorite_user_time (user_id, created_at),
  CONSTRAINT fk_user_favorite_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_subscription (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  target_type VARCHAR(32) NOT NULL,
  target_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_subscription_target (user_id, target_type, target_id),
  INDEX idx_user_subscription_user_time (user_id, created_at),
  INDEX idx_user_subscription_target (target_type, target_id, created_at),
  CONSTRAINT fk_user_subscription_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_notification (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  type VARCHAR(64) NOT NULL,
  title VARCHAR(160) NOT NULL,
  content TEXT,
  related_type VARCHAR(32),
  related_id BIGINT,
  is_read TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  read_at DATETIME,
  INDEX idx_user_notification_user_time (user_id, created_at),
  INDEX idx_user_notification_user_read (user_id, is_read, created_at),
  CONSTRAINT fk_user_notification_user FOREIGN KEY (user_id) REFERENCES users(id)
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

INSERT IGNORE INTO tag (id, category_id, name, display_name_zh, description_zh, prompt_text, negative_prompt_text, preview_image_url, weight, usage_count) VALUES
  (2001, 1001, 'cinematic', '电影感', '强调镜头语言、叙事氛围和胶片质感。', 'cinematic style', NULL, '/images/tags/cinematic.webp', 1.20, 0),
  (2002, 1001, 'watercolor', '水彩', '偏柔和、带纸面渗色感的插画风格。', 'watercolor illustration', NULL, '/images/tags/watercolor.webp', 1.00, 0),
  (2003, 1001, 'cyberpunk', '赛博朋克', '强调霓虹、未来都市、科技感与反乌托邦氛围。', 'cyberpunk aesthetic', NULL, '/images/tags/cyberpunk.webp', 1.10, 0),
  (2004, 1002, 'character', '人物', '适合角色肖像、半身像和人物主体画面。', 'detailed character portrait', NULL, '/images/tags/character.webp', 1.10, 0),
  (2005, 1002, 'landscape', '风景', '适合自然景观、城市景观和大场景画面。', 'wide landscape scene', NULL, '/images/tags/landscape.webp', 1.00, 0),
  (2006, 1003, 'soft light', '柔光', '画面光线更柔和，边缘不过分锐利。', 'soft diffused lighting', NULL, '/images/tags/soft-light.webp', 1.00, 0),
  (2007, 1003, 'rim light', '轮廓光', '强化主体边缘高光和戏剧性光影。', 'dramatic rim lighting', NULL, '/images/tags/rim-light.webp', 1.10, 0),
  (2008, 1004, 'close-up', '近景', '突出主体局部细节与面部情绪。', 'close-up composition', NULL, '/images/tags/close-up.webp', 1.00, 0),
  (2009, 1004, 'wide shot', '远景', '适合展现场景空间关系和整体构图。', 'wide angle composition', NULL, '/images/tags/wide-shot.webp', 1.00, 0),
  (2010, 1005, 'high detail', '高细节', '强调清晰度、细节层次和画面完成度。', 'highly detailed, sharp focus', 'low quality, blurry', '/images/tags/high-detail.webp', 1.30, 0);

INSERT IGNORE INTO tag_category (id, name, slug, parent_id, sort_order) VALUES
  (1006, 'Color', 'color', 0, 6),
  (1007, 'Mood', 'mood', 0, 7);

INSERT IGNORE INTO tag (id, category_id, name, display_name_zh, description_zh, prompt_text, negative_prompt_text, preview_image_url, weight, usage_count) VALUES
  (2011, 1001, 'anime', '日系动画', '强调清晰角色设计、动画式上色和精致背景。', 'anime style, detailed painted background', NULL, '/images/tags/anime.webp', 1.10, 0),
  (2012, 1001, 'oil painting', '油画', '带有层叠笔触、厚涂高光和传统绘画质感。', 'traditional oil painting, visible brushwork', NULL, '/images/tags/oil-painting.webp', 1.05, 0),
  (2013, 1001, 'ink wash', '水墨', '使用墨色层次、留白和氤氲感表现东方画意。', 'Chinese ink wash painting, expressive brushwork', NULL, '/images/tags/ink-wash.webp', 1.10, 0),
  (2014, 1001, 'pixel art', '像素艺术', '使用明确像素块、有限色板和复古游戏画面语言。', 'polished pixel art, crisp pixel clusters', NULL, '/images/tags/pixel-art.webp', 1.00, 0),
  (2015, 1001, '3d render', '三维渲染', '强调立体造型、材质表现和三维空间光影。', 'premium 3D render, physically based materials', NULL, '/images/tags/3d-render.webp', 1.05, 0),
  (2016, 1001, 'fantasy art', '奇幻艺术', '适合魔法世界、架空建筑和富有想象力的场景。', 'fantasy art, enchanted world, magical atmosphere', NULL, '/images/tags/fantasy-art.webp', 1.10, 0),
  (2017, 1001, 'photorealistic', '写实摄影', '追求真实材质、自然光学和可信的摄影质感。', 'photorealistic, natural textures, realistic optics', 'cartoon, illustration, CGI', '/images/tags/photorealistic.webp', 1.20, 0),
  (2018, 1001, 'minimalist', '极简主义', '减少非必要元素，以简洁形状和留白突出主体。', 'minimalist composition, clean geometric forms', 'busy composition, clutter', '/images/tags/minimalist.webp', 1.00, 0),
  (2019, 1002, 'architecture', '建筑', '突出建筑结构、空间关系、尺度与设计细节。', 'detailed architecture, structural design', NULL, '/images/tags/architecture.webp', 1.10, 0),
  (2020, 1002, 'animal', '动物', '适合动物肖像、自然生态和人与动物互动场景。', 'detailed animal subject, natural anatomy', NULL, '/images/tags/animal.webp', 1.05, 0),
  (2021, 1002, 'product', '产品', '突出产品外观、材质、陈列方式和商业展示效果。', 'professional product photography, material detail', NULL, '/images/tags/product.webp', 1.10, 0),
  (2022, 1002, 'food', '美食', '强调食物质感、摆盘、色泽和令人食欲的氛围。', 'appetizing food photography, detailed plating', NULL, '/images/tags/food.webp', 1.05, 0),
  (2023, 1002, 'vehicle', '载具', '适合汽车、飞行器、船只和概念交通工具设计。', 'detailed vehicle design, believable engineering', NULL, '/images/tags/vehicle.webp', 1.05, 0),
  (2024, 1003, 'golden hour', '黄金时刻', '使用日出或日落前后的暖色低角度光线。', 'golden hour lighting, warm low-angle sunlight', NULL, '/images/tags/golden-hour.webp', 1.10, 0),
  (2025, 1003, 'neon light', '霓虹灯光', '使用青色、洋红等高饱和人工光营造都市感。', 'vivid neon lighting, cyan and magenta glow', NULL, '/images/tags/neon-light.webp', 1.05, 0),
  (2026, 1003, 'moonlight', '月光', '使用冷色银蓝光和柔和夜间阴影塑造静谧感。', 'cool silver moonlight, soft blue shadows', NULL, '/images/tags/moonlight.webp', 1.05, 0),
  (2027, 1003, 'volumetric light', '体积光', '表现穿过云雾或尘埃的清晰光束和空间层次。', 'volumetric lighting, visible light rays, atmospheric haze', NULL, '/images/tags/volumetric-light.webp', 1.15, 0),
  (2028, 1003, 'studio light', '棚拍光', '使用可控主光、补光和轮廓光塑造专业棚拍效果。', 'professional studio lighting, controlled key and fill light', NULL, '/images/tags/studio-light.webp', 1.10, 0),
  (2029, 1004, 'bird''s-eye view', '鸟瞰视角', '从高处向下观察主体，强调场景布局与空间关系。', 'bird''s-eye view, top-down aerial composition', NULL, '/images/tags/birds-eye-view.webp', 1.05, 0),
  (2030, 1004, 'low angle', '低机位', '从较低位置仰拍主体，增强高度、力量和戏剧性。', 'low-angle shot, upward perspective', NULL, '/images/tags/low-angle.webp', 1.05, 0),
  (2031, 1004, 'symmetrical composition', '对称构图', '以中轴和均衡关系建立稳定、庄重的画面秩序。', 'symmetrical composition, balanced central framing', NULL, '/images/tags/symmetrical-composition.webp', 1.00, 0),
  (2032, 1004, 'rule of thirds', '三分法', '将主体放在三分线或交点附近，让画面更自然平衡。', 'rule of thirds composition, balanced subject placement', NULL, '/images/tags/rule-of-thirds.webp', 1.00, 0),
  (2033, 1005, 'clean lines', '清晰线稿', '强调稳定轮廓、干净线条和易于识别的形状。', 'clean confident linework, crisp readable shapes', 'messy lines, rough sketch', '/images/tags/clean-lines.webp', 1.10, 0),
  (2034, 1005, 'professional color grading', '专业调色', '统一明暗、肤色与色彩关系，形成成熟的后期观感。', 'professional color grading, controlled contrast, cohesive colors', NULL, '/images/tags/professional-color-grading.webp', 1.10, 0),
  (2035, 1006, 'warm palette', '暖色调', '以琥珀、陶土、暖白和柔和红色营造亲近感。', 'warm color palette, amber and terracotta tones', NULL, '/images/tags/warm-palette.webp', 1.00, 0),
  (2036, 1006, 'cool palette', '冷色调', '以蓝、青、灰紫等冷色建立清爽或冷静的画面。', 'cool color palette, blue teal and slate tones', NULL, '/images/tags/cool-palette.webp', 1.00, 0),
  (2037, 1006, 'pastel colors', '粉彩色', '使用低饱和浅色，让画面显得柔和、轻盈和清新。', 'soft pastel colors, powder blue mint and lavender', NULL, '/images/tags/pastel-colors.webp', 1.00, 0),
  (2038, 1007, 'dreamy', '梦幻', '通过柔光、薄雾和超现实细节营造轻盈梦境感。', 'dreamy ethereal atmosphere, luminous mist', NULL, '/images/tags/dreamy.webp', 1.05, 0),
  (2039, 1007, 'mysterious', '神秘', '使用雾气、隐藏信息和克制光线制造未知与悬念。', 'mysterious atmosphere, fog, hidden silhouettes', NULL, '/images/tags/mysterious.webp', 1.05, 0),
  (2040, 1007, 'epic', '史诗感', '强调宏大尺度、戏剧天空和具有英雄感的场面。', 'epic atmosphere, monumental scale, dramatic sky', NULL, '/images/tags/epic.webp', 1.15, 0);

INSERT IGNORE INTO tag_category (id, name, slug, parent_id, sort_order) VALUES
  (1008, 'Camera', 'camera', 0, 8),
  (1009, 'Material', 'material', 0, 9),
  (1010, 'Environment', 'environment', 0, 10),
  (1011, 'Culture', 'culture', 0, 11);

INSERT IGNORE INTO tag (id, category_id, name, display_name_zh, description_zh, prompt_text, negative_prompt_text, preview_image_url, weight, usage_count) VALUES
  (2041, 1001, 'engraving', '版画', '通过密集排线、刻痕和黑白层次形成传统版画质感。', 'black ink engraving, dense cross-hatching, carved line texture', NULL, '/images/tags/engraving.webp', 1.10, 0),
  (2042, 1001, 'pencil sketch', '铅笔素描', '使用石墨线条、轻重排线和纸张颗粒表现手绘结构。', 'graphite pencil sketch, visible construction lines, paper grain', NULL, '/images/tags/pencil-sketch.webp', 1.05, 0),
  (2043, 1001, 'cel shading', '赛璐璐上色', '使用清晰轮廓、分区阴影和动画式平涂色块。', 'cel-shaded illustration, crisp outlines, flat color blocks', NULL, '/images/tags/cel-shading.webp', 1.05, 0),
  (2044, 1001, 'claymation', '黏土动画', '呈现手工黏土塑形、轻微指纹和定格动画质感。', 'claymation style, handcrafted clay figures, stop-motion texture', NULL, '/images/tags/claymation.webp', 1.10, 0),
  (2045, 1001, 'low poly', '低多边形', '使用明确多边形切面、简化造型和几何明暗关系。', 'low-poly 3D art, faceted geometry, simplified forms', NULL, '/images/tags/low-poly.webp', 1.05, 0),
  (2046, 1001, 'paper cut', '纸艺剪纸', '使用分层纸片、清晰剪裁边缘和手工阴影表现画面。', 'layered paper-cut art, handcrafted paper edges, cut-paper depth', NULL, '/images/tags/paper-cut.webp', 1.10, 0),
  (2047, 1001, 'retro poster', '复古海报', '使用有限色板、粗颗粒印刷和复古图形设计语言。', 'retro poster illustration, limited color palette, screen-print texture', NULL, '/images/tags/retro-poster.webp', 1.05, 0),
  (2048, 1001, 'manga line art', '漫画线稿', '强调黑白墨线、速度线、网点和清晰角色轮廓。', 'manga line art, expressive ink lines, screentone shading', NULL, '/images/tags/manga-line-art.webp', 1.10, 0),
  (2049, 1008, 'macro photography', '微距摄影', '极近距离呈现微小主体与材质细节，背景通常明显虚化。', 'macro photography, extreme close focus, fine surface detail', NULL, '/images/tags/macro-photography.webp', 1.10, 0),
  (2050, 1008, 'shallow depth of field', '浅景深', '让主体清晰、前后景柔和虚化，突出视觉焦点。', 'shallow depth of field, sharp subject, softly blurred background', NULL, '/images/tags/shallow-depth-of-field.webp', 1.05, 0),
  (2051, 1008, 'wide-angle lens', '广角镜头', '扩大空间感和透视关系，适合建筑、室内与宏大环境。', 'wide-angle lens, expansive perspective, strong spatial depth', 'telephoto compression', '/images/tags/wide-angle-lens.webp', 1.05, 0),
  (2052, 1008, 'telephoto lens', '长焦镜头', '压缩远近距离并突出远处主体，形成平整的空间层次。', 'telephoto lens, compressed perspective, isolated distant subject', 'wide-angle distortion', '/images/tags/telephoto-lens.webp', 1.05, 0),
  (2053, 1008, 'fisheye lens', '鱼眼镜头', '使用强烈桶形畸变和超广视野营造夸张动感。', 'fisheye lens, dramatic barrel distortion, ultra-wide field of view', NULL, '/images/tags/fisheye-lens.webp', 1.05, 0),
  (2054, 1008, 'tilt-shift photography', '移轴摄影', '通过选择性焦平面与俯视构图产生微缩模型般的效果。', 'tilt-shift photography, selective focus plane, miniature effect', NULL, '/images/tags/tilt-shift-photography.webp', 1.05, 0),
  (2055, 1008, 'motion blur', '动态模糊', '保留运动方向上的拖影，表达速度、移动与追随拍摄感。', 'intentional motion blur, directional movement streaks, sense of speed', 'static frozen motion', '/images/tags/motion-blur.webp', 1.05, 0),
  (2056, 1009, 'glass material', '玻璃材质', '强调透明折射、边缘高光、内部反射与真实厚度。', 'realistic glass material, refraction, edge highlights, optical thickness', NULL, '/images/tags/glass-material.webp', 1.10, 0),
  (2057, 1009, 'metallic material', '金属材质', '强调金属反射、拉丝或抛光表面以及环境映射。', 'realistic metallic material, polished reflections, brushed metal detail', NULL, '/images/tags/metallic-material.webp', 1.10, 0),
  (2058, 1009, 'ceramic material', '陶瓷材质', '表现釉面高光、烧制纹理、细小瑕疵与厚实质感。', 'glazed ceramic material, fired surface texture, subtle imperfections', NULL, '/images/tags/ceramic-material.webp', 1.05, 0),
  (2059, 1009, 'fabric texture', '织物纹理', '突出纤维、编织结构、褶皱和柔软表面。', 'detailed fabric texture, visible fibers, woven structure, soft folds', NULL, '/images/tags/fabric-texture.webp', 1.05, 0),
  (2060, 1009, 'wood texture', '木材纹理', '表现年轮、木纹方向、雕刻痕迹和自然表面变化。', 'natural wood texture, visible grain, carved detail, organic variation', NULL, '/images/tags/wood-texture.webp', 1.05, 0),
  (2061, 1009, 'translucent material', '半透明材质', '允许部分光线穿透，呈现柔和内部散射与层次。', 'translucent material, subsurface light scattering, soft internal glow', NULL, '/images/tags/translucent-material.webp', 1.10, 0),
  (2062, 1009, 'frosted material', '磨砂材质', '使用细腻粗糙表面散射光线，形成柔和朦胧的透光感。', 'frosted material, diffused transmission, fine matte surface', NULL, '/images/tags/frosted-material.webp', 1.05, 0);

INSERT IGNORE INTO tag (id, category_id, name, display_name_zh, description_zh, prompt_text, negative_prompt_text, preview_image_url, weight, usage_count) VALUES
  (2063, 1010, 'rainy weather', '雨天', '加入降雨、湿润表面、雨雾和阴天环境光。', 'rainy weather, falling rain, wet reflective surfaces, overcast light', 'dry weather, clear sky', '/images/tags/rainy-weather.webp', 1.05, 0),
  (2064, 1010, 'snowy scene', '雪景', '使用积雪、飘雪、冷空气与冬季地表反光塑造环境。', 'snowy scene, fresh snow, falling snowflakes, cold winter atmosphere', 'summer vegetation, warm weather', '/images/tags/snowy-scene.webp', 1.05, 0),
  (2065, 1010, 'foggy atmosphere', '雾天', '通过远景衰减、低对比和雾气层次增加空间深度。', 'foggy atmosphere, layered mist, low contrast distance, atmospheric depth', 'crystal clear distance', '/images/tags/foggy-atmosphere.webp', 1.05, 0),
  (2066, 1010, 'underwater world', '水下世界', '表现水下光束、悬浮颗粒、流动水体与海洋环境。', 'underwater world, caustic light rays, suspended particles, flowing water', 'dry land, open air', '/images/tags/underwater-world.webp', 1.10, 0),
  (2067, 1010, 'outer space', '外太空', '加入星空、行星、失重感和宇宙尺度的深色背景。', 'outer space, distant planets, dense star field, zero-gravity atmosphere', 'daytime earth landscape', '/images/tags/outer-space.webp', 1.10, 0),
  (2068, 1010, 'ancient ruins', '古代遗迹', '使用风化石材、断壁、植物侵蚀和历史痕迹构建遗迹场景。', 'ancient ruins, weathered stone, broken arches, overgrown historical remains', 'modern new construction', '/images/tags/ancient-ruins.webp', 1.05, 0),
  (2069, 1010, 'desert landscape', '沙漠', '表现沙丘、风蚀地貌、热空气和干燥辽阔的空间。', 'desert landscape, sweeping dunes, wind-shaped sand, dry vast horizon', 'lush wet vegetation', '/images/tags/desert-landscape.webp', 1.05, 0),
  (2070, 1010, 'lush forest', '茂密森林', '使用层叠植被、潮湿地表、林间光和丰富绿色层次。', 'lush dense forest, layered vegetation, mossy ground, filtered forest light', 'barren landscape', '/images/tags/lush-forest.webp', 1.05, 0),
  (2071, 1011, 'steampunk', '蒸汽朋克', '融合黄铜机械、蒸汽动力、齿轮和维多利亚工业设计。', 'steampunk aesthetic, brass machinery, steam power, gears, Victorian engineering', NULL, '/images/tags/steampunk.webp', 1.10, 0),
  (2072, 1011, 'art deco', '装饰艺术', '使用几何对称、金属线条、阶梯形纹样和奢华装饰。', 'Art Deco design, geometric symmetry, metallic accents, stepped ornament', NULL, '/images/tags/art-deco.webp', 1.10, 0),
  (2073, 1011, 'art nouveau', '新艺术', '使用植物曲线、花卉纹样、流动轮廓和手工装饰。', 'Art Nouveau style, flowing botanical curves, floral ornament, elegant linework', NULL, '/images/tags/art-nouveau.webp', 1.10, 0),
  (2074, 1011, 'bauhaus', '包豪斯', '强调功能、基础几何、清晰网格和克制的现代配色。', 'Bauhaus design, functional geometry, clear grid, primary color accents', 'ornate decoration', '/images/tags/bauhaus.webp', 1.05, 0),
  (2075, 1011, 'Song dynasty painting', '宋画', '借鉴宋代山水与花鸟画的细腻观察、留白和淡雅设色。', 'Song dynasty Chinese painting, refined brushwork, poetic negative space, muted mineral colors', NULL, '/images/tags/song-dynasty-painting.webp', 1.10, 0),
  (2076, 1011, 'ukiyo-e', '浮世绘', '使用木版套色、平涂色块、明确轮廓和日式传统构图。', 'ukiyo-e woodblock print, flat color areas, bold contours, traditional Japanese composition', NULL, '/images/tags/ukiyo-e.webp', 1.10, 0),
  (2077, 1011, 'Y2K aesthetic', 'Y2K 美学', '使用千禧年科技感、透明塑料、金属色和早期数字界面语言。', 'Y2K aesthetic, translucent plastic, chrome accents, optimistic early-digital design', NULL, '/images/tags/y2k-aesthetic.webp', 1.05, 0),
  (2078, 1011, 'brutalism', '粗野主义', '强调裸露混凝土、厚重体块、结构重复和强烈尺度感。', 'brutalist architecture, exposed concrete, massive geometric volumes, structural repetition', 'delicate ornament', '/images/tags/brutalism.webp', 1.10, 0),
  (2079, 1011, 'rococo', '洛可可', '使用轻盈曲线、贝壳纹、粉彩装饰和精致宫廷细节。', 'Rococo style, ornate shell motifs, pastel decoration, elegant courtly detail', NULL, '/images/tags/rococo.webp', 1.10, 0),
  (2080, 1011, 'synthwave', '合成波', '使用霓虹落日、紫蓝网格、复古未来主义和八十年代电子氛围。', 'synthwave aesthetic, neon sunset, retro grid horizon, 1980s retro-futurism', NULL, '/images/tags/synthwave.webp', 1.10, 0);

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

INSERT IGNORE INTO tag_preview
  (id, tag_id, image_url, preview_type, scene_key, title_zh, prompt_snapshot, sort_order, is_cover)
SELECT
  tag.id * 1000 + scene.id_offset,
  tag.id,
  CONCAT(
    '/images/tags/gallery/',
    CASE tag.id
      WHEN 2001 THEN 'cinematic'
      WHEN 2002 THEN 'watercolor'
      WHEN 2003 THEN 'cyberpunk'
      WHEN 2011 THEN 'anime'
      WHEN 2012 THEN 'oil-painting'
      WHEN 2013 THEN 'ink-wash'
      WHEN 2014 THEN 'pixel-art'
      WHEN 2015 THEN '3d-render'
      WHEN 2016 THEN 'fantasy-art'
      WHEN 2017 THEN 'photorealistic'
      WHEN 2018 THEN 'minimalist'
    END,
    '/', scene.file_name, '.webp'),
  'COMPARISON',
  scene.scene_key,
  scene.title_zh,
  tag.prompt_text,
  scene.sort_order,
  0
FROM tag
CROSS JOIN (
  SELECT 2 AS id_offset, 'character' AS file_name, 'CHARACTER' AS scene_key, '人物对比' AS title_zh, 10 AS sort_order
  UNION ALL
  SELECT 3, 'landscape', 'LANDSCAPE', '风景对比', 20
  UNION ALL
  SELECT 4, 'object', 'OBJECT', '物品对比', 30
) scene
WHERE tag.id IN (2001, 2002, 2003, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018);

INSERT IGNORE INTO tag_preview
  (id, tag_id, image_url, preview_type, scene_key, title_zh, prompt_snapshot, sort_order, is_cover)
VALUES
  (2006002, 2006, '/images/tags/gallery/soft-light/character.webp', 'COMPARISON', 'CHARACTER', '人物光线对比', 'soft diffused lighting', 10, 0),
  (2006003, 2006, '/images/tags/gallery/soft-light/interior.webp', 'COMPARISON', 'INTERIOR', '室内光线对比', 'soft diffused lighting', 20, 0),
  (2007002, 2007, '/images/tags/gallery/rim-light/character.webp', 'COMPARISON', 'CHARACTER', '人物光线对比', 'dramatic rim lighting', 10, 0),
  (2007003, 2007, '/images/tags/gallery/rim-light/interior.webp', 'COMPARISON', 'INTERIOR', '室内光线对比', 'dramatic rim lighting', 20, 0),
  (2024002, 2024, '/images/tags/gallery/golden-hour/character.webp', 'COMPARISON', 'CHARACTER', '人物光线对比', 'golden hour lighting', 10, 0),
  (2024003, 2024, '/images/tags/gallery/golden-hour/interior.webp', 'COMPARISON', 'INTERIOR', '室内光线对比', 'golden hour lighting', 20, 0),
  (2025002, 2025, '/images/tags/gallery/neon-light/character.webp', 'COMPARISON', 'CHARACTER', '人物光线对比', 'vivid neon lighting', 10, 0),
  (2025003, 2025, '/images/tags/gallery/neon-light/interior.webp', 'COMPARISON', 'INTERIOR', '室内光线对比', 'vivid neon lighting', 20, 0),
  (2026002, 2026, '/images/tags/gallery/moonlight/character.webp', 'COMPARISON', 'CHARACTER', '人物光线对比', 'cool silver moonlight', 10, 0),
  (2026003, 2026, '/images/tags/gallery/moonlight/interior.webp', 'COMPARISON', 'INTERIOR', '室内光线对比', 'cool silver moonlight', 20, 0),
  (2027002, 2027, '/images/tags/gallery/volumetric-light/character.webp', 'COMPARISON', 'CHARACTER', '人物光线对比', 'volumetric lighting', 10, 0),
  (2027003, 2027, '/images/tags/gallery/volumetric-light/interior.webp', 'COMPARISON', 'INTERIOR', '室内光线对比', 'volumetric lighting', 20, 0),
  (2028002, 2028, '/images/tags/gallery/studio-light/character.webp', 'COMPARISON', 'CHARACTER', '人物光线对比', 'professional studio lighting', 10, 0),
  (2028003, 2028, '/images/tags/gallery/studio-light/interior.webp', 'COMPARISON', 'INTERIOR', '室内光线对比', 'professional studio lighting', 20, 0),
  (2008002, 2008, '/images/tags/gallery/close-up/character.webp', 'COMPARISON', 'CHARACTER', '人物构图对比', 'close-up composition', 10, 0),
  (2008003, 2008, '/images/tags/gallery/close-up/architecture.webp', 'COMPARISON', 'ARCHITECTURE', '建筑构图对比', 'close-up composition', 20, 0),
  (2009002, 2009, '/images/tags/gallery/wide-shot/character.webp', 'COMPARISON', 'CHARACTER', '人物构图对比', 'wide angle composition', 10, 0),
  (2009003, 2009, '/images/tags/gallery/wide-shot/architecture.webp', 'COMPARISON', 'ARCHITECTURE', '建筑构图对比', 'wide angle composition', 20, 0),
  (2029002, 2029, '/images/tags/gallery/birds-eye-view/character.webp', 'COMPARISON', 'CHARACTER', '人物构图对比', 'bird''s-eye view', 10, 0),
  (2029003, 2029, '/images/tags/gallery/birds-eye-view/architecture.webp', 'COMPARISON', 'ARCHITECTURE', '建筑构图对比', 'bird''s-eye view', 20, 0),
  (2030002, 2030, '/images/tags/gallery/low-angle/character.webp', 'COMPARISON', 'CHARACTER', '人物构图对比', 'low-angle shot', 10, 0),
  (2030003, 2030, '/images/tags/gallery/low-angle/architecture.webp', 'COMPARISON', 'ARCHITECTURE', '建筑构图对比', 'low-angle shot', 20, 0),
  (2031002, 2031, '/images/tags/gallery/symmetrical-composition/character.webp', 'COMPARISON', 'CHARACTER', '人物构图对比', 'symmetrical composition', 10, 0),
  (2031003, 2031, '/images/tags/gallery/symmetrical-composition/architecture.webp', 'COMPARISON', 'ARCHITECTURE', '建筑构图对比', 'symmetrical composition', 20, 0),
  (2032002, 2032, '/images/tags/gallery/rule-of-thirds/character.webp', 'COMPARISON', 'CHARACTER', '人物构图对比', 'rule of thirds composition', 10, 0),
  (2032003, 2032, '/images/tags/gallery/rule-of-thirds/architecture.webp', 'COMPARISON', 'ARCHITECTURE', '建筑构图对比', 'rule of thirds composition', 20, 0),
  (2035002, 2035, '/images/tags/gallery/warm-palette/character.webp', 'COMPARISON', 'CHARACTER', '人物色彩对比', 'warm color palette', 10, 0),
  (2035003, 2035, '/images/tags/gallery/warm-palette/still-life.webp', 'COMPARISON', 'STILL_LIFE', '静物色彩对比', 'warm color palette', 20, 0),
  (2036002, 2036, '/images/tags/gallery/cool-palette/character.webp', 'COMPARISON', 'CHARACTER', '人物色彩对比', 'cool color palette', 10, 0),
  (2036003, 2036, '/images/tags/gallery/cool-palette/still-life.webp', 'COMPARISON', 'STILL_LIFE', '静物色彩对比', 'cool color palette', 20, 0),
  (2037002, 2037, '/images/tags/gallery/pastel-colors/character.webp', 'COMPARISON', 'CHARACTER', '人物色彩对比', 'soft pastel colors', 10, 0),
  (2037003, 2037, '/images/tags/gallery/pastel-colors/still-life.webp', 'COMPARISON', 'STILL_LIFE', '静物色彩对比', 'soft pastel colors', 20, 0);

INSERT IGNORE INTO tag_preview
  (id, tag_id, image_url, preview_type, scene_key, title_zh, prompt_snapshot, sort_order, is_cover)
VALUES
  (2004002, 2004, '/images/tags/gallery/character/portrait.webp', 'EXAMPLE', 'CHARACTER', '人物肖像示例', 'detailed character portrait', 10, 0),
  (2004003, 2004, '/images/tags/gallery/character/full-body.webp', 'EXAMPLE', 'FULL_BODY', '全身角色示例', 'detailed character portrait', 20, 0),
  (2005002, 2005, '/images/tags/gallery/landscape/nature.webp', 'EXAMPLE', 'LANDSCAPE', '自然风景示例', 'wide landscape scene', 10, 0),
  (2005003, 2005, '/images/tags/gallery/landscape/urban.webp', 'EXAMPLE', 'URBAN', '城市景观示例', 'wide landscape scene', 20, 0),
  (2019002, 2019, '/images/tags/gallery/architecture/exterior.webp', 'EXAMPLE', 'EXTERIOR', '建筑外观示例', 'detailed architecture', 10, 0),
  (2019003, 2019, '/images/tags/gallery/architecture/interior.webp', 'EXAMPLE', 'INTERIOR', '建筑室内示例', 'detailed architecture', 20, 0),
  (2020002, 2020, '/images/tags/gallery/animal/wildlife.webp', 'EXAMPLE', 'WILDLIFE', '野生动物示例', 'detailed animal subject', 10, 0),
  (2020003, 2020, '/images/tags/gallery/animal/domestic.webp', 'EXAMPLE', 'DOMESTIC', '生活动物示例', 'detailed animal subject', 20, 0),
  (2021002, 2021, '/images/tags/gallery/product/studio.webp', 'EXAMPLE', 'STUDIO', '产品棚拍示例', 'professional product photography', 10, 0),
  (2021003, 2021, '/images/tags/gallery/product/lifestyle.webp', 'EXAMPLE', 'LIFESTYLE', '产品生活场景', 'professional product photography', 20, 0),
  (2022002, 2022, '/images/tags/gallery/food/plated.webp', 'EXAMPLE', 'FOOD', '精致摆盘示例', 'appetizing food photography', 10, 0),
  (2022003, 2022, '/images/tags/gallery/food/market.webp', 'EXAMPLE', 'LIFESTYLE', '生活美食示例', 'appetizing food photography', 20, 0),
  (2023002, 2023, '/images/tags/gallery/vehicle/road.webp', 'EXAMPLE', 'ROAD', '道路载具示例', 'detailed vehicle design', 10, 0),
  (2023003, 2023, '/images/tags/gallery/vehicle/studio.webp', 'EXAMPLE', 'STUDIO', '载具设计棚拍', 'detailed vehicle design', 20, 0),
  (2010002, 2010, '/images/tags/gallery/high-detail/close-up.webp', 'COMPARISON', 'CLOSE_UP', '局部高细节示例', 'highly detailed, sharp focus', 10, 0),
  (2010003, 2010, '/images/tags/gallery/high-detail/architecture.webp', 'COMPARISON', 'ARCHITECTURE', '复杂场景高细节', 'highly detailed, sharp focus', 20, 0),
  (2033002, 2033, '/images/tags/gallery/clean-lines/character.webp', 'COMPARISON', 'CHARACTER', '角色线稿示例', 'clean confident linework', 10, 0),
  (2033003, 2033, '/images/tags/gallery/clean-lines/object.webp', 'COMPARISON', 'OBJECT', '产品线稿示例', 'clean confident linework', 20, 0),
  (2034002, 2034, '/images/tags/gallery/professional-color-grading/character.webp', 'COMPARISON', 'CHARACTER', '人物调色示例', 'professional color grading', 10, 0),
  (2034003, 2034, '/images/tags/gallery/professional-color-grading/landscape.webp', 'COMPARISON', 'LANDSCAPE', '风景调色示例', 'professional color grading', 20, 0),
  (2038002, 2038, '/images/tags/gallery/dreamy/character.webp', 'COMPARISON', 'CHARACTER', '梦幻人物示例', 'dreamy ethereal atmosphere', 10, 0),
  (2038003, 2038, '/images/tags/gallery/dreamy/landscape.webp', 'COMPARISON', 'LANDSCAPE', '梦幻场景示例', 'dreamy ethereal atmosphere', 20, 0),
  (2039002, 2039, '/images/tags/gallery/mysterious/character.webp', 'COMPARISON', 'CHARACTER', '神秘人物示例', 'mysterious atmosphere', 10, 0),
  (2039003, 2039, '/images/tags/gallery/mysterious/architecture.webp', 'COMPARISON', 'ARCHITECTURE', '神秘建筑示例', 'mysterious atmosphere', 20, 0),
  (2040002, 2040, '/images/tags/gallery/epic/character.webp', 'COMPARISON', 'CHARACTER', '史诗人物示例', 'epic atmosphere', 10, 0),
  (2040003, 2040, '/images/tags/gallery/epic/landscape.webp', 'COMPARISON', 'LANDSCAPE', '史诗场景示例', 'epic atmosphere', 20, 0);
