USE green_channel;

-- 本文件仅用于本地开发。五类账号的初始密码均为 Dev@123456。
-- 上线或演示前必须修改密码，禁止复用开发密码。
INSERT INTO gc_user (username, password_hash, real_name, user_type, college_id, status)
VALUES
  ('student01', '$2y$10$4UhlxDrySWZYt5vv4GXqWuOgEu99q/oQL0rs5eiga38VAwl12b8UW', '测试学生', 1, NULL, 1),
  ('tutor01', '$2y$10$4UhlxDrySWZYt5vv4GXqWuOgEu99q/oQL0rs5eiga38VAwl12b8UW', '测试辅导员', 2, 1, 1),
  ('college01', '$2y$10$4UhlxDrySWZYt5vv4GXqWuOgEu99q/oQL0rs5eiga38VAwl12b8UW', '测试学院管理员', 3, 1, 1),
  ('school01', '$2y$10$4UhlxDrySWZYt5vv4GXqWuOgEu99q/oQL0rs5eiga38VAwl12b8UW', '测试资助中心', 4, NULL, 1),
  ('admin01', '$2y$10$4UhlxDrySWZYt5vv4GXqWuOgEu99q/oQL0rs5eiga38VAwl12b8UW', '测试系统管理员', 5, NULL, 1)
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  real_name = VALUES(real_name),
  user_type = VALUES(user_type),
  college_id = VALUES(college_id),
  status = VALUES(status),
  is_deleted = 0;

INSERT INTO gc_role (role_name, role_code, description, status, sort)
VALUES
  ('学生', 'STUDENT', '学生端基础角色', 1, 10),
  ('辅导员', 'TUTOR', '辅导员审核与申请角色', 1, 20),
  ('学院管理员', 'COLLEGE_ADMIN', '学院层面管理角色', 1, 30),
  ('学校资助中心', 'SCHOOL_ADMIN', '校级资助管理角色', 1, 40),
  ('系统管理员', 'SYSTEM_ADMIN', '系统配置和运维角色', 1, 50)
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name),
  description = VALUES(description),
  status = VALUES(status),
  is_deleted = 0;

INSERT INTO gc_user_role (user_id, role_id)
SELECT user.id, role.id
FROM gc_user user
JOIN gc_role role ON role.role_code = CASE user.user_type
  WHEN 1 THEN 'STUDENT'
  WHEN 2 THEN 'TUTOR'
  WHEN 3 THEN 'COLLEGE_ADMIN'
  WHEN 4 THEN 'SCHOOL_ADMIN'
  WHEN 5 THEN 'SYSTEM_ADMIN'
END
WHERE user.username IN ('student01', 'tutor01', 'college01', 'school01', 'admin01')
ON DUPLICATE KEY UPDATE is_deleted = 0;

-- 首批菜单权限。permission_code 是前后端共同使用的稳定契约，中文名称可调整。
INSERT INTO gc_permission
  (permission_name, permission_code, type, parent_id, path, icon, sort)
