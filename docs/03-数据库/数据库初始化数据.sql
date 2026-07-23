-- 状态：仅用于本地开发的初始化数据
-- 最后核验：2026-07-22
-- 禁止用于生产；演示或部署前必须更换所有开发密码

USE green_channel;

-- 本文件仅用于本地开发。五类账号的初始密码均为 Dev@123456。
-- 上线或演示前必须修改密码，禁止复用开发密码。
INSERT INTO gc_user (username, password_hash, real_name, user_type, college_id, status)
VALUES
  ('student01', '$2y$10$4UhlxDrySWZYt5vv4GXqWuOgEu99q/oQL0rs5eiga38VAwl12b8UW', '测试学生', 1, 1, 1),
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

-- 组织架构初始化数据（学校 → 学院 → 专业 → 班级）

INSERT INTO gc_school (id, school_name, school_code, address, contact_phone, introduction) VALUES
(1, '四川大学', '001', '四川省成都市', '010-12345678', '四川大学是一所综合性大学')
ON DUPLICATE KEY UPDATE
  school_name = VALUES(school_name), address = VALUES(address), is_deleted = 0;

INSERT INTO gc_college (id, school_id, college_name, college_code, contact_person, contact_phone) VALUES
(1, 1, '计算机科学与技术学院', 'CS', '张院长', '010-11111111'),
(2, 1, '电子信息工程学院', 'EE', '李院长', '010-22222222'),
(3, 1, '经济管理学院', 'EM', '王院长', '010-33333333')
ON DUPLICATE KEY UPDATE
  college_name = VALUES(college_name), contact_person = VALUES(contact_person), is_deleted = 0;

INSERT INTO gc_major (id, college_id, major_name, major_code, school_length, degree_type) VALUES
(1, 1, '计算机科学与技术', 'CS001', 4, '工学学士'),
(2, 2, '电子信息工程', 'EE001', 4, '工学学士'),
(3, 3, '工商管理', 'EM001', 4, '管理学学士')
ON DUPLICATE KEY UPDATE
  major_name = VALUES(major_name), school_length = VALUES(school_length), is_deleted = 0;

INSERT INTO gc_class (id, major_id, grade, class_name, class_code, tutor_id) VALUES
(1, 1, 2023, '计科2301', 'CS2301', (SELECT id FROM gc_user WHERE username = 'tutor01' AND is_deleted = 0 LIMIT 1)),
(2, 2, 2023, '电信2301', 'EE2301', (SELECT id FROM gc_user WHERE username = 'tutor01' AND is_deleted = 0 LIMIT 1)),
(3, 3, 2023, '工商2301', 'EM2301', (SELECT id FROM gc_user WHERE username = 'tutor01' AND is_deleted = 0 LIMIT 1))
ON DUPLICATE KEY UPDATE
  class_name = VALUES(class_name), tutor_id = VALUES(tutor_id), is_deleted = 0;

-- 学生信息初始化数据（补充测试学生，关联到班级）

INSERT INTO gc_student (id, user_id, student_no, name, gender, id_card, birth_date, nation,
  phone, email, home_address, enroll_year, college_id, major_id, class_id,
  poverty_level, family_economic_status, family_population, family_annual_income,
  is_registered_poor, is_low_income, is_extreme_poor, is_orphan_disabled, is_martyr_family, info_completed)
SELECT
  1,
  u.id,
  '2023001',
  '张三',
  1,
  'encrypted_id_card_001',
  '2004-05-15',
  '汉族',
  '13800001001',
  'zhangsan@test.edu.cn',
  '四川省南充市',
  2023,
  1, 1, 1,
  1,
  '父亲务农，母亲体弱，家庭收入微薄',
  5,
  12000.00,
  1, 1, 0, 0, 0,
  1
FROM gc_user u WHERE u.username = 'student01' AND u.is_deleted = 0
ON DUPLICATE KEY UPDATE
  name = VALUES(name), poverty_level = VALUES(poverty_level), is_deleted = 0;

-- 补充两个测试学生账号
INSERT INTO gc_user (username, password_hash, real_name, user_type, college_id, status)
VALUES
  ('student02', '$2y$10$4UhlxDrySWZYt5vv4GXqWuOgEu99q/oQL0rs5eiga38VAwl12b8UW', '李四', 1, 2, 1),
  ('student03', '$2y$10$4UhlxDrySWZYt5vv4GXqWuOgEu99q/oQL0rs5eiga38VAwl12b8UW', '王五', 1, 3, 1)
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash), real_name = VALUES(real_name),
  user_type = VALUES(user_type), college_id = VALUES(college_id), status = VALUES(status), is_deleted = 0;

