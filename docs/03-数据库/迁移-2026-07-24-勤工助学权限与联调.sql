-- 勤工助学主流程联调增量：补齐菜单、接口权限与五类演示角色授权。
-- 适用于已经执行过基础建库和初始化脚本的开发库，可重复执行。
USE green_channel;

INSERT INTO gc_permission
  (permission_name, permission_code, type, parent_id, path, icon, sort)
VALUES
  ('勤工助学岗位', 'workstudy:position:view', 1, 0, '/workstudy/positions', 'work', 37),
  ('我的勤工申请', 'workstudy:apply:view', 1, 0, '/workstudy/applications', 'form', 38),
  ('我的勤工考勤', 'workstudy:attendance:view', 1, 0, '/workstudy/attendance', 'clock', 39),
  ('我的工作评价', 'workstudy:evaluation:view', 1, 0, '/workstudy/evaluations', 'star', 40),
  ('我的勤工薪酬', 'workstudy:salary:view', 1, 0, '/workstudy/salaries', 'wallet', 41),
  ('我的勤工协议', 'workstudy:agreement:view', 1, 0, '/workstudy/agreements', 'file', 42),
  ('勤工助学批次', 'workstudy:batch:view', 1, 0, '/workstudy/batches', 'calendar', 118),
  ('勤工岗位管理', 'workstudy:position:publish', 1, 0, '/workstudy/position-manage', 'work', 119),
  ('勤工岗位审核', 'workstudy:position:approve', 1, 0, '/workstudy/position-review', 'audit', 120),
  ('勤工面试管理', 'workstudy:apply:review', 1, 0, '/workstudy/interviews', 'team', 121),
  ('勤工录用审批', 'workstudy:hire:approve', 1, 0, '/workstudy/hires', 'audit', 122),
  ('勤工考勤管理', 'workstudy:attendance:confirm', 1, 0, '/workstudy/attendance-manage', 'clock', 123),
  ('工作评价管理', 'workstudy:evaluation:submit', 1, 0, '/workstudy/evaluation-manage', 'star', 124),
  ('勤工薪酬确认', 'workstudy:salary:dept-confirm', 1, 0, '/workstudy/salary-confirm', 'wallet', 125),
  ('勤工薪酬管理', 'workstudy:salary:calculate', 1, 0, '/workstudy/salary-manage', 'wallet', 126),
  ('勤工协议管理', 'workstudy:agreement:renew', 1, 0, '/workstudy/agreement-manage', 'file', 127),
  ('创建勤工批次', 'workstudy:batch:create', 2, 0, NULL, NULL, 201),
  ('更新勤工批次', 'workstudy:batch:update', 2, 0, NULL, NULL, 202),
  ('删除勤工批次', 'workstudy:batch:delete', 2, 0, NULL, NULL, 203),
  ('提交岗位审核', 'workstudy:position:submit', 2, 0, NULL, NULL, 204),
  ('下架勤工岗位', 'workstudy:position:offline', 2, 0, NULL, NULL, 205),
  ('更新勤工岗位', 'workstudy:position:update', 2, 0, NULL, NULL, 206),
  ('提交勤工申请', 'workstudy:apply:submit', 2, 0, NULL, NULL, 207),
  ('录入面试结果', 'workstudy:apply:interview', 2, 0, NULL, NULL, 208),
  ('填写辅导员推荐', 'workstudy:apply:tutor-recommend', 2, 0, NULL, NULL, 209),
  ('查看录用记录', 'workstudy:hire:view', 2, 0, NULL, NULL, 210),
  ('办理离岗', 'workstudy:hire:leave', 2, 0, NULL, NULL, 211),
  ('勤工签到', 'workstudy:attendance:checkin', 2, 0, NULL, NULL, 212),
  ('勤工签退', 'workstudy:attendance:checkout', 2, 0, NULL, NULL, 213),
  ('申请补打卡', 'workstudy:attendance:repair', 2, 0, NULL, NULL, 214),
  ('审批勤工薪酬', 'workstudy:salary:school-approve', 2, 0, NULL, NULL, 215),
  ('标记薪酬发放', 'workstudy:salary:mark-paid', 2, 0, NULL, NULL, 216),
  ('签署勤工协议', 'workstudy:agreement:sign', 2, 0, NULL, NULL, 217)
ON DUPLICATE KEY UPDATE
  permission_name=VALUES(permission_name), type=VALUES(type), path=VALUES(path),
  icon=VALUES(icon), sort=VALUES(sort), is_deleted=0;

INSERT INTO gc_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM gc_role r
JOIN gc_permission p ON (
  (r.role_code='STUDENT' AND p.permission_code IN
    ('workstudy:position:view', 'workstudy:apply:view', 'workstudy:apply:submit',
     'workstudy:hire:view', 'workstudy:attendance:view', 'workstudy:attendance:checkin',
     'workstudy:attendance:checkout', 'workstudy:attendance:repair',
     'workstudy:evaluation:view', 'workstudy:salary:view',
     'workstudy:agreement:view', 'workstudy:agreement:sign'))
  OR (r.role_code='TUTOR' AND p.permission_code IN
    ('workstudy:position:view', 'workstudy:apply:view', 'workstudy:apply:review',
     'workstudy:apply:tutor-recommend', 'workstudy:attendance:view',
     'workstudy:attendance:confirm', 'workstudy:evaluation:view',
     'workstudy:evaluation:submit'))
  OR (r.role_code='COLLEGE_ADMIN' AND p.permission_code IN
    ('workstudy:batch:view', 'workstudy:position:view', 'workstudy:position:publish',
     'workstudy:position:submit', 'workstudy:position:update', 'workstudy:position:offline',
     'workstudy:apply:view', 'workstudy:apply:review', 'workstudy:apply:interview',
     'workstudy:attendance:view', 'workstudy:attendance:confirm',
     'workstudy:evaluation:view', 'workstudy:evaluation:submit',
     'workstudy:salary:view', 'workstudy:salary:dept-confirm'))
  OR (r.role_code='SCHOOL_ADMIN' AND p.permission_code IN
    ('workstudy:batch:view', 'workstudy:batch:create', 'workstudy:batch:update',
     'workstudy:batch:delete', 'workstudy:position:view', 'workstudy:position:approve',
     'workstudy:position:update', 'workstudy:position:offline',
     'workstudy:apply:view', 'workstudy:apply:review', 'workstudy:apply:interview',
     'workstudy:hire:view', 'workstudy:hire:approve', 'workstudy:hire:leave',
     'workstudy:attendance:view', 'workstudy:attendance:confirm',
     'workstudy:evaluation:view', 'workstudy:evaluation:submit',
     'workstudy:salary:view', 'workstudy:salary:calculate',
     'workstudy:salary:school-approve', 'workstudy:salary:mark-paid',
     'workstudy:agreement:view', 'workstudy:agreement:renew'))
  OR (r.role_code='SYSTEM_ADMIN' AND p.permission_code LIKE 'workstudy:%')
)
WHERE r.role_code IN ('STUDENT', 'TUTOR', 'COLLEGE_ADMIN', 'SCHOOL_ADMIN', 'SYSTEM_ADMIN')
ON DUPLICATE KEY UPDATE is_deleted=0;
