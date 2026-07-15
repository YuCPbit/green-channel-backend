-- 创建数据库
CREATE DATABASE IF NOT EXISTS green_channel DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE green_channel;

-- ========================================================
-- 分组：用户与权限组
-- ========================================================

-- 1. 系统用户表
CREATE TABLE `gc_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名/学号/工号',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希值',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `user_type` TINYINT NOT NULL COMMENT '用户类型: 1-学生 2-辅导员 3-学院管理员 4-学校管理员 5-系统管理员',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 2. 角色表
CREATE TABLE `gc_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 权限表
CREATE TABLE `gc_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
  `permission_code` VARCHAR(50) NOT NULL COMMENT '权限编码',
  `type` TINYINT NOT NULL COMMENT '类型: 1-菜单 2-按钮 3-数据',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父级ID',
  `path` VARCHAR(255) DEFAULT NULL COMMENT '菜单路由或API路径',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 4. 用户角色关联表
CREATE TABLE `gc_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 5. 角色权限关联表
CREATE TABLE `gc_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 6. 操作日志表
CREATE TABLE `gc_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '操作人用户ID',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型(如:新增,修改,审核)',
  `module` VARCHAR(50) NOT NULL COMMENT '操作模块(如:绿色通道审核)',
  `target_id` VARCHAR(100) DEFAULT NULL COMMENT '业务目标主键',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法(GET,POST等)',
  `request_url` VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
  `request_params` TEXT COMMENT '请求参数',
  `response_result` TEXT COMMENT '响应结果',
  `success` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否成功: 0-失败 1-成功',
  `operation_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 6.1 消息模板表
CREATE TABLE `gc_message_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `event_code` VARCHAR(80) NOT NULL COMMENT '消息事件编码',
  `title_template` VARCHAR(200) NOT NULL COMMENT '标题模板',
  `content_template` TEXT NOT NULL COMMENT '正文模板',
  `message_type` VARCHAR(50) NOT NULL DEFAULT 'BUSINESS' COMMENT '消息类型',
  `channels` VARCHAR(100) NOT NULL DEFAULT 'IN_APP' COMMENT '发送渠道，逗号分隔',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_template_event` (`event_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板表';

