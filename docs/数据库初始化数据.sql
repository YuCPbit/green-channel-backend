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
  ('批次配置', 'school:batch:view', 1, 0, '/batch', 'calendar', 100),
  ('学校审核', 'school:review:view', 1, 0, '/school-review', 'audit', 110),
  ('资金管理', 'school:fund:view', 1, 0, '/fund', 'wallet', 120),
  ('数据看板', 'school:dashboard:view', 1, 0, '/dashboard', 'dashboard', 130),
  ('消息中心', 'message:view', 1, 0, '/messages', 'bell', 140),
  ('用户管理', 'system:user:view', 1, 0, '/system/users', 'user', 150),
  ('角色权限', 'system:rbac:view', 1, 0, '/system/rbac', 'safety', 160),
  ('字典参数', 'system:dictionary:view', 1, 0, '/system/dictionary', 'setting', 170),
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
      ('school:batch:view', 'school:review:view', 'school:fund:view', 'school:dashboard:view', 'message:view'))
  OR (r.role_code = 'SYSTEM_ADMIN' AND p.permission_code IN
      ('system:user:view', 'system:rbac:view', 'system:dictionary:view', 'system:integration:view', 'system:log:view'))
)
WHERE r.role_code IN ('STUDENT', 'TUTOR', 'COLLEGE_ADMIN', 'SCHOOL_ADMIN', 'SYSTEM_ADMIN')
ON DUPLICATE KEY UPDATE is_deleted = 0;

