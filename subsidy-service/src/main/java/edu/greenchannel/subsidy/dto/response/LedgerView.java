package edu.greenchannel.subsidy.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 台账列表视图，包含联表查询的学生、学院、批次、操作人信息。
 */
public record LedgerView(
        long id,
        long batchId,
        String batchName,
        long applyId,
        String applyNo,
        long studentId,
        String studentName,
        String studentNo,
        Long collegeId,
        String collegeName,
        Integer grade,
        int subsidyType,
        String subsidyTypeName,
        BigDecimal approvedAmount,
        int disburseStatus,
        String disburseStatusName,
        LocalDateTime disburseTime,
        String disburseOperatorName,
        String bankCardNo,
        String remark,
        LocalDateTime createTime
) {}
