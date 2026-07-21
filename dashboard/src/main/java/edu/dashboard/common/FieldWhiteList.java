package edu.dashboard.common;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FieldWhiteList {

    // FR-3.16-001: 字段超市的白名单映射
    // Key: 前端传的字段名, Value: 数据库真实的列名 (防止SQL注入)
    private static final Map<String, String> STUDENT_REPORT_FIELDS = Map.ofEntries(
            Map.entry("student_no", "s.student_no"),
            Map.entry("name", "s.name"),
            Map.entry("gender", "s.gender"),
            Map.entry("nation", "s.nation"),
            Map.entry("poverty_level", "s.poverty_level"),
            Map.entry("phone", "s.phone"),
            Map.entry("college_name", "c.college_name"),
            Map.entry("major_name", "m.major_name"),
            Map.entry("class_name", "cl.class_name"),
            Map.entry("apply_amount", "sa.apply_amount"),
            Map.entry("approved_amount", "sa.approved_amount"),
            Map.entry("subsidy_type", "sa.subsidy_type"),
            Map.entry("apply_status", "sa.status")
    );

    public List<String> validateAndGetColumns(List<String> requestedFields) {
        Assert.notEmpty(requestedFields, "字段列表不能为空");
        return requestedFields.stream()
                .map(STUDENT_REPORT_FIELDS::get)
                .peek(col -> Assert.notNull(col, "包含非法字段，请检查字段编码"))
                .collect(Collectors.toList());
    }
}