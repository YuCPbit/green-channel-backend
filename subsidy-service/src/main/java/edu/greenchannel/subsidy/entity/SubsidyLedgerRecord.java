package edu.greenchannel.subsidy.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 补助发放台账实体记录，映射 gc_subsidy_ledger 表。
 * 使用 JDBC Record 模式，与 SubsidyApplyRecord 保持一致。
 */
public record SubsidyLedgerRecord(
        long id,
        long batchId,
        long applyId,
        long studentId,
        String applyNo,
        int subsidyType,
        BigDecimal approvedAmount,
        int disburseStatus,
        LocalDateTime disburseTime,
        Long disburseOperatorId,
        String bankCardNo,
        String remark,
        LocalDateTime createTime
) {
    /** 待发放 */
    public static final int DISBURSE_PENDING = 0;
    /** 已发放 */
    public static final int DISBURSE_DONE = 1;
    /** 发放失败 */
    public static final int DISBURSE_FAILED = 2;
}
