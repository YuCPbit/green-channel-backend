-- 勤工助学与绿色通道主流程联调增量：补齐菜单、接口权限与五类演示角色授权。
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
  ('签署勤工协议', 'workstudy:agreement:sign', 2, 0, NULL, NULL, 217),
  ('绿色通道审核', 'gift:review:view', 1, 0, '/gift/review', 'audit', 52),
  ('绿色通道批次管理', 'gift:green-batch:manage', 1, 0, '/gift/green-batches', 'calendar', 113),
  ('大礼包批次管理', 'gift:pack-batch:manage', 1, 0, '/gift/pack-batches', 'calendar', 114),
  ('大礼包物品管理', 'gift:item:manage', 1, 0, '/gift/items', 'gift', 115),
  ('名额分配管理', 'gift:quota:manage', 1, 0, '/gift/quotas', 'team', 116),
  ('礼包核销管理', 'gift:pickup:manage', 1, 0, '/gift/pickup', 'check', 117),
  ('补录管理', 'gift:supplement:manage', 1, 0, '/gift/supplement', 'form', 118),
  ('取消绿色通道终审', 'gift:review:school', 2, 0, NULL, NULL, 200),
  ('事务审批', 'college:tutor-review:view', 1, 0, '/tutor-review', 'audit', 72),
  ('事务审批', 'school:tutor-disburse:view', 1, 0, '/tutor-review', 'audit', 112),
  ('资金台账', 'school:ledger:menu', 1, 0, '/fund-ledger', 'file', 121)
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
    ('gift:review:view', 'workstudy:position:view', 'workstudy:apply:view', 'workstudy:apply:review',
     'workstudy:apply:tutor-recommend', 'workstudy:attendance:view',
     'workstudy:attendance:confirm', 'workstudy:evaluation:view',
     'workstudy:evaluation:submit'))
  OR (r.role_code='COLLEGE_ADMIN' AND p.permission_code IN
    ('gift:review:view', 'college:tutor-review:view',
     'workstudy:batch:view', 'workstudy:position:view', 'workstudy:position:publish',
     'workstudy:position:submit', 'workstudy:position:update', 'workstudy:position:offline',
     'workstudy:apply:view', 'workstudy:apply:review', 'workstudy:apply:interview',
     'workstudy:attendance:view', 'workstudy:attendance:confirm',
     'workstudy:evaluation:view', 'workstudy:evaluation:submit',
     'workstudy:salary:view', 'workstudy:salary:dept-confirm'))
  OR (r.role_code='SCHOOL_ADMIN' AND p.permission_code IN
    ('gift:review:view', 'gift:review:school', 'gift:green-batch:manage',
     'gift:pack-batch:manage', 'gift:item:manage', 'gift:quota:manage',
     'gift:pickup:manage', 'gift:supplement:manage',
     'school:tutor-disburse:view', 'school:ledger:menu',
     'workstudy:batch:view', 'workstudy:batch:create', 'workstudy:batch:update',
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
  OR (r.role_code='SYSTEM_ADMIN' AND p.permission_code LIKE 'gift:%')
)
WHERE r.role_code IN ('STUDENT', 'TUTOR', 'COLLEGE_ADMIN', 'SCHOOL_ADMIN', 'SYSTEM_ADMIN')
ON DUPLICATE KEY UPDATE is_deleted=0;

INSERT INTO gc_system_config
  (config_name, config_key, config_value, config_type, description, is_editable)
VALUES
  ('申诉窗口期天数', 'APPEAL_WINDOW_DAYS', '3', 'NUMBER', '审核不通过后允许申诉的天数', 1),
  ('勤工最低时薪', 'WORKSTUDY_MIN_HOURLY_WAGE', '12.00', 'NUMBER', '岗位发布允许的最低小时工资', 1),
  ('勤工每周工时上限', 'WORKSTUDY_MAX_WEEKLY_HOURS', '8', 'NUMBER', '学生单个录用岗位每周累计工时上限', 1)
ON DUPLICATE KEY UPDATE
  config_name = VALUES(config_name), config_type = VALUES(config_type),
  description = VALUES(description), is_editable = VALUES(is_editable), is_deleted = 0;

INSERT INTO gc_permission
  (permission_name, permission_code, type, parent_id, path, icon, sort)
VALUES
  ('用户编辑', 'system:user:edit', 2, 0, NULL, NULL, 155),
  ('角色权限编辑', 'system:rbac:edit', 2, 0, NULL, NULL, 165),
  ('事务类型配置', 'school:tutor-type:view', 1, 0, '/tutor-type-config', 'setting', 98)
ON DUPLICATE KEY UPDATE
  permission_name=VALUES(permission_name), type=VALUES(type), sort=VALUES(sort), is_deleted=0;

INSERT INTO gc_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM gc_role r
JOIN gc_permission p ON (
  (r.role_code='SYSTEM_ADMIN' AND p.permission_code IN
    ('system:user:edit', 'system:rbac:edit', 'school:tutor-type:view'))
  OR (r.role_code='SCHOOL_ADMIN' AND p.permission_code='school:tutor-type:view')
)
WHERE r.role_code IN ('SYSTEM_ADMIN', 'SCHOOL_ADMIN')
ON DUPLICATE KEY UPDATE is_deleted=0;
