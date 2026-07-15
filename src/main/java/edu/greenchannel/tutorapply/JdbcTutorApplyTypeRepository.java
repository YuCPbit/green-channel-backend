package edu.greenchannel.tutorapply;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcTutorApplyTypeRepository implements TutorApplyTypeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcTutorApplyTypeRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<TutorApplyType> findAll(boolean enabledOnly) {
        return jdbcTemplate.query("""
                SELECT id, type_name, type_code, description, need_amount, need_student,
                       approval_level, form_template, sort, status
                FROM gc_tutor_apply_type
                WHERE is_deleted = 0
                """ + (enabledOnly ? " AND status = 1" : "") + " ORDER BY sort, id",
                (resultSet, rowNum) -> map(
                        resultSet.getLong("id"), resultSet.getString("type_name"),
                        resultSet.getString("type_code"), resultSet.getString("description"),
                        resultSet.getInt("need_amount") == 1, resultSet.getInt("need_student") == 1,
                        resultSet.getInt("approval_level"), resultSet.getString("form_template"),
                        resultSet.getInt("sort"), resultSet.getInt("status") == 1));
    }

    @Override
    public Optional<TutorApplyType> findById(long id) {
        return jdbcTemplate.query("""
                SELECT id, type_name, type_code, description, need_amount, need_student,
                       approval_level, form_template, sort, status
                FROM gc_tutor_apply_type WHERE id = ? AND is_deleted = 0
                """, (resultSet, rowNum) -> map(
                resultSet.getLong("id"), resultSet.getString("type_name"),
                resultSet.getString("type_code"), resultSet.getString("description"),
                resultSet.getInt("need_amount") == 1, resultSet.getInt("need_student") == 1,
                resultSet.getInt("approval_level"), resultSet.getString("form_template"),
                resultSet.getInt("sort"), resultSet.getInt("status") == 1), id).stream().findFirst();
    }

    @Override
    public boolean existsByCode(String code, Long excludedId) {
        String sql = "SELECT COUNT(*) FROM gc_tutor_apply_type WHERE type_code = ? AND is_deleted = 0"
                + (excludedId == null ? "" : " AND id <> ?");
        Long count = excludedId == null
                ? jdbcTemplate.queryForObject(sql, Long.class, code)
                : jdbcTemplate.queryForObject(sql, Long.class, code, excludedId);
        return count != null && count > 0;
    }

    @Override
    public TutorApplyType insert(TutorApplyType type) {
        KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO gc_tutor_apply_type
                      (type_name, type_code, description, need_amount, need_student,
                       approval_level, form_template, sort, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            bind(statement, type);
            return statement;
        }, holder);
        Number key = holder.getKey();
        if (key == null) {
            throw new IllegalStateException("申请类型主键生成失败");
        }
        return new TutorApplyType(
                key.longValue(), type.typeName(), type.typeCode(), type.description(),
                type.needAmount(), type.needStudent(), type.approvalLevel(), type.formTemplate(),
                type.sort(), type.enabled());
    }

    @Override
    public TutorApplyType update(TutorApplyType type) {
        jdbcTemplate.update("""
                UPDATE gc_tutor_apply_type
                SET type_name = ?, type_code = ?, description = ?, need_amount = ?, need_student = ?,
                    approval_level = ?, form_template = ?, sort = ?, status = ?, update_time = NOW()
                WHERE id = ? AND is_deleted = 0
                """, type.typeName(), type.typeCode(), type.description(), type.needAmount() ? 1 : 0,
                type.needStudent() ? 1 : 0, type.approvalLevel(), toJson(type.formTemplate()),
                type.sort(), type.enabled() ? 1 : 0, type.id());
        return type;
    }

    @Override
    public void softDelete(long id) {
        jdbcTemplate.update("""
                UPDATE gc_tutor_apply_type SET is_deleted = 1, update_time = NOW()
                WHERE id = ? AND is_deleted = 0
                """, id);
    }

    private void bind(PreparedStatement statement, TutorApplyType type) throws java.sql.SQLException {
        statement.setString(1, type.typeName());
        statement.setString(2, type.typeCode());
        statement.setString(3, type.description());
        statement.setInt(4, type.needAmount() ? 1 : 0);
        statement.setInt(5, type.needStudent() ? 1 : 0);
        statement.setInt(6, type.approvalLevel());
        statement.setString(7, toJson(type.formTemplate()));
        statement.setInt(8, type.sort());
        statement.setInt(9, type.enabled() ? 1 : 0);
    }

    private TutorApplyType map(
            long id, String name, String code, String description, boolean needAmount,
            boolean needStudent, int approvalLevel, String template, int sort, boolean enabled) {
        return new TutorApplyType(
                id, name, code, description, needAmount, needStudent, approvalLevel,
                fromJson(template), sort, enabled);
    }

    private String toJson(Object value) {
        try {
            return value == null ? null : objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException("表单模板无法序列化", exception);
        }
    }

    private Object fromJson(String value) {
        try {
            return value == null ? null : objectMapper.readTree(value);
        } catch (JacksonException exception) {
            return null;
        }
    }
}
