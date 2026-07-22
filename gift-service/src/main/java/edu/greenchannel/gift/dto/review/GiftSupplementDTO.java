package edu.greenchannel.gift.dto.review;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GiftSupplementDTO {
    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotNull(message = "批次ID不能为空")
    private Long packBatchId;

    // 申请唯一编号，不传则自动生成
    private String applyNo;

    private String applyReason;

    // 领取码，补录时可预置
    private String pickupCode;

    // 补录操作人ID
    @NotNull(message = "补录操作人不能为空")
    private Long operatorId;

    // 补录备注说明
    private String remark;

    // 补录的申请提交时间（历史时间）
    private LocalDateTime applyTime;
}
