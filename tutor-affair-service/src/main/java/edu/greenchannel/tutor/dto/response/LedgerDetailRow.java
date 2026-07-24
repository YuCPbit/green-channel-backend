package edu.greenchannel.tutor.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 台账明细行（与汇总口径一致的逐条记录）
 */
public record LedgerDetailRow(
        Long id,
        String applyNo,
        String title,
        String typeName,
        String tutorName,
        String collegeName,
        BigDecimal amount,
        Integer disburseStatus,
        LocalDateTime disburseTime,
        LocalDateTime submitTime,
        Integer urgency
) {}
