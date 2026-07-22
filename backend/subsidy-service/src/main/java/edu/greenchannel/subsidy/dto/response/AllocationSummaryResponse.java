package edu.greenchannel.subsidy.dto.response;
import java.math.BigDecimal;
public record AllocationSummaryResponse(
        BigDecimal totalAmount,       // 总额度
        BigDecimal allocatedAmount,   // 已分配额度
        BigDecimal availableAmount    // 可用余额
) {}