package edu.greenchannel.tutorapply;

public record TutorApplyType(
        long id,
        String typeName,
        String typeCode,
        String description,
        boolean needAmount,
        boolean needStudent,
        int approvalLevel,
        Object formTemplate,
        int sort,
        boolean enabled) {
}
