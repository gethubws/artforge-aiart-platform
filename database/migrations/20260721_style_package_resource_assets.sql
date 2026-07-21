DROP PROCEDURE IF EXISTS aiart_add_column_if_missing;

DELIMITER //
CREATE PROCEDURE aiart_add_column_if_missing(
  IN table_name_value VARCHAR(64),
  IN column_name_value VARCHAR(64),
  IN column_definition_value TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = table_name_value
      AND column_name = column_name_value
  ) THEN
    SET @ddl = CONCAT(
      'ALTER TABLE `', REPLACE(table_name_value, '`', '``'),
      '` ADD COLUMN `', REPLACE(column_name_value, '`', '``'),
      '` ', column_definition_value
    );
    PREPARE statement_to_run FROM @ddl;
    EXECUTE statement_to_run;
    DEALLOCATE PREPARE statement_to_run;
  END IF;
END//
DELIMITER ;

CALL aiart_add_column_if_missing('style_package', 'license_type',
  'VARCHAR(64) NOT NULL DEFAULT ''STANDARD'' AFTER `negative_prompt_guide`');
CALL aiart_add_column_if_missing('style_package', 'license_summary',
  'VARCHAR(500) NULL AFTER `license_type`');
CALL aiart_add_column_if_missing('style_package', 'commercial_use',
  'TINYINT(1) NOT NULL DEFAULT 1 AFTER `license_summary`');
CALL aiart_add_column_if_missing('style_package', 'resource_count',
  'INT NOT NULL DEFAULT 0 AFTER `artwork_count`');
CALL aiart_add_column_if_missing('style_package', 'category_count',
  'INT NOT NULL DEFAULT 0 AFTER `resource_count`');

CALL aiart_add_column_if_missing('style_package_version', 'license_type',
  'VARCHAR(64) NOT NULL DEFAULT ''STANDARD'' AFTER `negative_prompt_guide`');
CALL aiart_add_column_if_missing('style_package_version', 'license_summary',
  'VARCHAR(500) NULL AFTER `license_type`');
CALL aiart_add_column_if_missing('style_package_version', 'commercial_use',
  'TINYINT(1) NOT NULL DEFAULT 1 AFTER `license_summary`');
CALL aiart_add_column_if_missing('style_package_version', 'resource_count',
  'INT NOT NULL DEFAULT 0 AFTER `artwork_count`');
CALL aiart_add_column_if_missing('style_package_version', 'category_count',
  'INT NOT NULL DEFAULT 0 AFTER `resource_count`');

DROP PROCEDURE aiart_add_column_if_missing;

CREATE TABLE IF NOT EXISTS style_package_asset (
  id BIGINT PRIMARY KEY,
  style_package_id BIGINT NOT NULL,
  contributor_id BIGINT NOT NULL,
  logical_key VARCHAR(80) NOT NULL,
  revision_number INT NOT NULL DEFAULT 1,
  name VARCHAR(120) NOT NULL,
  category_key VARCHAR(64) NOT NULL,
  asset_type VARCHAR(40) NOT NULL DEFAULT 'IMAGE',
  description TEXT,
  preview_image_url VARCHAR(512) NOT NULL,
  file_url VARCHAR(512),
  thumbnail_url VARCHAR(512),
  prompt_text TEXT,
  negative_prompt_text TEXT,
  generation_params_json JSON,
  width INT,
  height INT,
  file_format VARCHAR(20),
  background_mode VARCHAR(32) NOT NULL DEFAULT 'SOLID',
  license_scope VARCHAR(64) NOT NULL DEFAULT 'PACKAGE',
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_style_asset_revision (style_package_id, logical_key, revision_number),
  INDEX idx_style_asset_package_status (style_package_id, status, sort_order),
  INDEX idx_style_asset_category (style_package_id, category_key, status),
  INDEX idx_style_asset_contributor (contributor_id, created_at),
  CONSTRAINT fk_style_asset_package FOREIGN KEY (style_package_id) REFERENCES style_package(id),
  CONSTRAINT fk_style_asset_contributor FOREIGN KEY (contributor_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS style_package_version_asset (
  id BIGINT PRIMARY KEY,
  style_package_version_id BIGINT NOT NULL,
  style_package_asset_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_style_version_asset (style_package_version_id, style_package_asset_id),
  INDEX idx_style_version_asset_asset (style_package_asset_id),
  CONSTRAINT fk_style_version_asset_version FOREIGN KEY (style_package_version_id) REFERENCES style_package_version(id),
  CONSTRAINT fk_style_version_asset_asset FOREIGN KEY (style_package_asset_id) REFERENCES style_package_asset(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
