package edu.greenchannel.subsidy.dto.request;

import java.math.BigDecimal;

/**
 * Request DTO for submitting a review on a subsidy application.
 */
public record SubsidyReviewRequest(
        long applyId,
        int action,
        String comment,
        BigDecimal suggestAmount
) {
}
