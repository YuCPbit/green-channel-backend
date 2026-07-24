package edu.greenchannel.tutor.dto.response;

import java.math.BigDecimal;

/**
 * 台账汇总行（按学院 + 申请类型分组）
 */
public record LedgerSummaryRow(
        Long collegeId,
        String collegeName,
        Long typeId,
        String typeName,
        long totalCount,
        BigDecimal totalAmount,
        long disbursedCount,
        BigDecimal disbursedAmount,
        long pendingCount,
        BigDecimal pendingAmount
) {}
