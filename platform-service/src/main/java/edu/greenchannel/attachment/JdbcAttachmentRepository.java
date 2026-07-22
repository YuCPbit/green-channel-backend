package edu.greenchannel.attachment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class JdbcAttachmentRepository implements AttachmentRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcAttachmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AttachmentRecord save(AttachmentRecord record) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO gc_attachment
                      (owner_id, original_name, stored_name, content_type, file_size, storage_path,
                       business_type, business_id, status, create_time)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, record.ownerId());
            statement.setString(2, record.originalName());
            statement.setString(3, record.storedName());
            statement.setString(4, record.contentType());
            statement.setLong(5, record.size());
            statement.setString(6, record.storagePath());
            statement.setString(7, record.businessType());
            if (record.businessId() == null) {
                statement.setNull(8, java.sql.Types.BIGINT);
            } else {
                statement.setLong(8, record.businessId());
            }
            statement.setString(9, record.status());
            statement.setTimestamp(10, Timestamp.valueOf(record.createdTime()));
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("附件记录主键生成失败");
        }
        return new AttachmentRecord(
                key.longValue(), record.ownerId(), record.originalName(), record.storedName(),
                record.contentType(), record.size(), record.storagePath(), record.businessType(),
                record.businessId(), record.status(), record.createdTime()
        );
    }

    @Override
    public Optional<AttachmentRecord> findById(long id) {
        return jdbcTemplate.query("""
                        SELECT id, owner_id, original_name, stored_name, content_type, file_size,
                               storage_path, business_type, business_id, status, create_time
                        FROM gc_attachment WHERE id = ? AND is_deleted = 0
                        """,
                (resultSet, rowNum) -> new AttachmentRecord(
                        resultSet.getLong("id"), resultSet.getLong("owner_id"),
                        resultSet.getString("original_name"), resultSet.getString("stored_name"),
                        resultSet.getString("content_type"), resultSet.getLong("file_size"),
                        resultSet.getString("storage_path"), resultSet.getString("business_type"),
                        resultSet.getObject("business_id", Long.class), resultSet.getString("status"),
                        resultSet.getTimestamp("create_time").toLocalDateTime()), id).stream().findFirst();
    }

    @Override
    public void bind(long id, String businessType, long businessId) {
        jdbcTemplate.update("""
                UPDATE gc_attachment SET business_type = ?, business_id = ?, update_time = CURRENT_TIMESTAMP
                WHERE id = ? AND is_deleted = 0
                """, businessType, businessId, id);
    }

    @Override
    public void softDelete(long id) {
        jdbcTemplate.update("""
                UPDATE gc_attachment SET is_deleted = 1, status = 'DELETED', update_time = CURRENT_TIMESTAMP
                WHERE id = ? AND is_deleted = 0
                """, id);
    }
}
