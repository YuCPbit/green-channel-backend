-- 状态：仅用于本地开发的初始化数据
-- 最后核验：2026-07-22
-- 禁止用于生产；演示或部署前必须更换所有开发密码

USE green_channel;

-- 本文件仅用于本地开发。五类账号的初始密码均为 Dev@123456。
-- 上线或演示前必须修改密码，禁止复用开发密码。
INSERT INTO gc_user (username, password_hash, real_name, user_type, status)
VALUES
  ('student01', '$2y$10$4UhlxDrySWZYt5vv4GXqWuOgEu99q/oQL0rs5eiga38VAwl12b8UW', '测试学生', 1, 1),
  ('tutor01', '$2y$10$4UhlxDrySWZYt5vv4GXqWuOgEu99q/oQL0rs5eiga38VAwl12b8UW', '测试辅导员', 2, 1),
  ('college01', '$2y$10$4UhlxDrySWZYt5vv4GXqWuOgEu99q/oQL0rs5eiga38VAwl12b8UW', '测试学院管理员', 3, 1),
  ('school01', '$2y$10$4UhlxDrySWZYt5vv4GXqWuOgEu99q/oQL0rs5eiga38VAwl12b8UW', '测试资助中心', 4, 1),
  ('admin01', '$2y$10$4UhlxDrySWZYt5vv4GXqWuOgEu99q/oQL0rs5eiga38VAwl12b8UW', '测试系统管理员', 5, 1)
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  real_name = VALUES(real_name),
  user_type = VALUES(user_type),
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
