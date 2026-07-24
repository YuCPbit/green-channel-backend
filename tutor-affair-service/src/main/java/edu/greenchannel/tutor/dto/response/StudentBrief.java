package edu.greenchannel.tutor.dto.response;

/**
 * 学生简要信息
 */
public record StudentBrief(
        Long studentId,
        String studentName,
        String studentNo,
        Long collegeId,
        String collegeName,
        String className,
        Integer povertyLevel
) {}
