package edu.greenchannel.subsidy.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生端发放状态 DTO（仅展示必要信息）。
 */
public record LedgerDisburseStatusResponse(
        long applyId,
        int disburseStatus,
        String disburseStatusName,
        LocalDateTime disburseTime,
        BigDecimal approvedAmount
) {}