INSERT INTO gc_user_role (user_id, role_id)
SELECT u.id, r.id
FROM gc_user u
JOIN gc_role r ON r.role_code = 'STUDENT'
WHERE u.username IN ('student02', 'student03')
ON DUPLICATE KEY UPDATE is_deleted = 0;

INSERT INTO gc_student (id, user_id, student_no, name, gender, id_card, birth_date, nation,
  phone, email, home_address, enroll_year, college_id, major_id, class_id,
  poverty_level, family_economic_status, family_population, family_annual_income,
  is_registered_poor, is_low_income, is_extreme_poor, is_orphan_disabled, is_martyr_family, info_completed)
SELECT
  2,
  u.id,
  '2023002',
  '李四',
  2,
  'encrypted_id_card_002',
  '2004-09-20',
  '回族',
  '13800002001',
  'lisi@test.edu.cn',
  'XX省XX市XX区XX街道2号',
  2023,
  2, 2, 2,
  2,
  '单亲家庭，母亲一人抚养',
  3,
  18000.00,
  0, 1, 0, 0, 0,
  1
FROM gc_user u WHERE u.username = 'student02' AND u.is_deleted = 0
ON DUPLICATE KEY UPDATE
  name = VALUES(name), poverty_level = VALUES(poverty_level), is_deleted = 0;

INSERT INTO gc_student (id, user_id, student_no, name, gender, id_card, birth_date, nation,
  phone, email, home_address, enroll_year, college_id, major_id, class_id,
  poverty_level, family_economic_status, family_population, family_annual_income,
  is_registered_poor, is_low_income, is_extreme_poor, is_orphan_disabled, is_martyr_family, info_completed)
SELECT
  3,
  u.id,
  '2023003',
  '王五',
  1,
  'encrypted_id_card_003',
  '2003-12-01',
  '汉族',
  '13800003001',
  'wangwu@test.edu.cn',
  'XX省XX市XX区XX街道3号',
  2023,
  3, 3, 3,
  3,
  '家庭人口多，劳动力少',
  6,
  25000.00,
  0, 0, 0, 0, 0,
  1
FROM gc_user u WHERE u.username = 'student03' AND u.is_deleted = 0
ON DUPLICATE KEY UPDATE
  name = VALUES(name), poverty_level = VALUES(poverty_level), is_deleted = 0;

-- ========================================================
-- 绿色通道批次初始化数据
-- ========================================================

INSERT INTO gc_green_channel_batch (id, batch_name, academic_year, semester,
  apply_start_time, apply_end_time, college_submit_end_time,
  fund_source, allow_grades, status, creator_id, remark)
VALUES (
  1,
  '2025-2026学年第一学期绿色通道',
  '2025-2026',
  1,
  '2025-08-20 00:00:00',
  '2025-09-15 23:59:59',
  '2025-09-20 23:59:59',
  '学校事业经费',
  JSON_ARRAY(2025, 2024, 2023, 2022),
  1,
  (SELECT id FROM gc_user WHERE username = 'school01' AND is_deleted = 0 LIMIT 1),
  '面向全体在校生开放绿色通道申请'
)
ON DUPLICATE KEY UPDATE
  batch_name = VALUES(batch_name), fund_source = VALUES(fund_source),
  status = VALUES(status), remark = VALUES(remark), is_deleted = 0;

-- 补助批次初始化数据

-- 批次1：生活补助（进行中）
INSERT INTO gc_subsidy_batch (id, batch_name, academic_year, subsidy_type,
  total_amount, apply_start_time, apply_end_time, college_submit_end_time, status, creator_id)
VALUES (
  1,
  '2025-2026学年生活补助第一批',
  '2025-2026',
  1,
  500000.00,
  '2025-09-01 00:00:00',
  '2025-10-15 23:59:59',
  '2025-10-20 23:59:59',
  1,
  (SELECT id FROM gc_user WHERE username = 'school01' AND is_deleted = 0 LIMIT 1)
)
ON DUPLICATE KEY UPDATE
  batch_name = VALUES(batch_name), total_amount = VALUES(total_amount),
  status = VALUES(status), is_deleted = 0;

