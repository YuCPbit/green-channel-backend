package edu.greenchannel.gift.dto.review;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class BatchSubmitDTO {
    @NotEmpty(message = "请选择需要提交的申请单")
    private List<Long> applyIdList;
}