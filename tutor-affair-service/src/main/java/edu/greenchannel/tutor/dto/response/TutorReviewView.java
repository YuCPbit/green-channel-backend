package edu.greenchannel.tutor.dto.response;

import java.time.LocalDateTime;

/**
 * 审核记录视图
 */
public record TutorReviewView(
        Long id,
        Long reviewerId,
        String reviewerName,
        Integer reviewerRole,
        Integer action,
        String comment,
        LocalDateTime reviewTime
) {}
