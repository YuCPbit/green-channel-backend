package edu.greenchannel.subsidy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Read model for displaying subsidy applications in lists and detail views.
 */
public record SubsidyApplyView(
        long id,
        long batchId,
        String batchName,
        long studentId,
        String studentName,
        String studentNo,
        Long collegeId,
        String collegeName,
        Integer grade,
        int applicantType,
        String applyNo,
        int subsidyType,
        BigDecimal applyAmount,
        BigDecimal approvedAmount,
        String applyReason,
        int status,
        LocalDateTime applyTime,
        List<SubsidyReviewView> reviews
) {
}
