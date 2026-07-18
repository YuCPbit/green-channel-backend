package edu.greenchannel.subsidy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity record mapping gc_subsidy_review table.
 */
public record SubsidyReviewRecord(
        long id,
        long applyId,
        long reviewerId,
        int reviewerRole,
        int action,
        String comment,
        BigDecimal suggestAmount,
        LocalDateTime reviewTime
) {
    /** 辅导员 */
    public static final int ROLE_TUTOR = 1;
    /** 学院管理员 */
    public static final int ROLE_COLLEGE = 2;
    /** 学校资助中心 */
    public static final int ROLE_SCHOOL = 3;

    /** 通过 */
    public static final int ACTION_PASS = 1;
    /** 驳回修改 */
    public static final int ACTION_RETURN = 2;
    /** 不通过 */
    public static final int ACTION_REJECT = 3;
}