VALUES
  ('首页', 'home:view', 1, 0, '/', 'home', 10),
  ('绿色通道', 'student:green:view', 1, 0, '/green-channel', 'leaf', 20),
  ('困难补助', 'student:subsidy:view', 1, 0, '/subsidy', 'fund', 30),
  ('学生管理', 'tutor:student:view', 1, 0, '/students', 'team', 40),
  ('资助审核', 'tutor:review:view', 1, 0, '/aid-review', 'audit', 50),
  ('事务申请', 'tutor:application:view', 1, 0, '/tutor-application', 'form', 60),
  ('学院审核', 'college:review:view', 1, 0, '/college-review', 'audit', 70),
  ('额度管理', 'college:quota:view', 1, 0, '/quota', 'wallet', 80),
  ('学院报表', 'college:report:view', 1, 0, '/college-report', 'chart', 90),
  ('新生管理', 'school:student:view', 1, 0, '/school/students', 'team', 95),
  ('新生编辑', 'school:student:edit', 2, 0, NULL, NULL, 96),
  ('事务类型配置', 'school:tutor-type:edit', 2, 0, NULL, NULL, 97),
  ('批次配置', 'school:batch:view', 1, 0, '/batch', 'calendar', 100),
  ('学校审核', 'school:review:view', 1, 0, '/school-review', 'audit', 110),
  ('资金管理', 'school:fund:view', 1, 0, '/fund', 'wallet', 120),
  ('数据看板', 'school:dashboard:view', 1, 0, '/dashboard', 'dashboard', 130),
  ('消息中心', 'message:view', 1, 0, '/messages', 'bell', 140),
  ('用户管理', 'system:user:view', 1, 0, '/system/users', 'user', 150),
  ('角色权限', 'system:rbac:view', 1, 0, '/system/rbac', 'safety', 160),
  ('字典参数', 'system:dictionary:view', 1, 0, '/system/dictionary', 'setting', 170),
  ('字典编辑', 'system:dictionary:edit', 2, 0, NULL, NULL, 175),
  ('接口监控', 'system:integration:view', 1, 0, '/system/integration', 'api', 180),
  ('操作日志', 'system:log:view', 1, 0, '/system/logs', 'file', 190)
ON DUPLICATE KEY UPDATE
  permission_name = VALUES(permission_name),
  type = VALUES(type),
  path = VALUES(path),
  icon = VALUES(icon),
  sort = VALUES(sort),
  is_deleted = 0;

INSERT INTO gc_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM gc_role r
JOIN gc_permission p ON (
  p.permission_code = 'home:view'
  OR (r.role_code = 'STUDENT' AND p.permission_code IN
      ('student:green:view', 'student:subsidy:view', 'message:view'))
  OR (r.role_code = 'TUTOR' AND p.permission_code IN
      ('tutor:student:view', 'tutor:review:view', 'tutor:application:view', 'message:view'))
  OR (r.role_code = 'COLLEGE_ADMIN' AND p.permission_code IN
      ('college:review:view', 'college:quota:view', 'college:report:view', 'message:view'))
  OR (r.role_code = 'SCHOOL_ADMIN' AND p.permission_code IN
      ('school:student:view', 'school:student:edit', 'school:tutor-type:edit',
       'school:batch:view', 'school:review:view',
       'school:fund:view', 'school:dashboard:view', 'message:view'))
  OR (r.role_code = 'SYSTEM_ADMIN' AND p.permission_code IN
      ('message:view', 'school:tutor-type:edit', 'system:user:view', 'system:rbac:view',
       'system:dictionary:view', 'system:dictionary:edit',
       'system:integration:view', 'system:log:view'))
)
WHERE r.role_code IN ('STUDENT', 'TUTOR', 'COLLEGE_ADMIN', 'SCHOOL_ADMIN', 'SYSTEM_ADMIN')
ON DUPLICATE KEY UPDATE is_deleted = 0;

INSERT INTO gc_message_template
  (event_code, title_template, content_template, message_type, channels, status)
