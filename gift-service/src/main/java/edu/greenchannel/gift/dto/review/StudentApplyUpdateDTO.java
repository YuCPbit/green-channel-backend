package edu.greenchannel.gift.dto.review;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentApplyUpdateDTO {
    @NotNull(message = "申请单ID不能为空")
    private Long id;

    // 业务字段
    private String applyReason;
}
