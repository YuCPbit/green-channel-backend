package edu.greenchannel.dashboard.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CustomReportReqDTO {
    @NotEmpty(message = "报表字段不能为空")
    private List<String> fields; // 前端勾选的字段列表
    private Map<String, Object> filters; // 筛选条件，如 collegeId: 1
}
