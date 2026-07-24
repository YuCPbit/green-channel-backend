package edu.greenchannel.subsidy.dto.response;

import java.math.BigDecimal;

/**
 * 台账汇总统计响应。
 */
public record LedgerSummaryResponse(
        BigDecimal totalAmount,     // 总金额
        long totalCount,            // 总笔数
        long pendingCount,          // 待发放笔数
        BigDecimal pendingAmount,   // 待发放金额
        long doneCount,             // 已发放笔数
        BigDecimal doneAmount,      // 已发放金额
        long failedCount            // 发放失败笔数
) {}
