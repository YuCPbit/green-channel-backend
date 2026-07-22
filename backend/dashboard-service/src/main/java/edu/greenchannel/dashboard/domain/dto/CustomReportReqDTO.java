package edu.greenchannel.dashboard.domain.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CustomReportReqDTO {
    private String module = "base";
    private String reportType = "student-list";
    private String templateName = "自定义报表";
    private List<String> fields;
    private Map<String, Object> filters;
}
