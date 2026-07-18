package edu.greenchannel.subsidy;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating or updating a subsidy application.
 * <p>
 * {@code studentId} is only used when a tutor applies on behalf of a student.
 */
public record SubsidyApplyRequest(
        Long batchId,
        Long studentId,
        BigDecimal applyAmount,
        String applyReason,
        List<Long> attachmentIds
) {
}
