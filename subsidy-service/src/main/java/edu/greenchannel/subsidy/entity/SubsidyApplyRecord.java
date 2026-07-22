package edu.greenchannel.subsidy.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity record mapping gc_subsidy_apply table.
 */
public record SubsidyApplyRecord(
        long id,
        long batchId,
        long studentId,
        int applicantType,
        long applicantUserId,
        String applyNo,
        int subsidyType,
        BigDecimal applyAmount,
        BigDecimal approvedAmount,
        String applyReason,
        int status,
        LocalDateTime applyTime
) {
    /** Student self-apply */
    public static final int APPLICANT_TYPE_STUDENT = 1;
    /** Tutor proxy apply on behalf of student */
    public static final int APPLICANT_TYPE_TUTOR = 2;

    /** 待辅导员审核 */
    public static final int STATUS_PENDING_TUTOR = 1;
    /** 待学院审核 */
    public static final int STATUS_PENDING_COLLEGE = 2;
    /** 待学校审核 */
    public static final int STATUS_PENDING_SCHOOL = 3;
    /** 已通过 */
    public static final int STATUS_APPROVED = 4;
    /** 已驳回（可修改重提） */
    public static final int STATUS_RETURNED = 5;
    /** 不通过（终态） */
    public static final int STATUS_REJECTED = 6;
}
