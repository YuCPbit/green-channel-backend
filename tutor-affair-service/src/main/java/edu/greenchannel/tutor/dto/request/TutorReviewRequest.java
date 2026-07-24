package edu.greenchannel.tutor.dto.request;

/**
 * 审核请求
 */
public record TutorReviewRequest(
        Long applicationId,
        Integer action,
        String comment
) {}
