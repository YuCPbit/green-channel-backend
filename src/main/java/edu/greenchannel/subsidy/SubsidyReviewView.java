package edu.greenchannel.subsidy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Read model for a single review record, suitable for timeline display.
 */
public record SubsidyReviewView(
        long id,
        long applyId,
        long reviewerId,
        String reviewerName,
        int reviewerRole,
        String reviewerRoleName,
        int action,
        String actionName,
        String comment,
        BigDecimal suggestAmount,
        LocalDateTime reviewTime
) {
}
