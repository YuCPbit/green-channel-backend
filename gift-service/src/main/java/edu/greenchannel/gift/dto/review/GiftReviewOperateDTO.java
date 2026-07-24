package edu.greenchannel.gift.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

@Data
public class GiftReviewOperateDTO {
    @NotNull(message = "申请ID不能为空")
    private Long applyId;

    @NotNull(message = "审批操作类型不能为空")
    private Integer action;

    @NotBlank(message = "审核意见不能为空")
    private String comment;

    // 修改申请时传递更新字段
    private Map<String, Object> modifyData;
}