-- 批次2：路费补助（草稿/未开始）
INSERT INTO gc_subsidy_batch (id, batch_name, academic_year, subsidy_type,
  total_amount, apply_start_time, apply_end_time, college_submit_end_time, status, creator_id)
VALUES (
  2,
  '2025-2026学年寒假路费补助',
  '2025-2026',
  2,
  100000.00,
  '2025-12-01 00:00:00',
  '2025-12-31 23:59:59',
  '2026-01-05 23:59:59',
  0,
  (SELECT id FROM gc_user WHERE username = 'school01' AND is_deleted = 0 LIMIT 1)
)
ON DUPLICATE KEY UPDATE
  batch_name = VALUES(batch_name), total_amount = VALUES(total_amount),
  status = VALUES(status), is_deleted = 0;

-- 批次3：临时困难补助（进行中）
INSERT INTO gc_subsidy_batch (id, batch_name, academic_year, subsidy_type,
  total_amount, apply_start_time, apply_end_time, college_submit_end_time, status, creator_id)
VALUES (
  3,
  '2025-2026学年临时困难补助',
  '2025-2026',
  3,
  200000.00,
  '2025-09-15 00:00:00',
  '2026-01-15 23:59:59',
  '2026-01-20 23:59:59',
  1,
  (SELECT id FROM gc_user WHERE username = 'school01' AND is_deleted = 0 LIMIT 1)
)
ON DUPLICATE KEY UPDATE
  batch_name = VALUES(batch_name), total_amount = VALUES(total_amount),
  status = VALUES(status), is_deleted = 0;

-- 补助资金分配初始化数据（学校 → 学院 / 学院 → 年级）

-- 批次1 学院级分配（grade 为 NULL 表示学院总额）
INSERT INTO gc_subsidy_allocation (id, batch_id, allocator_role, target_type, source_id, target_id, college_id, grade, allocated_amount, used_amount) VALUES
(1, 1, 1, 1, 0, 1, 1, NULL, 200000.00, 3000.00),
(2, 1, 1, 1, 0, 2, 2, NULL, 180000.00, 0.00),
(3, 1, 1, 1, 0, 3, 3, NULL, 120000.00, 0.00)
ON DUPLICATE KEY UPDATE
  allocated_amount = VALUES(allocated_amount), used_amount = VALUES(used_amount), is_deleted = 0;

-- 批次1 年级级分配（学院1下各年级的细化分配）
INSERT INTO gc_subsidy_allocation (id, batch_id, allocator_role, target_type, source_id, target_id, college_id, grade, allocated_amount, used_amount) VALUES
(4, 1, 2, 2, 1, 2025, 1, 2025, 50000.00, 0.00),
(5, 1, 2, 2, 1, 2024, 1, 2024, 50000.00, 0.00),
(6, 1, 2, 2, 1, 2023, 1, 2023, 80000.00, 3000.00),
(7, 1, 2, 2, 1, 2022, 1, 2022, 20000.00, 0.00),
(8, 1, 2, 2, 2, 2023, 2, 2023, 70000.00, 0.00),
(9, 1, 2, 2, 3, 2023, 3, 2023, 50000.00, 0.00)
ON DUPLICATE KEY UPDATE
  allocated_amount = VALUES(allocated_amount), used_amount = VALUES(used_amount), is_deleted = 0;

-- 批次3 学院级分配
INSERT INTO gc_subsidy_allocation (id, batch_id, allocator_role, target_type, source_id, target_id, college_id, grade, allocated_amount, used_amount) VALUES
(10, 3, 1, 1, 0, 1, 1, NULL, 80000.00, 5000.00),
(11, 3, 1, 1, 0, 2, 2, NULL, 70000.00, 0.00),
(12, 3, 1, 1, 0, 3, 3, NULL, 50000.00, 0.00)
ON DUPLICATE KEY UPDATE
  allocated_amount = VALUES(allocated_amount), used_amount = VALUES(used_amount), is_deleted = 0;

-- 补助申请初始化数据

-- 申请1：张三（student01）申请生活补助，已通过（status=4），审批金额3000
INSERT INTO gc_subsidy_apply (id, batch_id, student_id, applicant_type, applicant_user_id,
  apply_no, subsidy_type, apply_amount, approved_amount, apply_reason, status, apply_time)
SELECT
  1, 1,
  (SELECT id FROM gc_student WHERE student_no = '2023001' AND is_deleted = 0 LIMIT 1),
  1,
  (SELECT id FROM gc_user WHERE username = 'student01' AND is_deleted = 0 LIMIT 1),
  'SUB20250901001',
  1,
  3000.00,
  3000.00,
  '家庭经济困难，申请生活补助以缓解生活压力',
  4,
  '2025-09-05 10:30:00'
