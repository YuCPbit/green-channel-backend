package edu.greenchannel.subsidy.repository;

import edu.greenchannel.common.PageResult;
import edu.greenchannel.subsidy.entity.SubsidyApplyRecord;
import edu.greenchannel.subsidy.dto.response.SubsidyApplyView;
import edu.greenchannel.subsidy.entity.SubsidyReviewRecord;
import edu.greenchannel.subsidy.dto.response.SubsidyReviewView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcSubsidyApplyRepository implements SubsidyApplyRepository {

    private static final RowMapper<SubsidyApplyRecord> APPLY_RECORD_MAPPER = (rs, rowNum) -> new SubsidyApplyRecord(
            rs.getLong("id"), rs.getLong("batch_id"), rs.getLong("student_id"),
            rs.getInt("applicant_type"), rs.getLong("applicant_user_id"),
            rs.getString("apply_no"), rs.getInt("subsidy_type"),
            rs.getBigDecimal("apply_amount"), rs.getBigDecimal("approved_amount"),
            rs.getString("apply_reason"), rs.getInt("status"),
            rs.getTimestamp("apply_time") != null ? rs.getTimestamp("apply_time").toLocalDateTime() : null);

    private static final RowMapper<SubsidyApplyView> APPLY_VIEW_MAPPER = (rs, rowNum) -> new SubsidyApplyView(
            rs.getLong("id"), rs.getLong("batch_id"), rs.getString("batch_name"),
            rs.getLong("student_id"), rs.getString("student_name"), rs.getString("student_no"),
            rs.getObject("college_id", Long.class), rs.getString("college_name"),
            rs.getObject("grade", Integer.class), rs.getInt("applicant_type"),
            rs.getString("apply_no"), rs.getInt("subsidy_type"),
            rs.getBigDecimal("apply_amount"), rs.getBigDecimal("approved_amount"),
            rs.getString("apply_reason"), rs.getInt("status"),
            rs.getTimestamp("apply_time") != null ? rs.getTimestamp("apply_time").toLocalDateTime() : null,
            List.of());

    private static final RowMapper<SubsidyReviewRecord> REVIEW_RECORD_MAPPER = (rs, rowNum) -> new SubsidyReviewRecord(
            rs.getLong("id"), rs.getLong("apply_id"), rs.getLong("reviewer_id"),
            rs.getInt("reviewer_role"), rs.getInt("action"), rs.getString("comment"),
            rs.getBigDecimal("suggest_amount"),
            rs.getTimestamp("review_time") != null ? rs.getTimestamp("review_time").toLocalDateTime() : null);

    private static final RowMapper<SubsidyReviewView> REVIEW_VIEW_MAPPER = (rs, rowNum) -> new SubsidyReviewView(
            rs.getLong("id"), rs.getLong("apply_id"), rs.getLong("reviewer_id"),
            rs.getString("reviewer_name"), rs.getInt("reviewer_role"),
            rs.getString("reviewer_role_name"), rs.getInt("action"),
            rs.getString("action_name"), rs.getString("comment"),
            rs.getBigDecimal("suggest_amount"),
            rs.getTimestamp("review_time") != null ? rs.getTimestamp("review_time").toLocalDateTime() : null);

    private static final RowMapper<StudentBrief> STUDENT_BRIEF_MAPPER = (rs, rowNum) -> new StudentBrief(
            rs.getLong("student_id"), rs.getString("student_no"), rs.getString("name"),
            rs.getLong("college_id"), rs.getString("college_name"),
            rs.getObject("grade", Integer.class));

    private final JdbcTemplate jdbc;

    public JdbcSubsidyApplyRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---------------------------------------------------------------
    // Apply
    // ---------------------------------------------------------------

    @Override
    public String generateApplyNo() {
        String prefix = "SBDY" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String pattern = prefix + "%";
        Long maxSeq = jdbc.queryForObject(
                "SELECT COALESCE(MAX(CAST(SUBSTRING(apply_no, 13) AS UNSIGNED)), 0) + 1 " +
                        "FROM gc_subsidy_apply WHERE apply_no LIKE ? AND is_deleted = 0",
                Long.class, pattern);
        long seq = maxSeq == null ? 1 : maxSeq;
        return prefix + String.format("%06d", seq);
    }

    @Override
    public SubsidyApplyRecord insert(SubsidyApplyRecord record) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO gc_subsidy_apply
                      (batch_id, student_id, applicant_type, applicant_user_id, apply_no,
                       subsidy_type, apply_amount, apply_reason, status, apply_time)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, record.batchId());
            ps.setLong(2, record.studentId());
            ps.setInt(3, record.applicantType());
            ps.setLong(4, record.applicantUserId());
            ps.setString(5, record.applyNo());
            ps.setInt(6, record.subsidyType());
            ps.setBigDecimal(7, record.applyAmount());
            ps.setString(8, record.applyReason());
            ps.setInt(9, record.status());
            return ps;
        }, keyHolder);
        long id = requiredKey(keyHolder);
        return new SubsidyApplyRecord(id, record.batchId(), record.studentId(), record.applicantType(),
                record.applicantUserId(), record.applyNo(), record.subsidyType(), record.applyAmount(),
                record.approvedAmount(), record.applyReason(), record.status(), record.applyTime());
    }

    @Override
    public SubsidyApplyRecord update(SubsidyApplyRecord record) {
        jdbc.update("""
                UPDATE gc_subsidy_apply
                SET batch_id = ?, apply_amount = ?, apply_reason = ?, status = ?, update_time = NOW()
                WHERE id = ? AND is_deleted = 0
                """, record.batchId(), record.applyAmount(), record.applyReason(), record.status(), record.id());
        return record;
    }

    @Override
    public Optional<SubsidyApplyRecord> findById(long id) {
        return jdbc.query("""
                        SELECT id, batch_id, student_id, applicant_type, applicant_user_id,
                               apply_no, subsidy_type, apply_amount, approved_amount,
                               apply_reason, status, apply_time
                        FROM gc_subsidy_apply WHERE id = ? AND is_deleted = 0
                        """, APPLY_RECORD_MAPPER, id).stream().findFirst();
    }

    @Override
    public boolean existsActiveByBatchAndStudent(long batchId, long studentId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM gc_subsidy_apply WHERE batch_id = ? AND student_id = ? AND is_deleted = 0",
                Long.class, batchId, studentId);
        return count != null && count > 0;
    }

    @Override
    public void updateApplyStatus(long applyId, int status, BigDecimal approvedAmount) {
        if (approvedAmount != null) {
            jdbc.update("UPDATE gc_subsidy_apply SET status = ?, approved_amount = ?, update_time = NOW() WHERE id = ?",
                    status, approvedAmount, applyId);
        } else {
            jdbc.update("UPDATE gc_subsidy_apply SET status = ?, update_time = NOW() WHERE id = ?", status, applyId);
        }
    }

    // ---------------------------------------------------------------
    // User / student lookups
    // ---------------------------------------------------------------

    @Override
    public Optional<Long> findStudentIdByUserId(long userId) {
        return jdbc.query("SELECT id FROM gc_student WHERE user_id = ? AND is_deleted = 0",
                        (rs, rowNum) -> rs.getLong("id"), userId).stream().findFirst();
    }

    @Override
    public Optional<Long> findCollegeIdByUserId(long userId) {
        return jdbc.query("SELECT college_id FROM gc_user WHERE id = ? AND is_deleted = 0",
                        (rs, rowNum) -> rs.getObject("college_id", Long.class), userId)
                .stream().findFirst();
    }

    @Override
    public List<StudentBrief> searchStudentsInCollege(long collegeId, String keyword, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT s.id AS student_id, s.student_no, s.name, s.college_id,
                       c.college_name, s.enroll_year AS grade
                FROM gc_student s
                JOIN gc_college c ON s.college_id = c.id
                WHERE s.college_id = ? AND s.is_deleted = 0
                """);
        List<Object> params = new ArrayList<>();
        params.add(collegeId);
        if (StringUtils.hasText(keyword)) {
            sql.append(" AND (s.student_no LIKE ? OR s.name LIKE ?)");
            String k = "%" + keyword.trim() + "%";
            params.add(k);
            params.add(k);
        }
        sql.append(" ORDER BY s.student_no LIMIT ?");
        params.add(limit);
        return jdbc.query(sql.toString(), STUDENT_BRIEF_MAPPER, params.toArray());
    }

    // ---------------------------------------------------------------
    // Batch info
    // ---------------------------------------------------------------

    @Override
    public Optional<BatchInfo> getBatchInfo(long batchId) {
        return jdbc.query("""
                        SELECT id, batch_name, status, apply_start_time, apply_end_time, college_submit_end_time
                        FROM gc_subsidy_batch WHERE id = ? AND is_deleted = 0
                        """,
                (rs, rowNum) -> new BatchInfo(
                        rs.getLong("id"), rs.getString("batch_name"), rs.getInt("status"),
                        rs.getTimestamp("apply_start_time").toLocalDateTime(),
                        rs.getTimestamp("apply_end_time").toLocalDateTime(),
                        rs.getTimestamp("college_submit_end_time").toLocalDateTime()),
                batchId).stream().findFirst();
    }

    @Override
    public Optional<StudentInfo> getStudentInfo(long studentId) {
        return jdbc.query("""
                        SELECT s.id AS student_id, s.college_id, s.enroll_year
                        FROM gc_student s WHERE s.id = ? AND s.is_deleted = 0
                        """,
                (rs, rowNum) -> new StudentInfo(
                        rs.getLong("student_id"), rs.getLong("college_id"),
                        rs.getInt("enroll_year"), BigDecimal.ZERO),
                studentId).stream().findFirst();
    }

    // ---------------------------------------------------------------
    // Allocation / quota
    // ---------------------------------------------------------------

    @Override
    public Optional<AllocationInfo> findGradeAllocation(long batchId, long collegeId, int grade) {
        return jdbc.query("""
                        SELECT id, allocated_amount, used_amount
                        FROM gc_subsidy_allocation
                        WHERE batch_id = ? AND college_id = ? AND grade = ? AND target_type = 2 AND is_deleted = 0
                        """,
                (rs, rowNum) -> new AllocationInfo(
                        rs.getLong("id"), rs.getBigDecimal("allocated_amount"),
                        rs.getBigDecimal("used_amount")),
                batchId, collegeId, grade).stream().findFirst();
    }

    @Override
    public Optional<AllocationInfo> findCollegeAllocation(long batchId, long collegeId) {
        return jdbc.query("""
                        SELECT id, allocated_amount, used_amount
                        FROM gc_subsidy_allocation
                        WHERE batch_id = ? AND college_id = ? AND target_type = 1 AND is_deleted = 0
                        """,
                (rs, rowNum) -> new AllocationInfo(
                        rs.getLong("id"), rs.getBigDecimal("allocated_amount"),
                        rs.getBigDecimal("used_amount")),
                batchId, collegeId).stream().findFirst();
    }

    @Override
    public boolean incrementGradeAllocation(long allocationId, BigDecimal amount) {
        int rows = jdbc.update("""
                UPDATE gc_subsidy_allocation
                SET used_amount = used_amount + ?, update_time = NOW()
                WHERE id = ? AND is_deleted = 0 AND (used_amount + ?) <= allocated_amount
                """, amount, allocationId, amount);
        return rows > 0;
    }

    @Override
    public boolean incrementCollegeAllocation(long allocationId, BigDecimal amount) {
        int rows = jdbc.update("""
                UPDATE gc_subsidy_allocation
                SET used_amount = used_amount + ?, update_time = NOW()
                WHERE id = ? AND is_deleted = 0 AND (used_amount + ?) <= allocated_amount
                """, amount, allocationId, amount);
        return rows > 0;
    }

    // ---------------------------------------------------------------
    // Available batches
    // ---------------------------------------------------------------

    @Override
    public List<BatchInfo> findAvailableBatches() {
        return jdbc.query("""
                SELECT id, batch_name, status, apply_start_time, apply_end_time, college_submit_end_time
                FROM gc_subsidy_batch WHERE status = 1 AND is_deleted = 0 ORDER BY id DESC
                """,
                (rs, rowNum) -> new BatchInfo(
                        rs.getLong("id"), rs.getString("batch_name"), rs.getInt("status"),
                        rs.getTimestamp("apply_start_time").toLocalDateTime(),
                        rs.getTimestamp("apply_end_time").toLocalDateTime(),
                        rs.getTimestamp("college_submit_end_time").toLocalDateTime()));
    }

    // ---------------------------------------------------------------
    // Review
    // ---------------------------------------------------------------

    @Override
    public SubsidyReviewRecord insertReview(SubsidyReviewRecord record) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO gc_subsidy_review
                      (apply_id, reviewer_id, reviewer_role, action, comment, suggest_amount, review_time)
                    VALUES (?, ?, ?, ?, ?, ?, NOW())
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, record.applyId());
            ps.setLong(2, record.reviewerId());
            ps.setInt(3, record.reviewerRole());
            ps.setInt(4, record.action());
            ps.setString(5, record.comment());
            ps.setBigDecimal(6, record.suggestAmount());
            return ps;
        }, keyHolder);
        long id = requiredKey(keyHolder);
        return new SubsidyReviewRecord(id, record.applyId(), record.reviewerId(), record.reviewerRole(),
                record.action(), record.comment(), record.suggestAmount(), record.reviewTime());
    }

    @Override
    public List<SubsidyReviewView> findReviewsByApplyId(long applyId) {
        return jdbc.query("""
                SELECT r.id, r.apply_id, r.reviewer_id, u.real_name AS reviewer_name,
                       r.reviewer_role, r.action, r.comment, r.suggest_amount, r.review_time,
                       CASE r.reviewer_role
                         WHEN 1 THEN '辅导员'
                         WHEN 2 THEN '学院'
                         WHEN 3 THEN '学校'
                         ELSE '未知'
                       END AS reviewer_role_name,
                       CASE r.action
                         WHEN 1 THEN '通过'
                         WHEN 2 THEN '退回'
                         WHEN 3 THEN '不通过'
                         ELSE '未知'
                       END AS action_name
                FROM gc_subsidy_review r
                JOIN gc_user u ON r.reviewer_id = u.id
                WHERE r.apply_id = ? AND r.is_deleted = 0
                ORDER BY r.review_time ASC
                """, REVIEW_VIEW_MAPPER, applyId);
    }

    // ---------------------------------------------------------------
    // Role-filtered searches
    // ---------------------------------------------------------------

    private static final String APPLY_VIEW_SELECT = """
            SELECT a.id, a.batch_id, b.batch_name, a.student_id,
                   s.name AS student_name, s.student_no, s.college_id,
                   c.college_name, s.enroll_year AS grade,
                   a.applicant_type, a.apply_no, a.subsidy_type,
                   a.apply_amount, a.approved_amount, a.apply_reason,
                   a.status, a.apply_time
            FROM gc_subsidy_apply a
            JOIN gc_subsidy_batch b ON a.batch_id = b.id
            JOIN gc_student s ON a.student_id = s.id
            JOIN gc_college c ON s.college_id = c.id
            """;

    @Override
    public PageResult<SubsidyApplyView> searchForStudent(long studentUserId, Long batchId, Integer status, int page, int size) {
        StringBuilder cond = new StringBuilder(" WHERE a.is_deleted = 0");
        List<Object> params = new ArrayList<>();
        cond.append(" AND s.user_id = ?");
        params.add(studentUserId);
        appendFilters(cond, params, batchId, status, null, null);
        return executeSearch(cond, params, page, size);
    }

    @Override
    public PageResult<SubsidyApplyView> searchForCollege(long collegeId, Long batchId, Integer status, String studentName, int page, int size) {
        StringBuilder cond = new StringBuilder(" WHERE a.is_deleted = 0");
        List<Object> params = new ArrayList<>();
        cond.append(" AND s.college_id = ?");
        params.add(collegeId);
        appendFilters(cond, params, batchId, status, null, studentName);
        return executeSearch(cond, params, page, size);
    }

    @Override
    public PageResult<SubsidyApplyView> searchForSchool(Long batchId, Integer status, String studentName, Long collegeId, int page, int size) {
        StringBuilder cond = new StringBuilder(" WHERE a.is_deleted = 0");
        List<Object> params = new ArrayList<>();
        appendFilters(cond, params, batchId, status, collegeId, studentName);
        return executeSearch(cond, params, page, size);
    }

    private void appendFilters(StringBuilder cond, List<Object> params,
                               Long batchId, Integer status, Long collegeId, String studentName) {
        if (batchId != null) {
            cond.append(" AND a.batch_id = ?");
            params.add(batchId);
        }
        if (status != null) {
            cond.append(" AND a.status = ?");
            params.add(status);
        }
        if (collegeId != null) {
            cond.append(" AND s.college_id = ?");
            params.add(collegeId);
        }
        if (StringUtils.hasText(studentName)) {
            cond.append(" AND (s.name LIKE ? OR s.student_no LIKE ?)");
            String k = "%" + studentName.trim() + "%";
            params.add(k);
            params.add(k);
        }
    }

    private PageResult<SubsidyApplyView> executeSearch(StringBuilder cond, List<Object> params, int page, int size) {
        String countSql = "SELECT COUNT(*) FROM gc_subsidy_apply a JOIN gc_student s ON a.student_id = s.id JOIN gc_college c ON s.college_id = c.id JOIN gc_subsidy_batch b ON a.batch_id = b.id" + cond;
        Long total = jdbc.queryForObject(countSql, Long.class, params.toArray());
        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(size);
        pageParams.add((page - 1) * size);
        String listSql = APPLY_VIEW_SELECT + cond
                + " ORDER BY a.apply_time DESC LIMIT ? OFFSET ?";
        List<SubsidyApplyView> items = jdbc.query(listSql, APPLY_VIEW_MAPPER, pageParams.toArray());
        return new PageResult<>(items, total == null ? 0 : total, page, size);
    }

    // ---------------------------------------------------------------
    // Detail view
    // ---------------------------------------------------------------

    @Override
    public Optional<SubsidyApplyView> findDetailById(long id) {
        Optional<SubsidyApplyView> opt = jdbc.query(
                APPLY_VIEW_SELECT + " WHERE a.id = ? AND a.is_deleted = 0",
                APPLY_VIEW_MAPPER, id).stream().findFirst();
        if (opt.isPresent()) {
            SubsidyApplyView view = opt.get();
            List<SubsidyReviewView> reviews = findReviewsByApplyId(id);
            return Optional.of(new SubsidyApplyView(
                    view.id(), view.batchId(), view.batchName(), view.studentId(), view.studentName(),
                    view.studentNo(), view.collegeId(), view.collegeName(), view.grade(),
                    view.applicantType(), view.applyNo(), view.subsidyType(), view.applyAmount(),
                    view.approvedAmount(), view.applyReason(), view.status(), view.applyTime(), reviews));
        }
        return Optional.empty();
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private long requiredKey(KeyHolder holder) {
        Number key = holder.getKey();
        if (key == null) {
            throw new IllegalStateException("主键生成失败");
        }
        return key.longValue();
    }
}
