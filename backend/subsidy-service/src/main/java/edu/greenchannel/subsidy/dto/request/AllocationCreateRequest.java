package edu.greenchannel.subsidy.dto.request;
import java.math.BigDecimal;
public record AllocationCreateRequest(
        Long batchId,
        Integer targetType, // 1-学校发学院，2-学院发年级
        Long targetId,      // 学院ID 或 年级ID
        BigDecimal amount
) {}