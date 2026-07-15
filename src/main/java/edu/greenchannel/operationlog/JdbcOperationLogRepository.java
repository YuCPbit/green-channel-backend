package edu.greenchannel.operationlog;

import edu.greenchannel.common.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcOperationLogRepository implements OperationLogRepository {
    private static final RowMapper<OperationLogEntry> ROW_MAPPER = (resultSet, rowNum) -> new OperationLogEntry(
            resultSet.getLong("id"), resultSet.getObject("user_id", Long.class),
            resultSet.getString("module"), resultSet.getString("operation_type"),
            resultSet.getString("target_id"), resultSet.getString("request_method"),
            resultSet.getString("request_url"), resultSet.getString("ip_address"),
            resultSet.getInt("success") == 1, resultSet.getString("description"),
            resultSet.getTimestamp("operation_time").toLocalDateTime());

    private final JdbcTemplate jdbcTemplate;

    public JdbcOperationLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(OperationLogEntry entry) {
        jdbcTemplate.update("""
                INSERT INTO gc_operation_log
                  (user_id, operation_type, module, target_id, description, ip_address,
                   request_method, request_url, success, operation_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, entry.operatorId(), entry.action(), entry.module(), entry.targetId(), entry.description(),
                entry.ipAddress(), entry.requestMethod(), entry.requestUrl(), entry.success() ? 1 : 0,
                entry.operationTime());
    }

    @Override
    public PageResult<OperationLogEntry> search(
            String module, Long operatorId, String action, LocalDateTime startTime,
            LocalDateTime endTime, Boolean success, int page, int size) {
        StringBuilder condition = new StringBuilder(" WHERE is_deleted = 0");
        List<Object> parameters = new ArrayList<>();
        append(condition, parameters, " AND module = ?", module);
        append(condition, parameters, " AND user_id = ?", operatorId);
        append(condition, parameters, " AND operation_type = ?", action);
        append(condition, parameters, " AND operation_time >= ?", startTime);
        append(condition, parameters, " AND operation_time <= ?", endTime);
        if (success != null) {
            condition.append(" AND success = ?");
            parameters.add(success ? 1 : 0);
        }
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM gc_operation_log" + condition,
                Long.class, parameters.toArray());
        List<Object> pageParameters = new ArrayList<>(parameters);
        pageParameters.add(size);
        pageParameters.add((page - 1) * size);
        List<OperationLogEntry> items = jdbcTemplate.query("""
                        SELECT id, user_id, module, operation_type, target_id, request_method,
                               request_url, ip_address, success, description, operation_time
                        FROM gc_operation_log
                        """ + condition + " ORDER BY operation_time DESC, id DESC LIMIT ? OFFSET ?",
                ROW_MAPPER, pageParameters.toArray());
        return new PageResult<>(items, total == null ? 0 : total, page, size);
    }

    private void append(StringBuilder sql, List<Object> parameters, String fragment, Object value) {
        if (value == null || value instanceof String text && !StringUtils.hasText(text)) {
            return;
        }
        sql.append(fragment);
        parameters.add(value);
    }
}
