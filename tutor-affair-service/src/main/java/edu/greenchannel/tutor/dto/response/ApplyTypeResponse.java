package edu.greenchannel.tutor.dto.response;

/**
 * 申请类型响应
 */
public record ApplyTypeResponse(
        Long id,
        String typeName,
        String typeCode,
        String description,
        Integer needAmount,
        Integer needStudent,
        Integer approvalLevel,
        String formTemplate,
        Integer sort,
        Integer status
) {}
