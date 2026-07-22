package edu.greenchannel.dashboard.common;

import edu.greenchannel.common.BusinessException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class FieldWhiteList {

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

    private static final Map<String, List<String>> REPORT_FIELDS = Map.of(
            "workstudy_position-stat", List.of("dept_name", "position_count", "plan_recruit_count",
                    "apply_count", "hire_count", "on_job_count", "on_job_rate"),
            "workstudy_student-stat", List.of("college_name", "poverty_level_text", "student_count"),
            "workstudy_salary-stat", List.of("salary_year", "salary_month", "paid_student_count",
                    "total_salary_paid"),
            "workstudy_position-detail", List.of("ws_department", "ws_position_name", "ws_student_name",
                    "ws_college_name", "ws_poverty_level", "ws_work_hours", "ws_salary")
    );

    private static final Map<String, String> CHINESE_HEADERS = Map.ofEntries(
            Map.entry("dept_name", "用工部门"),
            Map.entry("position_count", "岗位数"),
            Map.entry("plan_recruit_count", "计划招聘人数"),
            Map.entry("apply_count", "报名人数"),
            Map.entry("hire_count", "录用人数"),
            Map.entry("on_job_count", "在岗人数"),
            Map.entry("on_job_rate", "在岗率(%)"),
            Map.entry("college_name", "所属学院"),
            Map.entry("poverty_level_text", "困难等级"),
            Map.entry("student_count", "参与人数"),
            Map.entry("salary_year", "年度"),
            Map.entry("salary_month", "月份"),
            Map.entry("paid_student_count", "发放人数"),
            Map.entry("total_salary_paid", "发放总额(元)"),
            Map.entry("ws_department", "用工部门"),
            Map.entry("ws_position_name", "岗位名称"),
            Map.entry("ws_student_name", "学生姓名"),
            Map.entry("ws_college_name", "所属学院"),
            Map.entry("ws_poverty_level", "困难等级"),
            Map.entry("ws_work_hours", "工时"),
            Map.entry("ws_salary", "实发薪酬")
    );

    public List<String> validateAndGetColumns(List<String> requestedFields) {
        if (requestedFields == null || requestedFields.isEmpty()) {
            throw new BusinessException(40000, "报表字段不能为空");
        }
        return requestedFields.stream().map(field -> {
            String column = STUDENT_REPORT_FIELDS.get(field);
            if (column == null) {
                throw new BusinessException(40000, "包含非法字段: " + field);
            }
            return column;
        }).toList();
    }

    public List<String> validateReportFields(String module, String reportType, List<String> requestedFields) {
        String key = module + "_" + reportType;
        List<String> allowed = REPORT_FIELDS.get(key);
        if (allowed == null) {
            throw new BusinessException(40000, "不支持的报表类型: " + key);
        }
        List<String> fields = requestedFields == null || requestedFields.isEmpty() ? allowed : requestedFields;
        if (!Set.copyOf(allowed).containsAll(fields)) {
            throw new BusinessException(40000, "报表包含未授权字段");
        }
        return List.copyOf(fields);
    }

    public List<Map<String, Object>> projectRows(List<Map<String, Object>> rows, List<String> fields) {
        return rows.stream().map(row -> {
            Map<String, Object> projected = new LinkedHashMap<>();
            fields.forEach(field -> projected.put(field, valueIgnoreCase(row, field)));
            return projected;
        }).toList();
    }

    public String getChineseHeader(String field) {
        return CHINESE_HEADERS.getOrDefault(field, field);
    }

    private Object valueIgnoreCase(Map<String, Object> row, String field) {
        if (row.containsKey(field)) {
            return row.get(field);
        }
        return row.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(field))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