ON DUPLICATE KEY UPDATE
  apply_amount = VALUES(apply_amount), approved_amount = VALUES(approved_amount),
  status = VALUES(status), is_deleted = 0;

-- 申请2：李四（student02）申请生活补助，待学院审核（status=2，辅导员已通过）
INSERT INTO gc_subsidy_apply (id, batch_id, student_id, applicant_type, applicant_user_id,
  apply_no, subsidy_type, apply_amount, approved_amount, apply_reason, status, apply_time)
SELECT
  2, 1,
  (SELECT id FROM gc_student WHERE student_no = '2023002' AND is_deleted = 0 LIMIT 1),
  1,
  (SELECT id FROM gc_user WHERE username = 'student02' AND is_deleted = 0 LIMIT 1),
  'SUB20250910001',
  1,
  2000.00,
  NULL,
  '单亲家庭，母亲收入低，需要生活补助',
  2,
  '2025-09-10 14:20:00'
ON DUPLICATE KEY UPDATE
  apply_amount = VALUES(apply_amount), approved_amount = VALUES(approved_amount),
  status = VALUES(status), is_deleted = 0;

-- 申请3：王五（student03）申请生活补助，待辅导员审核（status=1）
INSERT INTO gc_subsidy_apply (id, batch_id, student_id, applicant_type, applicant_user_id,
  apply_no, subsidy_type, apply_amount, approved_amount, apply_reason, status, apply_time)
SELECT
  3, 1,
  (SELECT id FROM gc_student WHERE student_no = '2023003' AND is_deleted = 0 LIMIT 1),
  1,
  (SELECT id FROM gc_user WHERE username = 'student03' AND is_deleted = 0 LIMIT 1),
  'SUB20250912001',
  1,
  1500.00,
  NULL,
  '家庭人口多，经济困难，申请生活补助',
  1,
  '2025-09-12 09:15:00'
ON DUPLICATE KEY UPDATE
  apply_amount = VALUES(apply_amount), approved_amount = VALUES(approved_amount),
  status = VALUES(status), is_deleted = 0;

-- 申请4：辅导员为张三（student01）发起的临时困难补助，已驳回（status=5），审批金额5000
INSERT INTO gc_subsidy_apply (id, batch_id, student_id, applicant_type, applicant_user_id,
  apply_no, subsidy_type, apply_amount, approved_amount, apply_reason, status, apply_time)
SELECT
  4, 3,
  (SELECT id FROM gc_student WHERE student_no = '2023001' AND is_deleted = 0 LIMIT 1),
  2,
  (SELECT id FROM gc_user WHERE username = 'tutor01' AND is_deleted = 0 LIMIT 1),
  'SUB20251015001',
  3,
  5000.00,
  NULL,
  '学生家庭突发变故，父亲住院，急需临时困难补助',
  5,
  '2025-10-15 16:45:00'
ON DUPLICATE KEY UPDATE
  apply_amount = VALUES(apply_amount), approved_amount = VALUES(approved_amount),
  status = VALUES(status), is_deleted = 0;

-- 申请5：李四（student02）申请临时困难补助，待辅导员审核（status=1）
INSERT INTO gc_subsidy_apply (id, batch_id, student_id, applicant_type, applicant_user_id,
  apply_no, subsidy_type, apply_amount, approved_amount, apply_reason, status, apply_time)
SELECT
  5, 3,
  (SELECT id FROM gc_student WHERE student_no = '2023002' AND is_deleted = 0 LIMIT 1),
  1,
  (SELECT id FROM gc_user WHERE username = 'student02' AND is_deleted = 0 LIMIT 1),
  'SUB20251101001',
  3,
  3000.00,
  NULL,
  '家中遭遇自然灾害，房屋受损，申请临时困难补助',
  1,
  '2025-11-01 11:00:00'
ON DUPLICATE KEY UPDATE
  apply_amount = VALUES(apply_amount), approved_amount = VALUES(approved_amount),
  status = VALUES(status), is_deleted = 0;

-- ========================================================
-- 补助审核记录初始化数据
-- ========================================================

