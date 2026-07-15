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

