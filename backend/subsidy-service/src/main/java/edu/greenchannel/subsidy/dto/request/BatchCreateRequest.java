package edu.greenchannel.subsidy.dto.request;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record BatchCreateRequest(
        String batchName,
        String academicYear,
        Integer subsidyType,
        BigDecimal totalAmount,
        LocalDateTime applyStartTime,
        LocalDateTime applyEndTime,
        LocalDateTime collegeSubmitEndTime
) {}
