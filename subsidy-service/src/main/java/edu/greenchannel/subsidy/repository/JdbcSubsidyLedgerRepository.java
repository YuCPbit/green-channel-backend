package edu.greenchannel.subsidy.repository;

import edu.greenchannel.common.PageResult;
import edu.greenchannel.subsidy.dto.response.LedgerView;
import edu.greenchannel.subsidy.dto.response.LedgerSummaryResponse;
import edu.greenchannel.subsidy.entity.SubsidyLedgerRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 补助发放台账数据访问层 —— JdbcTemplate 实现。
 * 参考 JdbcSubsidyApplyRepository 的写法。
 */
@Repository
public class JdbcSubsidyLedgerRepository implements SubsidyLedgerRepository {

    /* ------ 台账记录 RowMapper ------ */
    private static final RowMapper<SubsidyLedgerRecord> LEDGER_RECORD_MAPPER = (rs, rowNum) -> new SubsidyLedgerRecord(
            rs.getLong("id"),
            rs.getLong("batch_id"),
            rs.getLong("apply_id"),
            rs.getLong("student_id"),
            rs.getString("apply_no"),
            rs.getInt("subsidy_type"),
            rs.getBigDecimal("approved_amount"),
            rs.getInt("disburse_status"),
            rs.getTimestamp("disburse_time") != null ? rs.getTimestamp("disburse_time").toLocalDateTime() : null,
            rs.getObject("disburse_operator_id", Long.class),
            rs.getString("bank_card_no"),
            rs.getString("remark"),
            rs.getTimestamp("create_time") != null ? rs.getTimestamp("create_time").toLocalDateTime() : null);

    /* ------ 台账视图 RowMapper（联表查询） ------ */
    private static final RowMapper<LedgerView> LEDGER_VIEW_MAPPER = (rs, rowNum) -> new LedgerView(
            rs.getLong("id"),
            rs.getLong("batch_id"),
            rs.getString("batch_name"),
            rs.getLong("apply_id"),
            rs.getString("apply_no"),
            rs.getLong("student_id"),
            rs.getString("student_name"),
            rs.getString("student_no"),
            rs.getObject("college_id", Long.class),
            rs.getString("college_name"),
            rs.getObject("grade", Integer.class),
            rs.getInt("subsidy_type"),
            rs.getString("subsidy_type_name"),
            rs.getBigDecimal("approved_amount"),
            rs.getInt("disburse_status"),
            rs.getString("disburse_status_name"),
            rs.getTimestamp("disburse_time") != null ? rs.getTimestamp("disburse_time").toLocalDateTime() : null,
            rs.getString("disburse_operator_name"),
            rs.getString("bank_card_no"),
            rs.getString("remark"),
            rs.getTimestamp("create_time") != null ? rs.getTimestamp("create_time").toLocalDateTime() : null);

    /** 台账列表查询的 SELECT + JOIN 片段 */
    private static final String LEDGER_VIEW_SELECT = """
            SELECT l.id, l.batch_id, b.batch_name,
                   l.apply_id, l.apply_no,
                   l.student_id, s.name AS student_name, s.student_no,
                   c.id AS college_id, c.college_name, s.enroll_year AS grade,
                   l.subsidy_type,
                   CASE l.subsidy_type
                     WHEN 1 THEN '生活补助' WHEN 2 THEN '路费补助' WHEN 3 THEN '临时困难补助'
                     ELSE '其他'
                   END AS subsidy_type_name,
                   l.approved_amount, l.disburse_status,
                   CASE l.disburse_status
                     WHEN 0 THEN '待发放' WHEN 1 THEN '已发放' WHEN 2 THEN '发放失败'
                     ELSE '未知'
                   END AS disburse_status_name,
                   l.disburse_time, u.real_name AS disburse_operator_name,
                   l.bank_card_no, l.remark, l.create_time
            FROM gc_subsidy_ledger l
            JOIN gc_subsidy_batch b ON l.batch_id = b.id
            JOIN gc_student s ON l.student_id = s.id
            JOIN gc_college c ON s.college_id = c.id
            LEFT JOIN gc_user u ON l.disburse_operator_id = u.id
            """;

    private final JdbcTemplate jdbc;

    public JdbcSubsidyLedgerRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* ======================== 基本 CRUD ======================== */

    @Override
    public SubsidyLedgerRecord insert(SubsidyLedgerRecord record) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO gc_subsidy_ledger
                      (batch_id, apply_id, student_id, apply_no, subsidy_type,
                       approved_amount, disburse_status, bank_card_no, remark, create_time)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, record.batchId());
            ps.setLong(2, record.applyId());
            ps.setLong(3, record.studentId());
            ps.setString(4, record.applyNo());
            ps.setInt(5, record.subsidyType());
            ps.setBigDecimal(6, record.approvedAmount());
            ps.setInt(7, record.disburseStatus());
            ps.setString(8, record.bankCardNo());
            ps.setString(9, record.remark());
            return ps;
        }, keyHolder);
        long id = requiredKey(keyHolder);
        return new SubsidyLedgerRecord(id, record.batchId(), record.applyId(), record.studentId(),
                record.applyNo(), record.subsidyType(), record.approvedAmount(),
                record.disburseStatus(), null, null,
                record.bankCardNo(), record.remark(), LocalDateTime.now());
    }

    @Override
    public Optional<SubsidyLedgerRecord> findById(long id) {
        return jdbc.query("""
                SELECT id, batch_id, apply_id, student_id, apply_no, subsidy_type,
                       approved_amount, disburse_status, disburse_time,
                       disburse_operator_id, bank_card_no, remark, create_time
                FROM gc_subsidy_ledger WHERE id = ? AND is_deleted = 0
                """, LEDGER_RECORD_MAPPER, id).stream().findFirst();
    }

    @Override
    public Optional<SubsidyLedgerRecord> findByApplyId(long applyId) {
        return jdbc.query("""
                SELECT id, batch_id, apply_id, student_id, apply_no, subsidy_type,
                       approved_amount, disburse_status, disburse_time,
                       disburse_operator_id, bank_card_no, remark, create_time
                FROM gc_subsidy_ledger WHERE apply_id = ? AND is_deleted = 0
                """, LEDGER_RECORD_MAPPER, applyId).stream().findFirst();
    }

    @Override
    public boolean existsByApplyId(long applyId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM gc_subsidy_ledger WHERE apply_id = ? AND is_deleted = 0",
                Long.class, applyId);
        return count != null && count > 0;
    }

    /* ======================== 分页查询 ======================== */

    @Override
    public PageResult<LedgerView> search(Long batchId, Integer disburseStatus, String studentName,
                                         Long collegeId, int page, int size) {
        StringBuilder cond = new StringBuilder(" WHERE l.is_deleted = 0");
        List<Object> params = new ArrayList<>();
        appendFilters(cond, params, batchId, disburseStatus, studentName, collegeId);

        String countSql = "SELECT COUNT(*) FROM gc_subsidy_ledger l "
                + "JOIN gc_student s ON l.student_id = s.id "
                + "JOIN gc_college c ON s.college_id = c.id" + cond;
        Long total = jdbc.queryForObject(countSql, Long.class, params.toArray());

        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(size);
        pageParams.add((page - 1) * size);
        String listSql = LEDGER_VIEW_SELECT + cond + " ORDER BY l.create_time DESC LIMIT ? OFFSET ?";
        List<LedgerView> items = jdbc.query(listSql, LEDGER_VIEW_MAPPER, pageParams.toArray());
        return new PageResult<>(items, total == null ? 0 : total, page, size);
    }

    /* ======================== 导出查询（不分页） ======================== */

    @Override
    public List<LedgerView> findAllForExport(Long batchId, Integer disburseStatus, Long collegeId) {
        StringBuilder cond = new StringBuilder(" WHERE l.is_deleted = 0");
        List<Object> params = new ArrayList<>();
        appendFilters(cond, params, batchId, disburseStatus, null, collegeId);
        return jdbc.query(LEDGER_VIEW_SELECT + cond + " ORDER BY l.create_time DESC",
                LEDGER_VIEW_MAPPER, params.toArray());
    }

    /* ======================== 汇总统计 ======================== */

    @Override
    public LedgerSummaryResponse summary(Long batchId) {
        StringBuilder cond = new StringBuilder(" WHERE is_deleted = 0");
        List<Object> params = new ArrayList<>();
        if (batchId != null) {
            cond.append(" AND batch_id = ?");
            params.add(batchId);
        }

        String sql = "SELECT "
                + "COALESCE(SUM(approved_amount), 0) AS total_amount, "
                + "COUNT(*) AS total_count, "
                + "COALESCE(SUM(CASE WHEN disburse_status = 0 THEN 1 ELSE 0 END), 0) AS pending_count, "
                + "COALESCE(SUM(CASE WHEN disburse_status = 0 THEN approved_amount ELSE 0 END), 0) AS pending_amount, "
                + "COALESCE(SUM(CASE WHEN disburse_status = 1 THEN 1 ELSE 0 END), 0) AS done_count, "
                + "COALESCE(SUM(CASE WHEN disburse_status = 1 THEN approved_amount ELSE 0 END), 0) AS done_amount, "
                + "COALESCE(SUM(CASE WHEN disburse_status = 2 THEN 1 ELSE 0 END), 0) AS failed_count "
                + "FROM gc_subsidy_ledger" + cond;

        return jdbc.query(sql, (rs, rowNum) -> new LedgerSummaryResponse(
                rs.getBigDecimal("total_amount"),
                rs.getLong("total_count"),
                rs.getLong("pending_count"),
                rs.getBigDecimal("pending_amount"),
                rs.getLong("done_count"),
                rs.getBigDecimal("done_amount"),
                rs.getLong("failed_count")
        ), params.toArray()).stream().findFirst().orElse(
                new LedgerSummaryResponse(BigDecimal.ZERO, 0, 0, BigDecimal.ZERO, 0, BigDecimal.ZERO, 0));
    }

    /* ======================== 更新发放状态 ======================== */

    @Override
    public int updateDisburseStatus(long id, int status, Long operatorId) {
        return jdbc.update("""
                UPDATE gc_subsidy_ledger
                SET disburse_status = ?, disburse_time = NOW(),
                    disburse_operator_id = ?, update_time = NOW()
                WHERE id = ? AND is_deleted = 0
                """, status, operatorId, id);
    }

    @Override
    public int updateDisburseWithRemark(long id, int status, Long operatorId, String remark) {
        return jdbc.update("""
                UPDATE gc_subsidy_ledger
                SET disburse_status = ?, disburse_time = NOW(),
                    disburse_operator_id = ?, remark = ?, update_time = NOW()
                WHERE id = ? AND is_deleted = 0
                """, status, operatorId, remark, id);
    }

    @Override
    public int batchUpdateDisburseStatus(List<Long> ids, int status, Long operatorId) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        // 构建 IN 子句的参数占位符
        StringBuilder placeholders = new StringBuilder();
        List<Object> params = new ArrayList<>();
        params.add(status);
        params.add(operatorId);
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) placeholders.append(", ");
            placeholders.append("?");
            params.add(ids.get(i));
        }
        String sql = "UPDATE gc_subsidy_ledger SET disburse_status = ?, disburse_time = NOW(), "
                + "disburse_operator_id = ?, update_time = NOW() "
                + "WHERE id IN (" + placeholders + ") AND is_deleted = 0";
        return jdbc.update(sql, params.toArray());
    }

    /* ======================== 内部方法 ======================== */

    /** 动态拼接筛选条件 */
    private void appendFilters(StringBuilder cond, List<Object> params,
                               Long batchId, Integer disburseStatus,
                               String studentName, Long collegeId) {
        if (batchId != null) {
            cond.append(" AND l.batch_id = ?");
            params.add(batchId);
        }
        if (disburseStatus != null) {
            cond.append(" AND l.disburse_status = ?");
            params.add(disburseStatus);
        }
        if (StringUtils.hasText(studentName)) {
            cond.append(" AND (s.name LIKE ? OR s.student_no LIKE ?)");
            String k = "%" + studentName.trim() + "%";
            params.add(k);
            params.add(k);
        }
        if (collegeId != null) {
            cond.append(" AND c.id = ?");
            params.add(collegeId);
        }
    }

    private long requiredKey(KeyHolder holder) {
        Number key = holder.getKey();
        if (key == null) {
            throw new IllegalStateException("台账主键生成失败");
        }
        return key.longValue();
    }
}