VALUES
  ('GIFT_APPLY_SUBMITTED', '绿色通道申请已提交', '您的绿色通道申请已提交，业务编号：${businessId}', 'BUSINESS', 'IN_APP,SMS,WECHAT,EMAIL', 1),
  ('GIFT_REVIEW_RETURNED', '绿色通道申请被退回', '您的绿色通道申请需要补充材料，业务编号：${businessId}', 'BUSINESS', 'IN_APP,SMS,WECHAT,EMAIL', 1),
  ('GIFT_REVIEW_PASSED', '绿色通道申请已通过', '您的绿色通道申请已审核通过，业务编号：${businessId}', 'BUSINESS', 'IN_APP,SMS,WECHAT,EMAIL', 1),
  ('SUBSIDY_APPLY_SUBMITTED', '困难补助申请已提交', '您的困难补助申请已提交，业务编号：${businessId}', 'BUSINESS', 'IN_APP,SMS,WECHAT,EMAIL', 1),
  ('SUBSIDY_REVIEW_RETURNED', '困难补助申请被退回', '您的困难补助申请需要补充材料，业务编号：${businessId}', 'BUSINESS', 'IN_APP,SMS,WECHAT,EMAIL', 1),
  ('SUBSIDY_REVIEW_PASSED', '困难补助申请已通过', '您的困难补助申请已审核通过，业务编号：${businessId}', 'BUSINESS', 'IN_APP,SMS,WECHAT,EMAIL', 1),
  ('PAYMENT_STATUS_CHANGED', '资助发放状态已更新', '您的资助发放状态已更新，业务编号：${businessId}', 'IMPORTANT', 'IN_APP,SMS,WECHAT,EMAIL', 1),
  ('WORK_STUDY_HIRED', '勤工助学录用通知', '您已被勤工助学岗位录用，业务编号：${businessId}', 'IMPORTANT', 'IN_APP,SMS,WECHAT,EMAIL', 1),
  ('WORK_STUDY_AGREEMENT_PENDING', '勤工助学协议待签署', '您有一份勤工助学协议待签署，业务编号：${businessId}', 'IMPORTANT', 'IN_APP,SMS,WECHAT,EMAIL', 1)
ON DUPLICATE KEY UPDATE
  title_template = VALUES(title_template), content_template = VALUES(content_template),
  message_type = VALUES(message_type), channels = VALUES(channels), status = VALUES(status), is_deleted = 0;

INSERT INTO gc_tutor_apply_type
  (type_name, type_code, description, need_amount, need_student, approval_level, form_template, sort, status)
VALUES
  ('班级资助专项工作经费申请', 'CLASS_AID_FUND', '申请班级资助专项工作经费', 1, 0, 2,
   JSON_ARRAY(JSON_OBJECT('key','purpose','label','经费用途','type','textarea','required',true)), 10, 1),
  ('学生困难等级调整申请', 'POVERTY_LEVEL_CHANGE', '调整学生困难等级', 0, 1, 2,
   JSON_ARRAY(JSON_OBJECT('key','reason','label','调整原因','type','textarea','required',true)), 20, 1),
  ('资助材料补充提交申请', 'AID_MATERIAL_SUPPLEMENT', '补充提交资助材料', 0, 1, 1,
   JSON_ARRAY(JSON_OBJECT('key','materialNote','label','材料说明','type','textarea','required',true)), 30, 1),
  ('特殊情况说明申请', 'SPECIAL_CASE_NOTE', '提交特殊情况说明', 0, 1, 2,
   JSON_ARRAY(JSON_OBJECT('key','caseNote','label','情况说明','type','textarea','required',true)), 40, 1)
ON DUPLICATE KEY UPDATE
  type_name = VALUES(type_name), description = VALUES(description), need_amount = VALUES(need_amount),
  need_student = VALUES(need_student), approval_level = VALUES(approval_level),
  form_template = VALUES(form_template), sort = VALUES(sort), status = VALUES(status), is_deleted = 0;

-- ***********************
-- 初始化：组织架构 → 补助批次 → 资金分配
-- ***********************

-- ==========================================
-- 1. 学校
-- ==========================================
INSERT INTO gc_school (id, school_name, school_code, address, contact_phone, introduction)
VALUES
  (1, '四川大学', 'DEMO_UNIV', 'XX省XX市XX区XX路100号', '010-12345678', '示范大学是一所综合性大学，本数据仅用于开发联调。')
ON DUPLICATE KEY UPDATE school_name = VALUES(school_name), school_code = VALUES(school_code), is_deleted = 0;

