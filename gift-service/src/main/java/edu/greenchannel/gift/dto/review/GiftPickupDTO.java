package edu.greenchannel.gift.dto.review;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GiftPickupDTO {
    @NotBlank(message = "领取码不能为空")
    private String pickupCode;

    private Long operatorId;

    // 异常登记、补发时填写备注
    private String remark;
}
