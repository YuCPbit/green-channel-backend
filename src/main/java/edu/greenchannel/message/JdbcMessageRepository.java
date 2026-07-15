package edu.greenchannel.message;

import edu.greenchannel.common.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcMessageRepository implements MessageRepository {
    private static final RowMapper<MessageRecord> MESSAGE_MAPPER = (resultSet, rowNum) -> new MessageRecord(
            resultSet.getLong("id"), resultSet.getLong("receiver_user_id"),
            resultSet.getString("event_code"), resultSet.getString("business_id"),
            resultSet.getString("title"), resultSet.getString("content"),
            resultSet.getString("message_type"), resultSet.getInt("read_status") == 1,
            resultSet.getTimestamp("read_time") == null ? null : resultSet.getTimestamp("read_time").toLocalDateTime(),
            resultSet.getTimestamp("create_time").toLocalDateTime());

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcMessageRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<MessageTemplate> findTemplate(String eventCode) {
        return jdbcTemplate.query("""
                SELECT id, event_code, title_template, content_template, message_type, channels
                FROM gc_message_template WHERE event_code = ? AND status = 1 AND is_deleted = 0
                """, (resultSet, rowNum) -> new MessageTemplate(
                resultSet.getLong("id"), resultSet.getString("event_code"),
                resultSet.getString("title_template"), resultSet.getString("content_template"),
                resultSet.getString("message_type"), splitChannels(resultSet.getString("channels"))),
                eventCode).stream().findFirst();
    }

    @Override
    public MessageRecord insertMessage(MessageRecord message) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO gc_message
                      (receiver_user_id, event_code, business_id, title, content, message_type, read_status)
                    VALUES (?, ?, ?, ?, ?, ?, 0)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, message.receiverUserId());
            statement.setString(2, message.eventCode());
            statement.setString(3, message.businessId());
            statement.setString(4, message.title());
            statement.setString(5, message.content());
            statement.setString(6, message.messageType());
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("消息主键生成失败");
        }
        return new MessageRecord(
                key.longValue(), message.receiverUserId(), message.eventCode(), message.businessId(),
                message.title(), message.content(), message.messageType(), false, null, message.createdTime());
    }

    @Override
    public void saveDelivery(long messageId, String channel, String status, int attempts, String failureType) {
        jdbcTemplate.update("""
                INSERT INTO gc_message_delivery
                  (message_id, channel, delivery_status, attempt_count, failure_type, next_retry_time)
                VALUES (?, ?, ?, ?, ?, CASE WHEN ? = 'RETRY' THEN DATE_ADD(NOW(), INTERVAL 5 MINUTE) ELSE NULL END)
                """, messageId, channel, status, attempts, failureType, status);
    }

    @Override
    public void saveEventRetry(MessageEvent event, Map<String, Object> variables, String failureType) {
        jdbcTemplate.update("""
                INSERT INTO gc_message_event_retry
                  (event_code, receiver_user_id, business_id, variables_json, failure_type,
                   retry_status, attempt_count, next_retry_time)
                VALUES (?, ?, ?, ?, ?, 'PENDING', 0, DATE_ADD(NOW(), INTERVAL 5 MINUTE))
                """, event.eventType(), event.receiverUserId(), event.businessId(), toJson(variables), failureType);
    }

    @Override
    public PageResult<MessageRecord> search(
            long receiverUserId, Boolean readStatus, String messageType, int page, int size) {
        StringBuilder condition = new StringBuilder(" WHERE receiver_user_id = ? AND is_deleted = 0");
        List<Object> parameters = new ArrayList<>();
        parameters.add(receiverUserId);
        if (readStatus != null) {
            condition.append(" AND read_status = ?");
            parameters.add(readStatus ? 1 : 0);
        }
        if (StringUtils.hasText(messageType)) {
            condition.append(" AND message_type = ?");
            parameters.add(messageType);
        }
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM gc_message" + condition, Long.class, parameters.toArray());
        List<Object> pageParameters = new ArrayList<>(parameters);
        pageParameters.add(size);
        pageParameters.add((page - 1) * size);
        List<MessageRecord> items = jdbcTemplate.query("""
                        SELECT id, receiver_user_id, event_code, business_id, title, content,
                               message_type, read_status, read_time, create_time
                        FROM gc_message
                        """ + condition + " ORDER BY create_time DESC, id DESC LIMIT ? OFFSET ?",
                MESSAGE_MAPPER, pageParameters.toArray());
        return new PageResult<>(items, total == null ? 0 : total, page, size);
    }

    @Override
    public long countUnread(long receiverUserId) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM gc_message
                WHERE receiver_user_id = ? AND read_status = 0 AND is_deleted = 0
                """, Long.class, receiverUserId);
        return count == null ? 0 : count;
    }

    @Override
    public Optional<MessageRecord> findByIdAndReceiver(long id, long receiverUserId) {
        return jdbcTemplate.query("""
                SELECT id, receiver_user_id, event_code, business_id, title, content,
                       message_type, read_status, read_time, create_time
                FROM gc_message WHERE id = ? AND receiver_user_id = ? AND is_deleted = 0
                """, MESSAGE_MAPPER, id, receiverUserId).stream().findFirst();
    }

    @Override
    public void markRead(long id, long receiverUserId) {
        jdbcTemplate.update("""
                UPDATE gc_message SET read_status = 1, read_time = COALESCE(read_time, NOW()),
                       update_time = NOW()
                WHERE id = ? AND receiver_user_id = ? AND is_deleted = 0
                """, id, receiverUserId);
    }

    @Override
    public int markAllRead(long receiverUserId) {
        return jdbcTemplate.update("""
                UPDATE gc_message SET read_status = 1, read_time = NOW(), update_time = NOW()
                WHERE receiver_user_id = ? AND read_status = 0 AND is_deleted = 0
                """, receiverUserId);
    }

    private List<String> splitChannels(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            return "{}";
        }
    }
}