-- ==========================================
-- 2. 学院
-- ==========================================
INSERT INTO gc_college (id, school_id, college_name, college_code, contact_person, contact_phone)
VALUES
  (1, 1, '计算机科学与技术学院', 'COLLEGE_CS', '李老师', '13800000001'),
  (2, 1, '数学与统计学院',       'COLLEGE_MATH', '王老师', '13800000002'),
  (3, 1, '经济管理学院',         'COLLEGE_EM',   '张老师', '13800000003')
ON DUPLICATE KEY UPDATE college_name = VALUES(college_name), college_code = VALUES(college_code), contact_person = VALUES(contact_person), contact_phone = VALUES(contact_phone), is_deleted = 0;

-- ==========================================
-- 3. 专业
-- ==========================================
INSERT INTO gc_major (id, college_id, major_name, major_code, school_length, degree_type)
VALUES
  (1, 1, '计算机科学与技术', 'CS_01', 4, '工学学士'),
  (2, 1, '软件工程',         'CS_02', 4, '工学学士'),
  (3, 1, '数据科学与大数据', 'CS_03', 4, '工学学士'),
  (4, 2, '数学与应用数学',   'MATH_01', 4, '理学学士'),
  (5, 2, '统计学',           'MATH_02', 4, '理学学士'),
  (6, 3, '工商管理',         'EM_01', 4, '管理学学士'),
  (7, 3, '经济学',           'EM_02', 4, '经济学学士')
ON DUPLICATE KEY UPDATE major_name = VALUES(major_name), major_code = VALUES(major_code), is_deleted = 0;

-- ==========================================
-- 4. 班级（grade 字段用于年级下拉列表数据源）
-- ==========================================
INSERT INTO gc_class (id, major_id, grade, class_name, class_code, tutor_id)
VALUES
  -- 计算机学院 — 2023 / 2024 / 2025 三个年级
  (1,  1, 2023, '计科2301班', 'CS2301', 2),
  (2,  1, 2023, '计科2302班', 'CS2302', 2),
  (3,  1, 2024, '计科2401班', 'CS2401', 2),
  (4,  1, 2024, '计科2402班', 'CS2402', 2),
  (5,  1, 2025, '计科2501班', 'CS2501', 2),
  (6,  2, 2023, '软工2301班', 'SE2301', 2),
  (7,  2, 2024, '软工2401班', 'SE2401', 2),
  (8,  2, 2025, '软工2501班', 'SE2501', 2),
  (9,  3, 2025, '大数据2501班', 'DS2501', 2),
  -- 数统学院 — 2023 / 2024 / 2025
  (10, 4, 2023, '数学2301班', 'MA2301', 2),
  (11, 4, 2024, '数学2401班', 'MA2401', 2),
  (12, 4, 2025, '数学2501班', 'MA2501', 2),
  (13, 5, 2024, '统计2401班', 'ST2401', 2),
  (14, 5, 2025, '统计2501班', 'ST2501', 2),
  -- 经管学院 — 2023 / 2024 / 2025
  (15, 6, 2024, '工商2401班', 'BA2401', 2),
  (16, 6, 2025, '工商2501班', 'BA2501', 2),
  (17, 7, 2023, '经济2301班', 'EC2301', 2),
  (18, 7, 2025, '经济2501班', 'EC2501', 2)
ON DUPLICATE KEY UPDATE class_name = VALUES(class_name), class_code = VALUES(class_code), is_deleted = 0;