-- 申请1（已通过）的三级审核记录
INSERT INTO gc_subsidy_review (id, apply_id, reviewer_id, reviewer_role, action, comment, suggest_amount, review_time) VALUES
(1, 1, (SELECT id FROM gc_user WHERE username = 'tutor01' AND is_deleted = 0 LIMIT 1), 1, 1, '情况属实，同意推荐', 3000.00, '2025-09-06 08:30:00'),
(2, 1, (SELECT id FROM gc_user WHERE username = 'college01' AND is_deleted = 0 LIMIT 1), 2, 1, '学院审核通过', 3000.00, '2025-09-07 10:00:00'),
(3, 1, (SELECT id FROM gc_user WHERE username = 'school01' AND is_deleted = 0 LIMIT 1), 3, 1, '学校终审通过，同意发放3000元', 3000.00, '2025-09-08 14:00:00')
ON DUPLICATE KEY UPDATE
  action = VALUES(action), comment = VALUES(comment), is_deleted = 0;

-- 申请2（待学院审）的辅导员审核记录（已通过）
INSERT INTO gc_subsidy_review (id, apply_id, reviewer_id, reviewer_role, action, comment, suggest_amount, review_time) VALUES
(4, 2, (SELECT id FROM gc_user WHERE username = 'tutor01' AND is_deleted = 0 LIMIT 1), 1, 1, '情况属实，建议补助2000元', 2000.00, '2025-09-11 09:00:00')
ON DUPLICATE KEY UPDATE
  action = VALUES(action), comment = VALUES(comment), is_deleted = 0;

-- 申请4（已驳回）的辅导员和学院审核记录
INSERT INTO gc_subsidy_review (id, apply_id, reviewer_id, reviewer_role, action, comment, suggest_amount, review_time) VALUES
(5, 4, (SELECT id FROM gc_user WHERE username = 'tutor01' AND is_deleted = 0 LIMIT 1), 1, 1, '情况紧急，建议补助5000元', 5000.00, '2025-10-16 08:00:00'),
(6, 4, (SELECT id FROM gc_user WHERE username = 'college01' AND is_deleted = 0 LIMIT 1), 2, 2, '材料不完整，需补充医院证明和费用明细', NULL, '2025-10-17 15:30:00')
ON DUPLICATE KEY UPDATE
  action = VALUES(action), comment = VALUES(comment), is_deleted = 0;

-- ========================================================
-- 补助发放台账初始化数据
-- ========================================================

-- 台账1：张三生活补助3000元，待发放
INSERT INTO gc_subsidy_ledger (id, batch_id, apply_id, student_id, apply_no, subsidy_type,
  approved_amount, disburse_status, disburse_time, disburse_operator_id, bank_card_no, remark)
SELECT
  1, 1, 1,
  (SELECT id FROM gc_student WHERE student_no = '2023001' AND is_deleted = 0 LIMIT 1),
  'SUB20250901001',
  1,
  3000.00,
  0,
  NULL,
  NULL,
  '6222021234567890123',
  '待学校财务处统一发放'
ON DUPLICATE KEY UPDATE
  approved_amount = VALUES(approved_amount), disburse_status = VALUES(disburse_status),
  bank_card_no = VALUES(bank_card_no), remark = VALUES(remark), is_deleted = 0;

-- ========================================================
-- 补充字典数据（补助类型、发放状态等）
-- ========================================================

INSERT INTO gc_dictionary (dict_type_code, dict_type_name, item_code, item_name, sort) VALUES
('SUBSIDY_TYPE', '补助类型', '1', '生活补助', 1),
('SUBSIDY_TYPE', '补助类型', '2', '路费补助', 2),
('SUBSIDY_TYPE', '补助类型', '3', '临时困难补助', 3),
('DISBURSE_STATUS', '发放状态', '0', '待发放', 1),
('DISBURSE_STATUS', '发放状态', '1', '已发放', 2),
('DISBURSE_STATUS', '发放状态', '2', '发放失败', 3),
('BATCH_STATUS', '批次状态', '0', '未开始', 1),
('BATCH_STATUS', '批次状态', '1', '进行中', 2),
('BATCH_STATUS', '批次状态', '2', '已结束', 3),
('FUND_SOURCE', '资金来源', 'CENTRAL', '中央财政', 1),
('FUND_SOURCE', '资金来源', 'SCHOOL', '学校事业经费', 2),
('FUND_SOURCE', '资金来源', 'DONATION', '社会捐赠', 3)
ON DUPLICATE KEY UPDATE
  dict_type_name = VALUES(dict_type_name), item_name = VALUES(item_name), sort = VALUES(sort), is_deleted = 0;
