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

    private static final Map<String, String> WORK_STUDY_REPORT_FIELDS = Map.ofEntries(
            // 岗位维度字段
            Map.entry("ws_department", "p.department_name"),
            Map.entry("ws_position_name", "p.position_name"),
            Map.entry("ws_recruit_count", "p.recruit_count"),
            Map.entry("ws_apply_count", "COUNT(DISTINCT a.id)"),
            Map.entry("ws_hired_count", "COUNT(DISTINCT h.id)"),
            Map.entry("ws_online_rate", "ROUND(COUNT(DISTINCT h.id) * 100.0 / p.recruit_count, 2)"),
            // 学生维度字段
            Map.entry("ws_student_name", "s.name"),
            Map.entry("ws_college_name", "c.college_name"),
            Map.entry("ws_poverty_level", "s.poverty_level"),
            Map.entry("ws_total_work_hours", "SUM(at.work_hours)"),
            // 薪酬维度字段
            Map.entry("ws_salary_month", "sal.salary_month"),
            Map.entry("ws_total_salary", "SUM(sal.final_amount)"),
            Map.entry("ws_budget_amount", "p.recruit_count * p.salary_rate * 22 * 8"), // 22天法定工作日
            Map.entry("ws_budget_exec_rate", "ROUND(SUM(sal.final_amount) * 100.0 / (p.recruit_count * p.salary_rate * 22 * 8), 2)")
    );

    // 合并所有白名单（新增方法）
    public List<String> validateAndGetWorkStudyFields(List<String> requestedFields) {
        return requestedFields.stream()
                .map(WORK_STUDY_REPORT_FIELDS::get)
                .peek(col -> Assert.notNull(col, "包含非法勤工助学字段，请检查字段编码"))
                .collect(Collectors.toList());
    }

    public List<String> validateAndGetColumns(List<String> requestedFields) {
        Assert.notEmpty(requestedFields, "字段列表不能为空");
        return requestedFields.stream()
                .map(STUDENT_REPORT_FIELDS::get)
                .peek(col -> Assert.notNull(col, "包含非法字段，请检查字段编码"))
                .collect(Collectors.toList());
    }
}