-- ==========================================
-- 5. 补助批次（覆盖草稿/进行中/已结束三种状态）
--    subsidy_type: 1=集中批次  2=动态-大病补助  3=动态-受灾补助  4=动态-其他补助
-- ==========================================
INSERT INTO gc_subsidy_batch (id, batch_name, academic_year, subsidy_type, total_amount, apply_start_time, apply_end_time, college_submit_end_time, status, creator_id)
VALUES
  -- 已开始的集中批次（可测试 ACTIVE 状态下的额度分配规则）
  (1, '2026年秋季集中批次', '2025-2026', 1, 100000.00,
   '2026-08-01 00:00:00', '2026-08-31 23:59:59', '2026-08-10 23:59:59', 1, 5),
  -- 草稿批次（可测试 DRAFT 状态：学院可提前向年级分配额度）
  (2, '2026年高温补助', '2026-2027', 3, 80000.00,
   '2026-06-01 00:00:00', '2026-07-31 23:59:59', '2026-07-10 23:59:59', 0, 5),
  -- 已结束批次（历史记录）
  (3, '2025年秋季集中批次', '2024-2025', 1, 60000.00,
   '2025-08-01 00:00:00', '2025-08-31 23:59:59', '2025-08-10 23:59:59', 2, 5),
  -- 动态批次
  (4, '2026年大病补助批次', '2025-2026', 2, 30000.00,
   '2026-09-01 00:00:00', '2026-12-31 23:59:59', '2026-12-01 23:59:59', 1, 5)
ON DUPLICATE KEY UPDATE
  batch_name = VALUES(batch_name), total_amount = VALUES(total_amount),
  apply_start_time = VALUES(apply_start_time), apply_end_time = VALUES(apply_end_time),
  college_submit_end_time = VALUES(college_submit_end_time), status = VALUES(status), is_deleted = 0;

-- ==========================================
-- 6. 学校 → 学院 额度分配（allocator_role=1, target_type=1）
-- ==========================================
INSERT INTO gc_subsidy_allocation (batch_id, allocator_role, target_type, source_id, target_id, college_id, grade, allocated_amount, used_amount)
VALUES
  -- 批次1（ACTIVE）：学校向三个学院分配共 90000，剩余 10000 可用余额
  (1, 1, 1, 0, 1, 1, NULL, 50000.00, 0.00),
  (1, 1, 1, 0, 2, 2, NULL, 25000.00, 0.00),
  (1, 1, 1, 0, 3, 3, NULL, 15000.00, 0.00),
  -- 批次4（动态-大病补助）：学校向两个学院分配
  (4, 1, 1, 0, 1, 1, NULL, 15000.00, 0.00),
  (4, 1, 1, 0, 2, 2, NULL, 10000.00, 0.00)
ON DUPLICATE KEY UPDATE allocated_amount = VALUES(allocated_amount), used_amount = VALUES(used_amount), is_deleted = 0;

-- ==========================================
-- 7. 学院 → 年级 额度分配（allocator_role=2, target_type=2）
-- ==========================================
INSERT INTO gc_subsidy_allocation (batch_id, allocator_role, target_type, source_id, target_id, college_id, grade, allocated_amount, used_amount)
VALUES
  -- 批次1 / 计算机学院：向 2023/2024/2025 三个年级分配共 35000（余额 15000）
  (1, 2, 2, 1, 2023, 1, 2023, 15000.00, 0.00),
  (1, 2, 2, 1, 2024, 1, 2024, 12000.00, 0.00),
  (1, 2, 2, 1, 2025, 1, 2025,  8000.00, 0.00),
  -- 批次1 / 数统学院：向 2023/2024 年级分配共 20000（余额 5000）
  (1, 2, 2, 2, 2023, 2, 2023, 12000.00, 0.00),
  (1, 2, 2, 2, 2024, 2, 2024,  8000.00, 0.00),
  -- 批次1 / 经管学院：仅向 2024 年级分配 10000（余额 5000）
  (1, 2, 2, 3, 2024, 3, 2024, 10000.00, 0.00),
  -- 批次4 / 计算机学院：已向 2023 年级分配 10000（余额 5000）
  (4, 2, 2, 1, 2023, 1, 2023, 10000.00, 0.00)
ON DUPLICATE KEY UPDATE allocated_amount = VALUES(allocated_amount), used_amount = VALUES(used_amount), is_deleted = 0;

