package edu.greenchannel.tutorapply;

public record TutorApplyTypeRequest(
        String typeName,
        String typeCode,
        String description,
        Boolean needAmount,
        Boolean needStudent,
        Integer approvalLevel,
        Object formTemplate,
        Integer sort,
        Boolean enabled) {
}
