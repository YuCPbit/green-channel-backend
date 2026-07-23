package edu.greenchannel.subsidy.service;

import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.common.PageResult;
import edu.greenchannel.subsidy.dto.response.LedgerView;
import edu.greenchannel.subsidy.dto.response.LedgerSummaryResponse;
import edu.greenchannel.subsidy.dto.response.LedgerDisburseStatusResponse;
import edu.greenchannel.subsidy.entity.SubsidyApplyRecord;
import edu.greenchannel.subsidy.entity.SubsidyLedgerRecord;
import edu.greenchannel.subsidy.repository.SubsidyLedgerRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 补助发放台账服务实现。
 */
@Service
@Transactional
public class SubsidyLedgerServiceImpl implements SubsidyLedgerService {

    private final SubsidyLedgerRepository ledgerRepository;
    private final JdbcTemplate jdbc;

    public SubsidyLedgerServiceImpl(SubsidyLedgerRepository ledgerRepository, JdbcTemplate jdbc) {
        this.ledgerRepository = ledgerRepository;
        this.jdbc = jdbc;
    }

    /* ======================== 生成台账 ======================== */

    @Override
    public void generateLedger(SubsidyApplyRecord approvedApply) {
        // 幂等检查：已存在则跳过
        if (ledgerRepository.existsByApplyId(approvedApply.id())) {
            return;
        }

        // 查询学生信息（学号、姓名、学院、年级、银行卡号）
        var row = jdbc.query("""
                SELECT s.id AS student_id, s.student_no, s.name AS student_name,
                       s.college_id, c.college_name, s.enroll_year AS grade
                FROM gc_student s
                JOIN gc_college c ON s.college_id = c.id
                WHERE s.id = ? AND s.is_deleted = 0
                """,
                (rs, rowNum) -> new Object() {
                    long studentId = rs.getLong("student_id");
                    String studentNo = rs.getString("student_no");
                    String studentName = rs.getString("student_name");
                    long collegeId = rs.getLong("college_id");
                    String collegeName = rs.getString("college_name");
                    Integer grade = rs.getObject("grade", Integer.class);
                }, approvedApply.studentId()).stream().findFirst()
                .orElseThrow(() -> new BusinessException(40400, "学生档案不存在"));

        // 创建台账记录
        BigDecimal finalAmount = approvedApply.approvedAmount() != null
                ? approvedApply.approvedAmount() : BigDecimal.ZERO;

        SubsidyLedgerRecord record = new SubsidyLedgerRecord(
                0, approvedApply.batchId(), approvedApply.id(), approvedApply.studentId(),
                approvedApply.applyNo(), approvedApply.subsidyType(), finalAmount,
                SubsidyLedgerRecord.DISBURSE_PENDING, null, null, null, null, null);

        ledgerRepository.insert(record);
    }

    /* ======================== 查询 ======================== */

    @Override
    public PageResult<LedgerView> search(Long batchId, Integer disburseStatus, String studentName,
                                         Long collegeId, int page, int size) {
        return ledgerRepository.search(batchId, disburseStatus, studentName, collegeId, page, size);
    }

    @Override
    public LedgerView getDetail(long id) {
        // 通过 search 查询单条（page=1, size=1 拿第一条，确保 SQL 一致）
        // 实际通过 ledgerRepository.findById 拿 record，再拼装视图更精确
        var record = ledgerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(40400, "台账记录不存在"));

        // 复用 search 拿视图
        var result = ledgerRepository.search(null, null, null, null, 1, 1000);
        return result.items().stream()
                .filter(v -> v.id() == id)
                .findFirst()
                .orElseThrow(() -> new BusinessException(40400, "台账记录不存在"));
    }

    @Override
    public LedgerSummaryResponse getSummary(Long batchId) {
        return ledgerRepository.summary(batchId);
    }

    /* ======================== 发放确认 ======================== */

    @Override
    public void confirmDisburse(long id, CurrentUser operator) {
        var record = ledgerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(40400, "台账记录不存在"));
        if (record.disburseStatus() == SubsidyLedgerRecord.DISBURSE_DONE) {
            throw new BusinessException(40900, "该记录已发放，不可重复操作");
        }
        int rows = ledgerRepository.updateDisburseStatus(id,
                SubsidyLedgerRecord.DISBURSE_DONE, operator.id());
        if (rows == 0) {
            throw new BusinessException(50000, "发放确认失败，请重试");
        }
    }

    @Override
    public void batchConfirmDisburse(List<Long> ids, CurrentUser operator) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(40001, "请选择需要确认发放的记录");
        }
        int rows = ledgerRepository.batchUpdateDisburseStatus(ids,
                SubsidyLedgerRecord.DISBURSE_DONE, operator.id());
        if (rows != ids.size()) {
            throw new BusinessException(50000, "批量发放部分失败，已成功发放 " + rows + " 条，请检查后重试");
        }
    }

    /* ======================== 导出 ======================== */

    @Override
    public List<LedgerView> getExportData(Long batchId, Integer disburseStatus, Long collegeId) {
        return ledgerRepository.findAllForExport(batchId, disburseStatus, collegeId);
    }

    /* ======================== 按申请ID查询发放状态 ======================== */

    @Override
    public LedgerDisburseStatusResponse getDisburseStatusByApplyId(long applyId) {
        var record = ledgerRepository.findByApplyId(applyId);
        if (record.isEmpty()) {
            return null;
        }
        var r = record.get();
        String statusName = switch (r.disburseStatus()) {
            case SubsidyLedgerRecord.DISBURSE_PENDING -> "待发放";
            case SubsidyLedgerRecord.DISBURSE_DONE -> "已发放";
            case SubsidyLedgerRecord.DISBURSE_FAILED -> "发放失败";
            default -> "未知";
        };
        return new LedgerDisburseStatusResponse(
                r.applyId(), r.disburseStatus(), statusName,
                r.disburseTime(), r.approvedAmount());
    }
}
