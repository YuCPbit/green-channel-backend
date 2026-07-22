package edu.greenchannel.message;

import edu.greenchannel.common.BusinessException;

import java.util.Arrays;

public enum MessageEventType {
    GIFT_APPLY_SUBMITTED,
    GIFT_REVIEW_RETURNED,
    GIFT_REVIEW_PASSED,
    SUBSIDY_APPLY_SUBMITTED,
    SUBSIDY_REVIEW_RETURNED,
    SUBSIDY_REVIEW_PASSED,
    PAYMENT_STATUS_CHANGED,
    WORK_STUDY_HIRED,
    WORK_STUDY_AGREEMENT_PENDING;

    public static MessageEventType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equals(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(40001, "不支持的消息事件"));
    }
}
