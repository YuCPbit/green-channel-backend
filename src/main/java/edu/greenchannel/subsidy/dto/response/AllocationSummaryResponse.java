package edu.greenchannel.subsidy.dto.response;
import java.math.BigDecimal;
public record AllocationSummaryResponse(
        BigDecimal totalAmount,       // 总额度（学校设定的总盘子，或学院从学校得到的总分配）
        BigDecimal allocatedAmount,   // 已分配额度（学校已分给学院，或学院已分给年级）
        BigDecimal availableAmount    // 可用余额
) {}