-- 6.2 站内消息表
CREATE TABLE `gc_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `receiver_user_id` BIGINT NOT NULL COMMENT '接收人用户ID',
  `event_code` VARCHAR(80) NOT NULL COMMENT '消息事件编码',
  `business_id` VARCHAR(100) DEFAULT NULL COMMENT '关联业务主键',
  `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
  `content` TEXT NOT NULL COMMENT '消息正文',
  `message_type` VARCHAR(50) NOT NULL DEFAULT 'BUSINESS' COMMENT '消息类型',
  `read_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `read_time` DATETIME DEFAULT NULL COMMENT '已读时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_message_receiver_read` (`receiver_user_id`, `read_status`, `create_time`),
  KEY `idx_message_business` (`event_code`, `business_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- 6.3 消息渠道投递记录表
CREATE TABLE `gc_message_delivery` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `message_id` BIGINT NOT NULL COMMENT '站内消息ID',
  `channel` VARCHAR(30) NOT NULL COMMENT 'IN_APP/SMS/WECHAT/EMAIL',
  `delivery_status` VARCHAR(20) NOT NULL COMMENT 'SUCCESS/RETRY/FAILED',
  `attempt_count` INT NOT NULL DEFAULT 0 COMMENT '尝试次数',
  `failure_type` VARCHAR(100) DEFAULT NULL COMMENT '失败类型，不记录异常原文',
  `next_retry_time` DATETIME DEFAULT NULL COMMENT '下次重试时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_delivery_retry` (`delivery_status`, `next_retry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息渠道投递记录表';

-- 6.4 消息事件失败重试表
CREATE TABLE `gc_message_event_retry` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `event_code` VARCHAR(80) NOT NULL COMMENT '消息事件编码',
  `receiver_user_id` BIGINT NOT NULL COMMENT '接收人用户ID',
  `business_id` VARCHAR(100) DEFAULT NULL COMMENT '关联业务主键',
  `variables_json` JSON DEFAULT NULL COMMENT '模板变量，仅存业务字段',
  `failure_type` VARCHAR(100) DEFAULT NULL COMMENT '失败类型，不记录异常原文',
  `retry_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/DEAD',
  `attempt_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
  `next_retry_time` DATETIME DEFAULT NULL COMMENT '下次重试时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_event_retry` (`retry_status`, `next_retry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息事件失败重试表';


-- ========================================================
-- 分组：组织架构组
-- ========================================================

-- 7. 学校信息表
CREATE TABLE `gc_school` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `school_name` VARCHAR(100) NOT NULL COMMENT '学校名称',
  `school_code` VARCHAR(50) NOT NULL COMMENT '学校代码',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
  `contact_phone` VARCHAR(50) DEFAULT NULL COMMENT '联系电话',
  `introduction` TEXT COMMENT '简介',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学校信息表';

-- 8. 学院表
CREATE TABLE `gc_college` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `school_id` BIGINT NOT NULL COMMENT '学校ID',
  `college_name` VARCHAR(100) NOT NULL COMMENT '学院名称',
  `college_code` VARCHAR(50) NOT NULL COMMENT '学院代码',
  `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(50) DEFAULT NULL COMMENT '联系电话',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_school_id` (`school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院表';

-- 9. 专业表
CREATE TABLE `gc_major` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `college_id` BIGINT NOT NULL COMMENT '学院ID',
  `major_name` VARCHAR(100) NOT NULL COMMENT '专业名称',
  `major_code` VARCHAR(50) NOT NULL COMMENT '专业代码',
  `school_length` INT DEFAULT 4 COMMENT '学制(年)',
  `degree_type` VARCHAR(50) DEFAULT NULL COMMENT '学位类型(如:工学学士)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_college_id` (`college_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业表';

-- 10. 班级表
CREATE TABLE `gc_class` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `major_id` BIGINT NOT NULL COMMENT '专业ID',
  `grade` INT NOT NULL COMMENT '年级(如:2023)',
  `class_name` VARCHAR(100) NOT NULL COMMENT '班级名称',
  `class_code` VARCHAR(50) NOT NULL COMMENT '班级代码',
  `tutor_id` BIGINT DEFAULT NULL COMMENT '辅导员用户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_major_grade` (`major_id`, `grade`),
  KEY `idx_tutor_id` (`tutor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- ========================================================
-- 分组：学生信息组
-- ========================================================

-- 11. 学生基本信息表
CREATE TABLE `gc_student` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '关联的用户ID',
  `student_no` VARCHAR(50) NOT NULL COMMENT '学号',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `gender` TINYINT DEFAULT NULL COMMENT '性别: 1-男 2-女',
  `id_card` VARCHAR(255) NOT NULL COMMENT '身份证号(加密存储)',
  `birth_date` DATE DEFAULT NULL COMMENT '出生日期',
  `nation` VARCHAR(20) DEFAULT NULL COMMENT '民族',
  `political_status` VARCHAR(20) DEFAULT NULL COMMENT '政治面貌',
  `phone` VARCHAR(50) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `home_address` VARCHAR(255) DEFAULT NULL COMMENT '家庭地址',
  `enroll_year` INT NOT NULL COMMENT '入学年份',
  `college_id` BIGINT NOT NULL COMMENT '学院ID',
  `major_id` BIGINT NOT NULL COMMENT '专业ID',
  `class_id` BIGINT DEFAULT NULL COMMENT '班级ID',
  `student_type` VARCHAR(20) DEFAULT '本科' COMMENT '学生类型[本科/研究生]',
  `poverty_level` TINYINT DEFAULT NULL COMMENT '困难等级: 1-特别困难 2-困难 3-一般困难 4-不困难',
  `family_economic_status` VARCHAR(50) DEFAULT NULL COMMENT '家庭经济情况说明',
  `family_population` INT DEFAULT NULL COMMENT '家庭人口数',
  `family_annual_income` DECIMAL(12,2) DEFAULT NULL COMMENT '家庭年收入',
  `is_registered_poor` TINYINT(1) DEFAULT 0 COMMENT '是否建档立卡: 0-否 1-是',
  `is_low_income` TINYINT(1) DEFAULT 0 COMMENT '是否低保: 0-否 1-是',
  `is_extreme_poor` TINYINT(1) DEFAULT 0 COMMENT '是否特困供养: 0-否 1-是',
  `is_orphan_disabled` TINYINT(1) DEFAULT 0 COMMENT '是否孤残: 0-否 1-是',
  `is_martyr_family` TINYINT(1) DEFAULT 0 COMMENT '是否烈属: 0-否 1-是',
  `disaster_status` VARCHAR(255) DEFAULT NULL COMMENT '家庭受灾情况',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `info_completed` TINYINT(1) DEFAULT 0 COMMENT '信息完善状态: 0-未完善 1-已完善',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_no` (`student_no`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_college_class` (`college_id`, `class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生基本信息表';

-- 12. 学生家庭成员表
CREATE TABLE `gc_student_family` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` BIGINT NOT NULL COMMENT '学生ID',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `relation` VARCHAR(20) NOT NULL COMMENT '与学生关系',
  `age` INT DEFAULT NULL COMMENT '年龄',
  `work_unit` VARCHAR(100) DEFAULT NULL COMMENT '工作单位',
  `profession` VARCHAR(50) DEFAULT NULL COMMENT '职业',
  `annual_income` DECIMAL(10,2) DEFAULT NULL COMMENT '年收入',
  `health_status` VARCHAR(50) DEFAULT NULL COMMENT '健康状况',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生家庭成员表';

-- ========================================================
-- 分组：绿色通道配置组
-- ========================================================

-- 14. 绿色通道批次表
CREATE TABLE `gc_green_channel_batch` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_name` VARCHAR(100) NOT NULL COMMENT '批次名称',
  `academic_year` VARCHAR(20) NOT NULL COMMENT '学年(如:2023-2024)',
  `semester` TINYINT NOT NULL COMMENT '学期: 1-第一学期 2-第二学期',
  `apply_start_time` DATETIME NOT NULL COMMENT '申请开始时间',
  `apply_end_time` DATETIME NOT NULL COMMENT '申请结束时间',
  `college_submit_end_time` DATETIME NOT NULL COMMENT '学院提交截止时间',
  `fund_source` VARCHAR(50) DEFAULT NULL COMMENT '资金来源(中央财政/学校事业经费/社会捐赠)',
  `allow_grades` JSON DEFAULT NULL COMMENT '可申请年级范围JSON',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-未开始 1-进行中 2-已结束',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绿色通道批次表';

-- 16. 爱心大礼包批次表
CREATE TABLE `gc_gift_pack_batch` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `gc_batch_id` BIGINT NOT NULL COMMENT '关联的绿色通道批次ID',
  `batch_name` VARCHAR(100) NOT NULL COMMENT '大礼包批次名称',
  `max_items` INT DEFAULT 5 COMMENT '学生可选物品个数上限',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_gc_batch_id` (`gc_batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='爱心大礼包批次表';

-- 17. 爱心大礼包物品表
CREATE TABLE `gc_gift_pack_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pack_batch_id` BIGINT NOT NULL COMMENT '大礼包批次ID',
  `item_name` VARCHAR(100) NOT NULL COMMENT '物品名称',
  `image_url` VARCHAR(255) DEFAULT NULL COMMENT '图片URL',
  `item_type` VARCHAR(50) DEFAULT NULL COMMENT '类型',
  `size_options` JSON DEFAULT NULL COMMENT '尺寸选项JSON(如:["S","M","L","XL"])',
  `introduction` VARCHAR(255) DEFAULT NULL COMMENT '简介',
  `unit_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '单价',
  `gender_limit` TINYINT DEFAULT 0 COMMENT '适用性别: 0-通用 1-男 2-女',
  `is_required` TINYINT(1) DEFAULT 0 COMMENT '是否必选/赠送类: 0-否 1-是',
  `inventory` INT DEFAULT 0 COMMENT '库存数量',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_pack_batch_id` (`pack_batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='爱心大礼包物品表';

-- 18. 爱心大礼包名额分配表
CREATE TABLE `gc_gift_pack_quota` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pack_batch_id` BIGINT NOT NULL COMMENT '大礼包批次ID',
  `college_id` BIGINT NOT NULL COMMENT '学院ID',
  `grade` INT DEFAULT NULL COMMENT '年级(若为空表示学院总名额，不为空表示该学院下某年级名额)',
  `allocated_quota` INT NOT NULL DEFAULT 0 COMMENT '分配名额',
  `used_quota` INT NOT NULL DEFAULT 0 COMMENT '已用名额(已通过终审的数量)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pack_college_grade` (`pack_batch_id`, `college_id`, `grade`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='爱心大礼包名额分配表';


-- ========================================================
-- 分组：绿色通道申请组
-- ========================================================

-- 21. 爱心大礼包申请表
CREATE TABLE `gc_gift_pack_apply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pack_batch_id` BIGINT NOT NULL COMMENT '大礼包批次ID',
  `student_id` BIGINT NOT NULL COMMENT '学生ID',
  `apply_no` VARCHAR(50) NOT NULL COMMENT '申请编号',
  `apply_reason` VARCHAR(500) DEFAULT NULL COMMENT '申请理由',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '申请状态(同申请状态字典)',
  `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `pickup_code` VARCHAR(50) DEFAULT NULL COMMENT '终审通过后生成的礼包领取码',
  `pickup_status` TINYINT NOT NULL DEFAULT 0 COMMENT '领取状态: 0-待领取 1-已领取 2-异常待处理 3-已补发',
  `pickup_time` DATETIME DEFAULT NULL COMMENT '实际领取时间',
  `pickup_operator_id` BIGINT DEFAULT NULL COMMENT '核销工作人员用户ID',
  `pickup_remark` VARCHAR(500) DEFAULT NULL COMMENT '尺码更换、缺货补发等异常说明',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pack_apply_no` (`apply_no`),
  UNIQUE KEY `uk_pickup_code` (`pickup_code`),
  UNIQUE KEY `uk_batch_student` (`pack_batch_id`, `student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='爱心大礼包申请表';

-- 22. 爱心大礼包申请物品明细表
CREATE TABLE `gc_gift_pack_apply_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `apply_id` BIGINT NOT NULL COMMENT '大礼包申请ID',
  `item_id` BIGINT NOT NULL COMMENT '物品ID',
  `selected_size` VARCHAR(50) DEFAULT NULL COMMENT '选择的尺寸',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_apply_id` (`apply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='爱心大礼包申请物品明细表';

-- 23. 申请附件表
CREATE TABLE `gc_apply_attachment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `apply_id` BIGINT NOT NULL COMMENT '申请ID(根据类型关联)',
  `apply_type` TINYINT NOT NULL COMMENT '申请类型: 1-大礼包 2-补助 3-申诉 4-辅导员事务',
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径/URL',
  `file_size` BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
  `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型(如:jpg,png,pdf)',
  `ocr_result` TEXT COMMENT 'OCR识别结果JSON',
  `upload_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_apply_type_id` (`apply_type`, `apply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申请附件表';

-- 23.1 公共附件表（V1 公共接口使用；支持草稿阶段先上传、后绑定）
CREATE TABLE `gc_attachment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `owner_id` BIGINT NOT NULL COMMENT '上传人用户ID',
  `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `stored_name` VARCHAR(255) NOT NULL COMMENT '服务端随机文件名',
  `content_type` VARCHAR(150) NOT NULL COMMENT '服务端校验后的MIME类型',
  `file_size` BIGINT NOT NULL COMMENT '文件大小(字节)',
  `storage_path` VARCHAR(500) NOT NULL COMMENT '服务端存储路径，不对外返回',
  `business_type` VARCHAR(50) DEFAULT NULL COMMENT '业务类型编码',
  `business_id` BIGINT DEFAULT NULL COMMENT '绑定的业务主键',
  `status` VARCHAR(20) NOT NULL DEFAULT 'READY' COMMENT 'READY/DELETED',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stored_name` (`stored_name`),
  KEY `idx_attachment_owner` (`owner_id`, `create_time`),
  KEY `idx_attachment_business` (`business_type`, `business_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公共附件表';


-- ========================================================
-- 分组：审核流程组
-- ========================================================

-- 24. 审核记录表
CREATE TABLE `gc_review_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `apply_id` BIGINT NOT NULL COMMENT '关联的申请ID',
  `apply_type` TINYINT NOT NULL COMMENT '申请类型: 1-大礼包 2-补助',
  `reviewer_id` BIGINT NOT NULL COMMENT '审核人用户ID',
  `reviewer_role` TINYINT NOT NULL COMMENT '审核人角色层级: 1-辅导员 2-学院 3-学校',
  `action` TINYINT NOT NULL COMMENT '审核动作: 1-通过 2-驳回修改 3-不通过 4-修改 5-取消',
  `comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
  `modified_content` JSON DEFAULT NULL COMMENT '若为修改动作，记录修改前后的内容JSON比对快照',
  `review_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  `review_seq` INT DEFAULT 1 COMMENT '审核顺序号',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_apply_type_id` (`apply_type`, `apply_id`),
  KEY `idx_reviewer_id` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录表';

-- 25. 审核意见模板表
CREATE TABLE `gc_review_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `template_content` VARCHAR(500) NOT NULL COMMENT '模板内容',
  `applicable_roles` VARCHAR(50) DEFAULT NULL COMMENT '适用角色(如:1,2,3)',
  `use_count` INT DEFAULT 0 COMMENT '使用次数',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核意见模板表';

-- 26. 审核超时配置表
CREATE TABLE `gc_review_timeout_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_level` TINYINT NOT NULL COMMENT '适用角色层级: 1-辅导员 2-学院 3-学校',
  `timeout_days` INT NOT NULL DEFAULT 3 COMMENT '超时天数',
  `remind_method` VARCHAR(50) DEFAULT 'SYS_MSG' COMMENT '提醒方式: SYS_MSG, SMS, EMAIL',
  `is_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核超时配置表';

-- ========================================================
-- 分组：困难补助组
-- ========================================================

-- 28. 补助批次表
CREATE TABLE `gc_subsidy_batch` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_name` VARCHAR(100) NOT NULL COMMENT '批次名称',
  `academic_year` VARCHAR(20) NOT NULL COMMENT '学年',
  `subsidy_type` TINYINT NOT NULL COMMENT '补助类型: 1-生活补助 2-路费补助 3-临时困难补助',
  `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '学校总金额盘',
  `apply_start_time` DATETIME NOT NULL COMMENT '申请开始时间',
  `apply_end_time` DATETIME NOT NULL COMMENT '申请结束时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-未开始 1-进行中 2-已结束',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补助批次表';

-- 29. 补助金额分配表
CREATE TABLE `gc_subsidy_allocation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_id` BIGINT NOT NULL COMMENT '补助批次ID',
  `college_id` BIGINT NOT NULL COMMENT '学院ID',
  `grade` INT DEFAULT NULL COMMENT '年级(为空则为学院总额)',
  `allocated_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '分配金额',
  `used_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '已用金额',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_college_grade` (`batch_id`, `college_id`, `grade`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补助金额分配表';

-- 30. 补助申请表
CREATE TABLE `gc_subsidy_apply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_id` BIGINT NOT NULL COMMENT '补助批次ID',
  `student_id` BIGINT NOT NULL COMMENT '学生ID',
  `applicant_type` TINYINT NOT NULL DEFAULT 1 COMMENT '发起方: 1-学生自主申请 2-辅导员主动申请',
  `applicant_user_id` BIGINT NOT NULL COMMENT '实际发起人用户ID',
  `apply_no` VARCHAR(50) NOT NULL COMMENT '申请编号',
  `subsidy_type` TINYINT NOT NULL COMMENT '补助类型(同批次表)',
  `apply_amount` DECIMAL(10,2) NOT NULL COMMENT '期望申请金额',
  `approved_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '最终审批金额',
  `apply_reason` VARCHAR(500) DEFAULT NULL COMMENT '申请理由',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '申请状态: 1-待辅导员审 2-待学院审 3-待学校审 4-已通过 5-已驳回 6-不通过；辅导员发起时直接从2开始',
  `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  UNIQUE KEY `uk_batch_student` (`batch_id`, `student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补助申请表';

-- 31. 补助审核记录表
CREATE TABLE `gc_subsidy_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `apply_id` BIGINT NOT NULL COMMENT '补助申请ID',
  `reviewer_id` BIGINT NOT NULL COMMENT '审核人ID',
  `reviewer_role` TINYINT NOT NULL COMMENT '审核人角色(1/2/3)',
  `action` TINYINT NOT NULL COMMENT '审核动作',
  `comment` VARCHAR(500) DEFAULT NULL COMMENT '意见',
  `suggest_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '本级填写的建议发放金额',
  `review_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_apply_id` (`apply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补助审核记录表';

-- ========================================================
-- 分组：系统管理及配置组
-- ========================================================

-- 44. 系统参数配置表
CREATE TABLE `gc_system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_name` VARCHAR(100) NOT NULL COMMENT '参数名',
  `config_key` VARCHAR(100) NOT NULL COMMENT '参数键(如: APPEAL_WINDOW_DAYS)',
  `config_value` TEXT NOT NULL COMMENT '参数值',
  `config_type` VARCHAR(20) DEFAULT 'TEXT' COMMENT '类型: TEXT, NUMBER, BOOLEAN, JSON',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `is_editable` TINYINT(1) DEFAULT 1 COMMENT '是否允许前端修改: 0-否 1-是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数配置表';

-- 45. 数据字典表
CREATE TABLE `gc_dictionary` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dict_type_code` VARCHAR(50) NOT NULL COMMENT '字典类型编码(如: GENDER)',
  `dict_type_name` VARCHAR(100) NOT NULL COMMENT '字典类型名称(如: 性别)',
  `item_code` VARCHAR(50) NOT NULL COMMENT '字典项编码(如: 1)',
  `item_name` VARCHAR(100) NOT NULL COMMENT '字典项名称(如: 男)',
  `item_value` VARCHAR(100) DEFAULT NULL COMMENT '字典项值(扩展用)',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_item_code` (`dict_type_code`, `item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';


-- ========================================================
-- 初始化数据插入
-- ========================================================

-- 插入默认角色
INSERT INTO `gc_role` (`role_name`, `role_code`, `description`, `sort`) VALUES
('系统管理员', 'ADMIN', '负责系统参数与权限管理', 1),
('学校管理员', 'SCHOOL_ADMIN', '资助中心，负责全局管理与最终审批', 2),
('学院管理员', 'COLLEGE_ADMIN', '负责学院额度分配与二级审批', 3),
('辅导员', 'TUTOR', '负责班级学生管理与初审', 4),
('学生', 'STUDENT', '普通学生，申请资助服务', 5);

-- 插入默认管理员账号 (密码为 admin123 假设哈希值为 xyz)
INSERT INTO `gc_user` (`username`, `password_hash`, `real_name`, `user_type`, `status`) VALUES
('admin', '$2a$10$xyz', '超级管理员', 5, 1);

-- 插入默认字典值
INSERT INTO `gc_dictionary` (`dict_type_code`, `dict_type_name`, `item_code`, `item_name`, `sort`) VALUES
('GENDER', '性别', '1', '男', 1),
('GENDER', '性别', '2', '女', 2),
('APPLY_STATUS', '申请状态', '1', '草稿', 1),
('APPLY_STATUS', '申请状态', '2', '待辅导员审核', 2),
('APPLY_STATUS', '申请状态', '3', '待学院审核', 3),
('APPLY_STATUS', '申请状态', '4', '待学校审核', 4),
('APPLY_STATUS', '申请状态', '5', '已通过', 5),
('APPLY_STATUS', '申请状态', '6', '已驳回', 6),
('APPLY_STATUS', '申请状态', '7', '不通过', 7),
('POVERTY_LEVEL', '贫困等级', '1', '特别困难', 1),
('POVERTY_LEVEL', '贫困等级', '2', '困难', 2),
('POVERTY_LEVEL', '贫困等级', '3', '一般困难', 3),
('POVERTY_LEVEL', '贫困等级', '4', '不困难', 4);

-- 插入系统参数
INSERT INTO `gc_system_config` (`config_name`, `config_key`, `config_value`, `config_type`, `description`) VALUES
('申诉窗口期天数', 'APPEAL_WINDOW_DAYS', '3', 'NUMBER', '审核不通过后，允许学生发起申诉的天数');


-- ========================================================
-- 分组：辅导员事务申请组
-- ========================================================

-- 48. 辅导员申请类型配置表
CREATE TABLE `gc_tutor_apply_type` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type_name` VARCHAR(100) NOT NULL COMMENT '申请类型名称(如:班级资助专项工作经费)',
  `type_code` VARCHAR(50) NOT NULL COMMENT '申请类型编码',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '类型说明',
  `need_amount` TINYINT(1) DEFAULT 0 COMMENT '是否需要金额字段: 0-否 1-是',
  `need_student` TINYINT(1) DEFAULT 1 COMMENT '是否需要关联学生: 0-否 1-是',
  `approval_level` TINYINT DEFAULT 2 COMMENT '审批级数: 1-仅学院 2-学院+学校',
  `form_template` JSON DEFAULT NULL COMMENT '表单字段模板JSON配置',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_code` (`type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='辅导员申请类型配置表';

-- 49. 辅导员申请主表
CREATE TABLE `gc_tutor_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `apply_no` VARCHAR(50) NOT NULL COMMENT '申请编号',
  `type_id` BIGINT NOT NULL COMMENT '申请类型ID',
  `tutor_id` BIGINT NOT NULL COMMENT '辅导员用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '申请标题',
  `description` TEXT NOT NULL COMMENT '申请事由详细说明',
  `amount` DECIMAL(10,2) DEFAULT NULL COMMENT '申请金额(如适用)',
  `urgency` TINYINT DEFAULT 1 COMMENT '紧急程度: 1-普通 2-紧急 3-特急',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1-草稿 2-待学院审批 3-待学校审批 4-已通过 5-已驳回',
  `form_data` JSON DEFAULT NULL COMMENT '动态表单数据JSON',
  `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `submit_time` DATETIME DEFAULT NULL COMMENT '正式提交时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_tutor_id` (`tutor_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='辅导员申请主表';

-- 50. 辅导员申请关联学生表
CREATE TABLE `gc_tutor_app_student` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `application_id` BIGINT NOT NULL COMMENT '辅导员申请ID',
  `student_id` BIGINT NOT NULL COMMENT '关联学生ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_student` (`application_id`, `student_id`),
  KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='辅导员申请关联学生表';

-- 51. 辅导员申请审核记录表
CREATE TABLE `gc_tutor_app_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `application_id` BIGINT NOT NULL COMMENT '辅导员申请ID',
  `reviewer_id` BIGINT NOT NULL COMMENT '审核人ID',
  `reviewer_role` TINYINT NOT NULL COMMENT '审核角色层级: 2-学院 3-学校',
  `action` TINYINT NOT NULL COMMENT '审核动作: 1-通过 2-驳回 3-转交 4-备案',
  `comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
  `review_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_application_id` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='辅导员申请审核记录表';


-- ========================================================
-- 分组：勤工助学组
-- ========================================================

-- 52. 勤工助学批次表
CREATE TABLE `gc_work_study_batch` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_name` VARCHAR(100) NOT NULL COMMENT '批次名称',
  `academic_year` VARCHAR(20) NOT NULL COMMENT '学年',
  `semester` TINYINT NOT NULL COMMENT '学期: 1-第一学期 2-第二学期',
  `register_start_time` DATETIME NOT NULL COMMENT '报名开始时间',
  `register_end_time` DATETIME NOT NULL COMMENT '报名截止时间',
  `interview_start_time` DATETIME DEFAULT NULL COMMENT '面试开始时间',
  `interview_end_time` DATETIME DEFAULT NULL COMMENT '面试结束时间',
  `work_start_date` DATE NOT NULL COMMENT '上岗开始日期',
  `work_end_date` DATE NOT NULL COMMENT '上岗结束日期',
  `max_positions` INT DEFAULT 0 COMMENT '全校岗位总数上限',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0-未开始 1-报名中 2-面试中 3-进行中 4-已结束',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勤工助学批次表';

-- 53. 勤工助学岗位表
CREATE TABLE `gc_work_study_position` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_id` BIGINT NOT NULL COMMENT '批次ID',
  `position_name` VARCHAR(100) NOT NULL COMMENT '岗位名称',
  `department_name` VARCHAR(100) NOT NULL COMMENT '用工部门名称',
  `department_id` BIGINT DEFAULT NULL COMMENT '用工部门ID(学院ID或自定义)',
  `description` TEXT DEFAULT NULL COMMENT '岗位描述',
  `work_location` VARCHAR(200) DEFAULT NULL COMMENT '工作地点',
  `work_time_desc` VARCHAR(200) DEFAULT NULL COMMENT '工作时间描述(如:周一至五 14:00-18:00)',
  `max_weekly_hours` INT DEFAULT 8 COMMENT '每周最大工时(小时)',
  `position_type` TINYINT DEFAULT 1 COMMENT '岗位类型: 1-固定岗 2-临时岗',
  `recruit_count` INT NOT NULL COMMENT '招聘人数',
  `hired_count` INT DEFAULT 0 COMMENT '已录用人数',
  `salary_type` TINYINT DEFAULT 1 COMMENT '薪酬方式: 1-按小时 2-按月',
  `salary_rate` DECIMAL(8,2) NOT NULL COMMENT '薪酬标准(元/小时 或 元/月)',
  `requirements` VARCHAR(500) DEFAULT NULL COMMENT '岗位要求(专业/技能等)',
  `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` VARCHAR(50) DEFAULT NULL COMMENT '联系电话',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0-草稿 1-待审核 2-已上架 3-已下架 4-审核不通过',
  `publisher_id` BIGINT DEFAULT NULL COMMENT '发布人ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勤工助学岗位表';

-- 54. 勤工助学报名申请表
CREATE TABLE `gc_work_study_apply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `position_id` BIGINT NOT NULL COMMENT '岗位ID',
  `student_id` BIGINT NOT NULL COMMENT '学生ID',
  `apply_no` VARCHAR(50) NOT NULL COMMENT '申请编号',
  `self_intro` TEXT DEFAULT NULL COMMENT '自我介绍',
  `available_time` VARCHAR(200) DEFAULT NULL COMMENT '可工作时间段描述',
  `skills` VARCHAR(500) DEFAULT NULL COMMENT '特长/经历',
  `apply_reason` VARCHAR(500) DEFAULT NULL COMMENT '申请理由',
  `tutor_recommend` VARCHAR(500) DEFAULT NULL COMMENT '辅导员推荐意见',
  `interview_status` TINYINT DEFAULT 0 COMMENT '面试状态: 0-待面试 1-已面试 2-面试通过 3-面试不通过',
  `status` TINYINT DEFAULT 1 COMMENT '申请状态: 1-已报名 2-面试中 3-待录用审批 4-已录用 5-未录用',
  `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  UNIQUE KEY `uk_position_student` (`position_id`, `student_id`),
  KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勤工助学报名申请表';

-- 55. 勤工助学录用记录表
CREATE TABLE `gc_work_study_hire` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `apply_id` BIGINT NOT NULL COMMENT '报名申请ID',
  `position_id` BIGINT NOT NULL COMMENT '岗位ID',
  `student_id` BIGINT NOT NULL COMMENT '学生ID',
  `hire_status` TINYINT DEFAULT 1 COMMENT '录用状态: 1-在岗 2-已调岗 3-主动离岗 4-违规解聘',
  `hire_date` DATE NOT NULL COMMENT '录用日期',
  `leave_date` DATE DEFAULT NULL COMMENT '离岗日期',
  `leave_reason` VARCHAR(255) DEFAULT NULL COMMENT '离岗原因',
  `approved_by` BIGINT DEFAULT NULL COMMENT '录用审批人ID(资助中心)',
  `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_position_id` (`position_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勤工助学录用记录表';

-- 56. 勤工助学考勤记录表
CREATE TABLE `gc_work_study_attendance` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `hire_id` BIGINT NOT NULL COMMENT '录用记录ID',
  `student_id` BIGINT NOT NULL COMMENT '学生ID',
  `attendance_date` DATE NOT NULL COMMENT '考勤日期',
  `check_in_time` DATETIME DEFAULT NULL COMMENT '签到时间',
  `check_out_time` DATETIME DEFAULT NULL COMMENT '签退时间',
  `work_hours` DECIMAL(5,2) DEFAULT 0.00 COMMENT '工作时长(小时)',
  `check_type` TINYINT DEFAULT 1 COMMENT '打卡方式: 1-定位打卡 2-二维码扫码',
  `check_in_location` VARCHAR(200) DEFAULT NULL COMMENT '签到定位信息',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1-正常 2-迟到 3-早退 4-请假 5-旷工 6-补打卡待审批 7-补打卡已通过',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `confirmed_by` BIGINT DEFAULT NULL COMMENT '用工部门确认人ID',
  `confirm_time` DATETIME DEFAULT NULL COMMENT '确认时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_hire_date` (`hire_id`, `attendance_date`),
  KEY `idx_student_date` (`student_id`, `attendance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勤工助学考勤记录表';

-- 57. 勤工助学月度评价表
CREATE TABLE `gc_work_study_evaluation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `hire_id` BIGINT NOT NULL COMMENT '录用记录ID',
  `student_id` BIGINT NOT NULL COMMENT '学生ID',
  `eval_year` INT NOT NULL COMMENT '评价年份',
  `eval_month` INT NOT NULL COMMENT '评价月份(1-12)',
  `score` TINYINT NOT NULL COMMENT '评分(1-5分)',
  `comment` VARCHAR(500) DEFAULT NULL COMMENT '文字评语',
  `evaluator_id` BIGINT NOT NULL COMMENT '评价人ID(用工部门负责人)',
  `eval_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hire_month` (`hire_id`, `eval_year`, `eval_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勤工助学月度评价表';

-- 58. 勤工助学薪酬记录表
CREATE TABLE `gc_work_study_salary` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `hire_id` BIGINT NOT NULL COMMENT '录用记录ID',
  `student_id` BIGINT NOT NULL COMMENT '学生ID',
  `position_id` BIGINT NOT NULL COMMENT '岗位ID',
  `salary_year` INT NOT NULL COMMENT '薪酬年份',
  `salary_month` INT NOT NULL COMMENT '薪酬月份(1-12)',
  `total_work_hours` DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '当月总工时(小时)',
  `total_work_days` INT NOT NULL DEFAULT 0 COMMENT '当月出勤天数',
  `salary_rate` DECIMAL(8,2) NOT NULL COMMENT '薪酬标准快照(元/小时)',
  `calculated_amount` DECIMAL(10,2) NOT NULL COMMENT '系统核算金额',
  `confirmed_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '部门确认金额',
  `final_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '资助中心最终审批金额',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1-待部门确认 2-待资助中心审批 3-已审批 4-已发放',
  `dept_confirm_id` BIGINT DEFAULT NULL COMMENT '部门确认人ID',
  `dept_confirm_time` DATETIME DEFAULT NULL COMMENT '部门确认时间',
  `school_approve_id` BIGINT DEFAULT NULL COMMENT '资助中心审批人ID',
  `school_approve_time` DATETIME DEFAULT NULL COMMENT '资助中心审批时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hire_month` (`hire_id`, `salary_year`, `salary_month`),
  KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勤工助学薪酬记录表';

-- 59. 勤工助学协议表
CREATE TABLE `gc_work_study_agreement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `hire_id` BIGINT NOT NULL COMMENT '录用记录ID',
  `student_id` BIGINT NOT NULL COMMENT '学生ID',
  `position_id` BIGINT NOT NULL COMMENT '岗位ID',
  `agreement_no` VARCHAR(50) NOT NULL COMMENT '协议编号',
  `template_content` TEXT DEFAULT NULL COMMENT '协议模板内容快照',
  `start_date` DATE NOT NULL COMMENT '协议开始日期',
  `end_date` DATE NOT NULL COMMENT '协议结束日期',
  `sign_status` TINYINT DEFAULT 0 COMMENT '签署状态: 0-待签署 1-已签署 2-已到期 3-已续签',
  `student_sign_time` DATETIME DEFAULT NULL COMMENT '学生签署时间',
  `renew_count` INT DEFAULT 0 COMMENT '续签次数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否逻辑删除: 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_agreement_no` (`agreement_no`),
  KEY `idx_hire_id` (`hire_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勤工助学协议表';


-- 新增字典数据
INSERT INTO `gc_dictionary` (`dict_type_code`, `dict_type_name`, `item_code`, `item_name`, `sort`) VALUES
('WORK_STUDY_POS_STATUS', '勤工助学岗位状态', '0', '草稿', 1),
('WORK_STUDY_POS_STATUS', '勤工助学岗位状态', '1', '待审核', 2),
('WORK_STUDY_POS_STATUS', '勤工助学岗位状态', '2', '已上架', 3),
('WORK_STUDY_POS_STATUS', '勤工助学岗位状态', '3', '已下架', 4),
('TUTOR_APP_STATUS', '辅导员申请状态', '1', '草稿', 1),
('TUTOR_APP_STATUS', '辅导员申请状态', '2', '待学院审批', 2),
('TUTOR_APP_STATUS', '辅导员申请状态', '3', '待学校审批', 3),
('TUTOR_APP_STATUS', '辅导员申请状态', '4', '已通过', 4),
('TUTOR_APP_STATUS', '辅导员申请状态', '5', '已驳回', 5);

-- 新增系统参数
INSERT INTO `gc_system_config` (`config_name`, `config_key`, `config_value`, `config_type`, `description`) VALUES
('勤工助学每周最大工时', 'WS_MAX_WEEKLY_HOURS', '8', 'NUMBER', '学生每周工作时间不得超过此值(小时)'),
('勤工助学每批次最多报名岗位数', 'WS_MAX_APPLY_COUNT', '3', 'NUMBER', '学生每批次最多可报名的岗位数量'),
('勤工助学最低时薪标准', 'WS_MIN_HOURLY_RATE', '12', 'NUMBER', '薪酬标准不得低于此值(元/小时)'),
('辅导员申请单笔金额上限', 'TUTOR_APP_MAX_AMOUNT', '50000', 'NUMBER', '辅导员事务申请单笔金额上限(元)');
