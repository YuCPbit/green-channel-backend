-- A 接管功能增量迁移：资助方案、申诉、满意度问卷、岗位变动
-- 适用于已经执行过 2026-07-22 建库脚本的开发库。
USE green_channel;

CREATE TABLE IF NOT EXISTS `gc_aid_plan` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `plan_name` VARCHAR(120) NOT NULL,
  `fund_source` VARCHAR(120) NOT NULL,
  `amount_mode` VARCHAR(20) NOT NULL,
  `fixed_amount` DECIMAL(10,2) DEFAULT NULL,
  `min_amount` DECIMAL(10,2) DEFAULT NULL,
  `max_amount` DECIMAL(10,2) DEFAULT NULL,
  `quota_limit` INT NOT NULL,
  `valid_start` DATE NOT NULL,
  `valid_end` DATE NOT NULL,
  `condition_expression` TEXT,
  `status` TINYINT NOT NULL DEFAULT 0,
  `creator_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_aid_plan_status_date` (`status`, `valid_start`, `valid_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资助方案表';

CREATE TABLE IF NOT EXISTS `gc_appeal` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `appeal_no` VARCHAR(50) NOT NULL,
  `source_type` VARCHAR(20) NOT NULL,
  `source_apply_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `reason` VARCHAR(1000) NOT NULL,
  `attachment_ids` JSON DEFAULT NULL,
  `target_role` TINYINT NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `conclusion` VARCHAR(1000) DEFAULT NULL,
  `handler_id` BIGINT DEFAULT NULL,
  `submit_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `handle_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_appeal_no` (`appeal_no`),
  UNIQUE KEY `uk_appeal_source` (`source_type`, `source_apply_id`, `student_id`),
  KEY `idx_appeal_target_status` (`target_role`, `status`, `submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申请申诉表';

CREATE TABLE IF NOT EXISTS `gc_satisfaction_survey` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `target_type` VARCHAR(20) NOT NULL,
  `target_batch_id` BIGINT DEFAULT NULL,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  `creator_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_survey_status_date` (`status`, `start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资助满意度问卷';

CREATE TABLE IF NOT EXISTS `gc_satisfaction_response` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `survey_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `score` TINYINT NOT NULL,
  `suggestion` VARCHAR(1000) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_survey_student` (`survey_id`, `student_id`),
  KEY `idx_survey_score` (`survey_id`, `score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资助满意度答卷';

CREATE TABLE IF NOT EXISTS `gc_work_study_movement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `movement_no` VARCHAR(50) NOT NULL,
  `hire_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `from_position_id` BIGINT NOT NULL,
  `to_position_id` BIGINT DEFAULT NULL,
  `movement_type` VARCHAR(20) NOT NULL,
  `reason` VARCHAR(500) NOT NULL,
  `applicant_user_id` BIGINT NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1,
  `reviewer_id` BIGINT DEFAULT NULL,
  `review_comment` VARCHAR(500) DEFAULT NULL,
  `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `review_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_workstudy_movement_no` (`movement_no`),
  KEY `idx_movement_hire_status` (`hire_id`, `status`),
  KEY `idx_movement_student_time` (`student_id`, `apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勤工助学岗位变动申请表';

-- 兼容早期建库脚本：只有缺少 salary_rate 时才增加。
SET @salary_rate_exists = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'gc_work_study_hire'
     AND COLUMN_NAME = 'salary_rate'
);
SET @salary_rate_sql = IF(
  @salary_rate_exists = 0,
  'ALTER TABLE gc_work_study_hire ADD COLUMN salary_rate DECIMAL(8,2) DEFAULT NULL COMMENT ''录用时薪酬标准快照'' AFTER approve_time',
  'SELECT 1'
);
PREPARE salary_rate_statement FROM @salary_rate_sql;
EXECUTE salary_rate_statement;
DEALLOCATE PREPARE salary_rate_statement;

INSERT INTO gc_permission
  (permission_name, permission_code, type, parent_id, path, icon, sort)
VALUES
  ('资助方案管理', 'school:plan:view', 1, 0, '/aid-plans', 'solution', 122),
  ('申诉处理', 'school:appeal:view', 1, 0, '/appeals', 'audit', 124),
  ('问卷管理', 'school:survey:view', 1, 0, '/surveys', 'form', 126),
  ('我的申诉', 'student:appeal:view', 1, 0, '/my-appeals', 'audit', 32),
  ('满意度反馈', 'student:survey:view', 1, 0, '/survey-feedback', 'form', 34),
  ('申诉处理', 'tutor:appeal:view', 1, 0, '/appeals', 'audit', 62),
  ('申诉处理', 'college:appeal:view', 1, 0, '/appeals', 'audit', 82),
  ('岗位变动申请', 'workstudy:movement:apply', 1, 0, '/workstudy/movements', 'swap', 36),
  ('岗位变动审批', 'workstudy:movement:review', 1, 0, '/workstudy/movement-review', 'audit', 128)
ON DUPLICATE KEY UPDATE
  permission_name=VALUES(permission_name), type=VALUES(type), path=VALUES(path),
  icon=VALUES(icon), sort=VALUES(sort), is_deleted=0;

INSERT INTO gc_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM gc_role r
JOIN gc_permission p ON (
  (r.role_code='STUDENT' AND p.permission_code IN
    ('student:appeal:view', 'student:survey:view', 'workstudy:movement:apply'))
  OR (r.role_code='TUTOR' AND p.permission_code='tutor:appeal:view')
  OR (r.role_code='COLLEGE_ADMIN' AND p.permission_code='college:appeal:view')
  OR (r.role_code='SCHOOL_ADMIN' AND p.permission_code IN
    ('school:plan:view', 'school:appeal:view', 'school:survey:view', 'workstudy:movement:review'))
)
ON DUPLICATE KEY UPDATE is_deleted=0;
