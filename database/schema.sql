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
