package edu.greenchannel.gift.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PickupRequest(
        @NotBlank(message = "领取码不能为空") String pickupCode,
        @NotNull(message = "核销工作人员ID不能为空") Long operatorId,
        @Size(max = 500, message = "领取备注不能超过500字") String remark
) {
}
