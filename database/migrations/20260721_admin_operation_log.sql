CREATE TABLE IF NOT EXISTS admin_operation_log (
  id BIGINT PRIMARY KEY,
  operator_id BIGINT NOT NULL,
  action VARCHAR(64) NOT NULL,
  target_type VARCHAR(40) NOT NULL,
  target_id BIGINT,
  request_method VARCHAR(12) NOT NULL,
  request_path VARCHAR(255) NOT NULL,
  summary VARCHAR(500),
  ip_address VARCHAR(64),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_admin_operation_time (created_at),
  INDEX idx_admin_operation_operator (operator_id, created_at),
  INDEX idx_admin_operation_action (action, target_type, created_at),
  CONSTRAINT fk_admin_operation_operator FOREIGN KEY (operator_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
