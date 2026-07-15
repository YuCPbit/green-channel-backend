package edu.greenchannel.integration;

import edu.greenchannel.common.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcIntegrationRepository implements IntegrationRepository {
    private static final RowMapper<IntegrationCallLog> ROW_MAPPER = (resultSet, rowNum) -> new IntegrationCallLog(
            resultSet.getLong("id"), resultSet.getString("client_id"),
            resultSet.getString("request_method"), resultSet.getString("request_path"),
            resultSet.getInt("success") == 1, resultSet.getLong("duration_ms"),
            resultSet.getString("failure_type"), resultSet.getTimestamp("create_time").toLocalDateTime());

    private final JdbcTemplate jdbcTemplate;

    public JdbcIntegrationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(IntegrationCallLog log) {
        jdbcTemplate.update("""
                INSERT INTO gc_integration_call_log
                  (client_id, request_method, request_path, success, duration_ms, failure_type, create_time)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, log.clientId(), log.requestMethod(), log.requestPath(), log.success() ? 1 : 0,
                log.durationMs(), log.failureType(), log.createdTime());
    }

    @Override
    public PageResult<IntegrationCallLog> search(
            String clientId, Boolean success, int page, int size) {
        StringBuilder condition = new StringBuilder(" WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        if (StringUtils.hasText(clientId)) {
            condition.append(" AND client_id = ?");
            parameters.add(clientId.trim());
        }
        if (success != null) {
            condition.append(" AND success = ?");
            parameters.add(success ? 1 : 0);
        }
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM gc_integration_call_log" + condition,
                Long.class, parameters.toArray());
        List<Object> pageParameters = new ArrayList<>(parameters);
        pageParameters.add(size);
        pageParameters.add((page - 1) * size);
        List<IntegrationCallLog> items = jdbcTemplate.query("""
                        SELECT id, client_id, request_method, request_path, success,
                               duration_ms, failure_type, create_time
                        FROM gc_integration_call_log
                        """ + condition + " ORDER BY create_time DESC, id DESC LIMIT ? OFFSET ?",
                ROW_MAPPER, pageParameters.toArray());
        return new PageResult<>(items, total == null ? 0 : total, page, size);
    }
}
