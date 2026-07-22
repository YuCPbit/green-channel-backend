package edu.greenchannel.subsidy.dto.response;
import java.math.BigDecimal;
public record AllocationItemResponse(
        Long id,
        Long batchId,
        Integer targetType,
        Long targetId,
        Long collegeId,
        Integer grade,
        BigDecimal amount,
        BigDecimal usedAmount
) {}
