package edu.greenchannel.student;

import edu.greenchannel.common.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcStudentRepository implements StudentRepository {
    private static final RowMapper<StudentRecord> ROW_MAPPER = (resultSet, rowNum) -> new StudentRecord(
            resultSet.getLong("id"), resultSet.getLong("user_id"), resultSet.getString("student_no"),
            resultSet.getString("name"), resultSet.getObject("gender", Integer.class),
            resultSet.getString("id_card"), resultSet.getString("phone"), resultSet.getString("email"),
            resultSet.getInt("enroll_year"), resultSet.getLong("college_id"), resultSet.getLong("major_id"),
            resultSet.getObject("class_id", Long.class), resultSet.getString("student_type"),
            resultSet.getInt("info_completed") == 1);

    private static final String SELECT_FIELDS = """
            SELECT id, user_id, student_no, name, gender, id_card, phone, email,
                   enroll_year, college_id, major_id, class_id, student_type, info_completed
            FROM gc_student
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcStudentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean existsByStudentNo(String studentNo, Long excludedId) {
        String sql = "SELECT COUNT(*) FROM gc_student WHERE student_no = ? AND is_deleted = 0"
                + (excludedId == null ? "" : " AND id <> ?");
        Long count = excludedId == null
                ? jdbcTemplate.queryForObject(sql, Long.class, studentNo)
                : jdbcTemplate.queryForObject(sql, Long.class, studentNo, excludedId);
        if (count != null && count > 0) {
            return true;
        }
        Long userCount = excludedId == null
                ? jdbcTemplate.queryForObject("""
                        SELECT COUNT(*) FROM gc_user WHERE username = ? AND is_deleted = 0
                        """, Long.class, studentNo)
                : jdbcTemplate.queryForObject("""
                        SELECT COUNT(*) FROM gc_user
                        WHERE username = ? AND is_deleted = 0
                          AND id <> COALESCE((SELECT user_id FROM gc_student WHERE id = ?), -1)
                        """, Long.class, studentNo, excludedId);
        return userCount != null && userCount > 0;
    }

    @Override
    public StudentRecord insert(StudentRecord student, String passwordHash) {
        KeyHolder userKey = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO gc_user (username, password_hash, real_name, phone, email, user_type, status)
                    VALUES (?, ?, ?, ?, ?, 1, 1)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, student.studentNo());
            statement.setString(2, passwordHash);
            statement.setString(3, student.name());
            statement.setString(4, student.phone());
            statement.setString(5, student.email());
            return statement;
        }, userKey);
        long userId = requiredKey(userKey, "用户");
        jdbcTemplate.update("""
                INSERT INTO gc_user_role (user_id, role_id)
                SELECT ?, id FROM gc_role WHERE role_code = 'STUDENT' AND is_deleted = 0 LIMIT 1
                """, userId);

        KeyHolder studentKey = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO gc_student
                      (user_id, student_no, name, gender, id_card, phone, email, enroll_year,
                       college_id, major_id, class_id, student_type, info_completed)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            bindStudent(statement, new StudentRecord(
                    student.id(), userId, student.studentNo(), student.name(), student.gender(),
                    student.protectedIdCard(), student.phone(), student.email(), student.enrollYear(),
                    student.collegeId(), student.majorId(), student.classId(), student.studentType(),
                    student.infoCompleted()));
            return statement;
        }, studentKey);
        long studentId = requiredKey(studentKey, "学生");
        return new StudentRecord(
                studentId, userId, student.studentNo(), student.name(), student.gender(),
                student.protectedIdCard(), student.phone(), student.email(), student.enrollYear(),
                student.collegeId(), student.majorId(), student.classId(), student.studentType(),
                student.infoCompleted());
    }

    @Override
    public Optional<StudentRecord> findById(long id) {
        return jdbcTemplate.query(
                SELECT_FIELDS + " WHERE id = ? AND is_deleted = 0", ROW_MAPPER, id).stream().findFirst();
    }

    @Override
    public StudentRecord update(StudentRecord student) {
        jdbcTemplate.update("""
                UPDATE gc_student
                SET student_no = ?, name = ?, gender = ?, id_card = ?, phone = ?, email = ?,
                    enroll_year = ?, college_id = ?, major_id = ?, class_id = ?, student_type = ?,
                    info_completed = ?, update_time = NOW()
                WHERE id = ? AND is_deleted = 0
                """, student.studentNo(), student.name(), student.gender(), student.protectedIdCard(),
                student.phone(), student.email(), student.enrollYear(), student.collegeId(), student.majorId(),
                student.classId(), student.studentType(), student.infoCompleted() ? 1 : 0, student.id());
        jdbcTemplate.update("""
                UPDATE gc_user SET username = ?, real_name = ?, phone = ?, email = ?, update_time = NOW()
                WHERE id = ? AND is_deleted = 0
                """, student.studentNo(), student.name(), student.phone(), student.email(), student.userId());
        return student;
    }

    @Override
    public PageResult<StudentRecord> search(
            String studentNo, String name, Long collegeId, Integer enrollYear, int page, int size) {
        StringBuilder condition = new StringBuilder(" WHERE is_deleted = 0");
        List<Object> parameters = new ArrayList<>();
        appendLike(condition, parameters, "student_no", studentNo);
        appendLike(condition, parameters, "name", name);
        appendEqual(condition, parameters, "college_id", collegeId);
        appendEqual(condition, parameters, "enroll_year", enrollYear);
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM gc_student" + condition, Long.class, parameters.toArray());
        List<Object> pageParameters = new ArrayList<>(parameters);
        pageParameters.add(size);
        pageParameters.add((page - 1) * size);
        List<StudentRecord> items = jdbcTemplate.query(
                SELECT_FIELDS + condition + " ORDER BY enroll_year DESC, student_no LIMIT ? OFFSET ?",
                ROW_MAPPER, pageParameters.toArray());
        return new PageResult<>(items, total == null ? 0 : total, page, size);
    }

    @Override
    public boolean hasBusinessApplication(long studentId) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT
                  (SELECT COUNT(*) FROM gc_gift_pack_apply WHERE student_id = ? AND is_deleted = 0) +
                  (SELECT COUNT(*) FROM gc_subsidy_apply WHERE student_id = ? AND is_deleted = 0)
                """, Long.class, studentId, studentId);
        return count != null && count > 0;
    }

    @Override
    public void softDelete(long studentId, long userId) {
        jdbcTemplate.update("UPDATE gc_student SET is_deleted = 1, update_time = NOW() WHERE id = ?", studentId);
        jdbcTemplate.update("UPDATE gc_user SET is_deleted = 1, update_time = NOW() WHERE id = ?", userId);
    }

    private void bindStudent(PreparedStatement statement, StudentRecord student) throws java.sql.SQLException {
        statement.setLong(1, student.userId());
        statement.setString(2, student.studentNo());
        statement.setString(3, student.name());
        statement.setObject(4, student.gender());
        statement.setString(5, student.protectedIdCard());
        statement.setString(6, student.phone());
        statement.setString(7, student.email());
        statement.setInt(8, student.enrollYear());
        statement.setLong(9, student.collegeId());
        statement.setLong(10, student.majorId());
        statement.setObject(11, student.classId());
        statement.setString(12, student.studentType());
        statement.setInt(13, student.infoCompleted() ? 1 : 0);
    }

    private long requiredKey(KeyHolder holder, String entity) {
        Number key = holder.getKey();
        if (key == null) {
            throw new IllegalStateException(entity + "主键生成失败");
        }
        return key.longValue();
    }

    private void appendLike(StringBuilder sql, List<Object> parameters, String column, String value) {
        if (StringUtils.hasText(value)) {
            sql.append(" AND ").append(column).append(" LIKE ?");
            parameters.add("%" + value.trim() + "%");
        }
    }

    private void appendEqual(StringBuilder sql, List<Object> parameters, String column, Object value) {
        if (value != null) {
            sql.append(" AND ").append(column).append(" = ?");
            parameters.add(value);
        }
    }
}
