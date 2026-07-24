package edu.greenchannel.tutor.dto.request;

import java.math.BigDecimal;
import java.util.List;

/**
 * 辅导员发起/修改事务申请请求
 */
public record TutorApplyRequest(
        Long typeId,
        String title,
        String description,
        BigDecimal amount,
        Integer urgency,
        List<Long> studentIds,
        String formData
) {}
