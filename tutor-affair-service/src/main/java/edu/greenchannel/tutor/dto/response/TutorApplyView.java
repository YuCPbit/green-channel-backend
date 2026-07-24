package edu.greenchannel.tutor.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 申请详情视图（含审核记录和关联学生）
 */
public record TutorApplyView(
        Long id,
        String applyNo,
        Long typeId,
        String typeName,
        Long tutorId,
        String tutorName,
        String title,
        String description,
        BigDecimal amount,
        Integer urgency,
        Integer status,
        String formData,
        String remark,
        LocalDateTime applyTime,
        LocalDateTime submitTime,
        Integer disburseStatus,
        LocalDateTime disburseTime,
        List<TutorReviewView> reviews,
        List<StudentBrief> students
) {}
