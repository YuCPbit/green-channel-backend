package edu.greenchannel.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public class JdbcUserRepository implements UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        return jdbcTemplate.query("""
                        SELECT id, username, password_hash, real_name, user_type, status
                        FROM gc_user
                        WHERE username = ? AND is_deleted = 0
                        LIMIT 1
                        """,
                (resultSet, rowNum) -> {
                    long userId = resultSet.getLong("id");
                    List<String> roleCodes = queryRoleField(userId, "role_code");
                    List<String> roleNames = queryRoleField(userId, "role_name");
                    List<PermissionRow> permissions = queryPermissions(userId);
                    return new UserAccount(
                        userId,
                        resultSet.getString("username"),
                        resultSet.getString("password_hash"),
                        resultSet.getString("real_name"),
                        UserType.fromCode(resultSet.getInt("user_type")),
                        resultSet.getInt("status") == 1,
                        roleCodes,
                        roleNames,
                        permissions.stream().map(PermissionRow::code).toList(),
                        permissions.stream().filter(permission -> permission.type() == 1)
                                .map(PermissionRow::name).toList()
                    );
                }, username).stream().findFirst();
    }

    @Override
    public void updateLastLoginTime(long userId) {
        jdbcTemplate.update("UPDATE gc_user SET last_login_time = CURRENT_TIMESTAMP WHERE id = ?", userId);
    }

    private List<String> queryRoleField(long userId, String field) {
        if (!field.equals("role_code") && !field.equals("role_name")) {
            throw new IllegalArgumentException("不允许查询的角色字段");
        }
        return jdbcTemplate.queryForList("""
                SELECT r.%s
                FROM gc_role r
                JOIN gc_user_role ur ON ur.role_id = r.id AND ur.is_deleted = 0
                WHERE ur.user_id = ? AND r.status = 1 AND r.is_deleted = 0
                ORDER BY r.sort, r.id
                """.formatted(field), String.class, userId);
    }

    private List<PermissionRow> queryPermissions(long userId) {
        return jdbcTemplate.query("""
                SELECT DISTINCT p.permission_code, p.permission_name, p.type, p.sort, p.id
                FROM gc_permission p
                JOIN gc_role_permission rp ON rp.permission_id = p.id AND rp.is_deleted = 0
                JOIN gc_user_role ur ON ur.role_id = rp.role_id AND ur.is_deleted = 0
                JOIN gc_role r ON r.id = ur.role_id AND r.status = 1 AND r.is_deleted = 0
                WHERE ur.user_id = ? AND p.is_deleted = 0
                ORDER BY p.sort, p.id
                """, (resultSet, rowNum) -> new PermissionRow(
                resultSet.getString("permission_code"),
                resultSet.getString("permission_name"),
                resultSet.getInt("type")
        ), userId);
    }

    private record PermissionRow(String code, String name, int type) {
    }
}
