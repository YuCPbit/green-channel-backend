package edu.greenchannel.subsidy.dto.response;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record BatchResponse(
        Long id,
        String batchName,
        String academicYear,
        Integer subsidyType,
        BigDecimal totalAmount,
        LocalDateTime applyStartTime,
        LocalDateTime applyEndTime,
        LocalDateTime collegeSubmitEndTime,
        Integer status,
        LocalDateTime createTime
) {}
