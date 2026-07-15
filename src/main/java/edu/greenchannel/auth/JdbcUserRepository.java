package edu.greenchannel.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
                (resultSet, rowNum) -> new UserAccount(
                        resultSet.getLong("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password_hash"),
                        resultSet.getString("real_name"),
                        UserType.fromCode(resultSet.getInt("user_type")),
                        resultSet.getInt("status") == 1
                ), username).stream().findFirst();
    }

    @Override
    public void updateLastLoginTime(long userId) {
        jdbcTemplate.update("UPDATE gc_user SET last_login_time = CURRENT_TIMESTAMP WHERE id = ?", userId);
    }
}

