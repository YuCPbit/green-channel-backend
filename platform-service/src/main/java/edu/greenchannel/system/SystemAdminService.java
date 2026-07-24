package edu.greenchannel.system;

import edu.greenchannel.auth.UserType;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.common.PageResult;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SystemAdminService {
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SystemAdminService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record AdminUserView(long id, String username, String realName, String phone, String email,
            int userType, Long collegeId, int status, LocalDateTime lastLoginTime,
            List<Long> roleIds, List<String> roleNames) {
    }

    public record AdminUserRequest(String username, String password, String realName, String phone, String email,
            Integer userType, Long collegeId, Integer status, List<Long> roleIds) {
    }

    public record PasswordResetRequest(String password) {
    }

    public record RoleView(long id, String roleName, String roleCode, String description,
            int status, int sort, List<Long> permissionIds) {
    }

    public record RoleRequest(String roleName, String roleCode, String description,
            Integer status, Integer sort, List<Long> permissionIds) {
    }

    public record PermissionView(long id, String permissionName, String permissionCode, int type, String path) {
    }

    public record SystemConfigView(long id, String configName, String configKey, String configValue,
            String configType, String description, boolean editable) {
    }

    public record SystemConfigRequest(String configValue) {
    }

    public PageResult<AdminUserView> users(String keyword, Integer userType, Integer status, int page, int size) {
        requirePage(page, size);
        StringBuilder where = new StringBuilder(" WHERE u.is_deleted = 0");
        List<Object> args = new ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            where.append(" AND (u.username LIKE ? OR u.real_name LIKE ?)");
            String value = "%" + keyword.trim() + "%";
            args.add(value);
            args.add(value);
        }
        if (userType != null) {
            where.append(" AND u.user_type = ?");
            args.add(userType);
        }
        if (status != null) {
            where.append(" AND u.status = ?");
            args.add(status);
        }
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM gc_user u" + where, Long.class, args.toArray());
        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(size);
        pageArgs.add((page - 1) * size);
        List<AdminUserView> items = jdbcTemplate.query("""
                        SELECT u.id, u.username, u.real_name, u.phone, u.email, u.user_type,
                               u.college_id, u.status, u.last_login_time
                        FROM gc_user u
                        """ + where + " ORDER BY u.id DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> userView(
                        rs.getLong("id"), rs.getString("username"), rs.getString("real_name"),
                        rs.getString("phone"), rs.getString("email"), rs.getInt("user_type"),
                        rs.getObject("college_id", Long.class), rs.getInt("status"),
                        rs.getObject("last_login_time", LocalDateTime.class)),
                pageArgs.toArray());
        return new PageResult<>(items, total == null ? 0 : total, page, size);
    }

    @Transactional
    public AdminUserView createUser(AdminUserRequest request) {
        validateUser(request, true);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO gc_user
                          (username, password_hash, real_name, phone, email, user_type, college_id, status)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, request.username().trim());
                statement.setString(2, passwordEncoder.encode(request.password()));
                statement.setString(3, request.realName().trim());
                statement.setString(4, blankToNull(request.phone()));
                statement.setString(5, blankToNull(request.email()));
                statement.setInt(6, request.userType());
                statement.setObject(7, request.collegeId());
                statement.setInt(8, request.status() == null ? 1 : request.status());
                return statement;
            }, keyHolder);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(40900, "用户名已存在");
        }
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("用户主键生成失败");
        }
        long id = key.longValue();
        replaceUserRoles(id, request.roleIds());
        return findUser(id);
    }

    @Transactional
    public AdminUserView updateUser(long id, AdminUserRequest request) {
        requireExistingUser(id);
        validateUser(request, false);
        try {
            jdbcTemplate.update("""
                    UPDATE gc_user
                    SET username = ?, real_name = ?, phone = ?, email = ?, user_type = ?,
                        college_id = ?, status = ?, update_time = NOW()
                    WHERE id = ? AND is_deleted = 0
                    """, request.username().trim(), request.realName().trim(), blankToNull(request.phone()),
                    blankToNull(request.email()), request.userType(), request.collegeId(),
                    request.status() == null ? 1 : request.status(), id);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(40900, "用户名已存在");
        }
        replaceUserRoles(id, request.roleIds());
        return findUser(id);
    }

    public void resetPassword(long id, PasswordResetRequest request) {
        requireExistingUser(id);
        if (request == null || request.password() == null || request.password().length() < 8) {
            throw new BusinessException(40000, "新密码至少需要8个字符");
        }
        jdbcTemplate.update("""
                UPDATE gc_user SET password_hash = ?, update_time = NOW()
                WHERE id = ? AND is_deleted = 0
                """, passwordEncoder.encode(request.password()), id);
    }

    public List<RoleView> roles() {
        return jdbcTemplate.query("""
                        SELECT id, role_name, role_code, description, status, sort
                        FROM gc_role WHERE is_deleted = 0 ORDER BY sort, id
                        """,
                (rs, rowNum) -> roleView(
                        rs.getLong("id"), rs.getString("role_name"), rs.getString("role_code"),
                        rs.getString("description"), rs.getInt("status"), rs.getInt("sort")));
    }

    public List<PermissionView> permissions() {
        return jdbcTemplate.query("""
                        SELECT id, permission_name, permission_code, type, path
                        FROM gc_permission WHERE is_deleted = 0 ORDER BY sort, id
                        """,
                (rs, rowNum) -> new PermissionView(
                        rs.getLong("id"), rs.getString("permission_name"),
                        rs.getString("permission_code"), rs.getInt("type"), rs.getString("path")));
    }

    public List<SystemConfigView> configs() {
        return jdbcTemplate.query("""
                        SELECT id, config_name, config_key, config_value, config_type,
                               description, is_editable
                        FROM gc_system_config WHERE is_deleted = 0 ORDER BY id
                        """,
                (rs, rowNum) -> new SystemConfigView(
                        rs.getLong("id"), rs.getString("config_name"), rs.getString("config_key"),
                        rs.getString("config_value"), rs.getString("config_type"),
                        rs.getString("description"), rs.getInt("is_editable") == 1));
    }

    public SystemConfigView updateConfig(long id, SystemConfigRequest request) {
        SystemConfigView existing = configs().stream().filter(config -> config.id() == id)
                .findFirst().orElseThrow(() -> new BusinessException(40400, "系统参数不存在"));
        if (!existing.editable()) {
            throw new BusinessException(40300, "该系统参数不允许在线修改");
        }
        if (request == null || request.configValue() == null) {
            throw new BusinessException(40000, "参数值不能为空");
        }
        validateConfigValue(existing.configType(), request.configValue());
        jdbcTemplate.update("""
                UPDATE gc_system_config SET config_value = ?, update_time = NOW()
                WHERE id = ? AND is_deleted = 0
                """, request.configValue().trim(), id);
        return configs().stream().filter(config -> config.id() == id).findFirst().orElseThrow();
    }

    @Transactional
    public RoleView createRole(RoleRequest request) {
        validateRole(request);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO gc_role (role_name, role_code, description, status, sort)
                        VALUES (?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, request.roleName().trim());
                statement.setString(2, request.roleCode().trim());
                statement.setString(3, blankToNull(request.description()));
                statement.setInt(4, request.status() == null ? 1 : request.status());
                statement.setInt(5, request.sort() == null ? 0 : request.sort());
                return statement;
            }, keyHolder);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(40900, "角色编码已存在");
        }
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("角色主键生成失败");
        }
        long id = key.longValue();
        replaceRolePermissions(id, request.permissionIds());
        return findRole(id);
    }

    @Transactional
    public RoleView updateRole(long id, RoleRequest request) {
        validateRole(request);
        requireExistingRole(id);
        try {
            jdbcTemplate.update("""
                    UPDATE gc_role SET role_name = ?, role_code = ?, description = ?,
                        status = ?, sort = ?, update_time = NOW()
                    WHERE id = ? AND is_deleted = 0
                    """, request.roleName().trim(), request.roleCode().trim(),
                    blankToNull(request.description()), request.status() == null ? 1 : request.status(),
                    request.sort() == null ? 0 : request.sort(), id);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(40900, "角色编码已存在");
        }
        replaceRolePermissions(id, request.permissionIds());
        return findRole(id);
    }

    private AdminUserView findUser(long id) {
        return jdbcTemplate.query("""
                        SELECT id, username, real_name, phone, email, user_type,
                               college_id, status, last_login_time
                        FROM gc_user WHERE id = ? AND is_deleted = 0
                        """,
                (rs, rowNum) -> userView(
                        rs.getLong("id"), rs.getString("username"), rs.getString("real_name"),
                        rs.getString("phone"), rs.getString("email"), rs.getInt("user_type"),
                        rs.getObject("college_id", Long.class), rs.getInt("status"),
                        rs.getObject("last_login_time", LocalDateTime.class)), id)
                .stream().findFirst().orElseThrow(() -> new BusinessException(40400, "用户不存在"));
    }

    private AdminUserView userView(long id, String username, String realName, String phone, String email,
            int userType, Long collegeId, int status, LocalDateTime lastLoginTime) {
        List<Long> roleIds = jdbcTemplate.queryForList("""
                SELECT r.id FROM gc_role r
                JOIN gc_user_role ur ON ur.role_id = r.id AND ur.is_deleted = 0
                WHERE ur.user_id = ? AND r.is_deleted = 0 ORDER BY r.sort, r.id
                """, Long.class, id);
        List<String> roleNames = jdbcTemplate.queryForList("""
                SELECT r.role_name FROM gc_role r
                JOIN gc_user_role ur ON ur.role_id = r.id AND ur.is_deleted = 0
                WHERE ur.user_id = ? AND r.is_deleted = 0 ORDER BY r.sort, r.id
                """, String.class, id);
        return new AdminUserView(id, username, realName, phone, email, userType, collegeId,
                status, lastLoginTime, roleIds, roleNames);
    }

    private RoleView findRole(long id) {
        return jdbcTemplate.query("""
                        SELECT id, role_name, role_code, description, status, sort
                        FROM gc_role WHERE id = ? AND is_deleted = 0
                        """,
                (rs, rowNum) -> roleView(
                        rs.getLong("id"), rs.getString("role_name"), rs.getString("role_code"),
                        rs.getString("description"), rs.getInt("status"), rs.getInt("sort")), id)
                .stream().findFirst().orElseThrow(() -> new BusinessException(40400, "角色不存在"));
    }

    private RoleView roleView(long id, String name, String code, String description, int status, int sort) {
        List<Long> permissionIds = jdbcTemplate.queryForList("""
                SELECT permission_id FROM gc_role_permission
                WHERE role_id = ? AND is_deleted = 0 ORDER BY permission_id
                """, Long.class, id);
        return new RoleView(id, name, code, description, status, sort, permissionIds);
    }

    private void replaceUserRoles(long userId, List<Long> roleIds) {
        jdbcTemplate.update("UPDATE gc_user_role SET is_deleted = 1 WHERE user_id = ?", userId);
        if (roleIds == null) {
            return;
        }
        for (Long roleId : roleIds.stream().filter(id -> id != null).distinct().toList()) {
            requireExistingRole(roleId);
            jdbcTemplate.update("""
                    INSERT INTO gc_user_role (user_id, role_id, is_deleted)
                    VALUES (?, ?, 0)
                    ON DUPLICATE KEY UPDATE is_deleted = 0, update_time = NOW()
                    """, userId, roleId);
        }
    }

    private void replaceRolePermissions(long roleId, List<Long> permissionIds) {
        jdbcTemplate.update("UPDATE gc_role_permission SET is_deleted = 1 WHERE role_id = ?", roleId);
        if (permissionIds == null) {
            return;
        }
        for (Long permissionId : permissionIds.stream().filter(id -> id != null).distinct().toList()) {
            Long count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM gc_permission WHERE id = ? AND is_deleted = 0",
                    Long.class, permissionId);
            if (count == null || count == 0) {
                throw new BusinessException(40000, "所选权限不存在");
            }
            jdbcTemplate.update("""
                    INSERT INTO gc_role_permission (role_id, permission_id, is_deleted)
                    VALUES (?, ?, 0)
                    ON DUPLICATE KEY UPDATE is_deleted = 0, update_time = NOW()
                    """, roleId, permissionId);
        }
    }

    private void validateUser(AdminUserRequest request, boolean creating) {
        if (request == null || !StringUtils.hasText(request.username())
                || !StringUtils.hasText(request.realName()) || request.userType() == null) {
            throw new BusinessException(40000, "用户名、姓名和用户类型不能为空");
        }
        if (creating && (request.password() == null || request.password().length() < 8)) {
            throw new BusinessException(40000, "初始密码至少需要8个字符");
        }
        try {
            UserType.fromCode(request.userType());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(40000, "用户类型不正确");
        }
        if ((request.userType() == 2 || request.userType() == 3) && request.collegeId() == null) {
            throw new BusinessException(40000, "辅导员和学院管理员必须选择所属学院");
        }
        if (request.status() != null && request.status() != 0 && request.status() != 1) {
            throw new BusinessException(40000, "用户状态不正确");
        }
    }

    private void validateRole(RoleRequest request) {
        if (request == null || !StringUtils.hasText(request.roleName())
                || !StringUtils.hasText(request.roleCode())) {
            throw new BusinessException(40000, "角色名称和编码不能为空");
        }
        if (request.status() != null && request.status() != 0 && request.status() != 1) {
            throw new BusinessException(40000, "角色状态不正确");
        }
    }

    private void requireExistingUser(long id) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM gc_user WHERE id = ? AND is_deleted = 0", Long.class, id);
        if (count == null || count == 0) {
            throw new BusinessException(40400, "用户不存在");
        }
    }

    private void requireExistingRole(long id) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM gc_role WHERE id = ? AND is_deleted = 0", Long.class, id);
        if (count == null || count == 0) {
            throw new BusinessException(40400, "角色不存在");
        }
    }

    private void requirePage(int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BusinessException(40001, "分页参数不正确");
        }
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private void validateConfigValue(String type, String value) {
        String normalized = value.trim();
        try {
            if ("NUMBER".equalsIgnoreCase(type)) {
                new java.math.BigDecimal(normalized);
            } else if ("BOOLEAN".equalsIgnoreCase(type)
                    && !normalized.equalsIgnoreCase("true") && !normalized.equalsIgnoreCase("false")) {
                throw new IllegalArgumentException();
            }
        } catch (RuntimeException exception) {
            throw new BusinessException(40000, "参数值与配置类型不匹配");
        }
    }
}
