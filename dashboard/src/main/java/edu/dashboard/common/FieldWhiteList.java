package edu.dashboard.common;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FieldWhiteList {

    // ========== 字段编码 -> 数据库列名 ==========
    private static final Map<String, String> BASIC_FIELDS = Map.ofEntries(
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
            Map.entry("apply_status", "sa.status"),
            Map.entry("apply_time", "sa.apply_time")
    );

    private static final Map<String, String> WORK_STUDY_FIELDS = Map.ofEntries(
            Map.entry("ws_department", "p.department_name"),
            Map.entry("ws_position_name", "p.position_name"),
            Map.entry("ws_recruit_count", "p.recruit_count"),
            Map.entry("ws_apply_count", "COUNT(DISTINCT a.id)"),
            Map.entry("ws_hired_count", "COUNT(DISTINCT h.id)"),
            Map.entry("ws_online_rate", "ROUND(COUNT(DISTINCT h.id) * 100.0 / p.recruit_count, 2)"),
            Map.entry("ws_student_name", "s.name"),
            Map.entry("ws_college_name", "c.college_name"),
            Map.entry("ws_poverty_level", "s.poverty_level"),
            Map.entry("ws_total_work_hours", "SUM(at.work_hours)"),
            Map.entry("ws_salary_month", "sal.salary_month"),
            Map.entry("ws_total_salary", "SUM(sal.final_amount)"),
            Map.entry("ws_budget_exec_rate", "ROUND(SUM(sal.final_amount) * 100.0 / (p.recruit_count * p.salary_rate * 22 * 8), 2)")
    );

    // ========== 字段编码 -> 中文名称 ==========
    public static final Map<String, String> FIELD_CHINESE_NAMES = Map.ofEntries(
            // 基础字段
            Map.entry("student_no", "学号"),
            Map.entry("name", "姓名"),
            Map.entry("gender", "性别"),
            Map.entry("nation", "民族"),
            Map.entry("poverty_level", "贫困等级"),
            Map.entry("phone", "联系电话"),
            Map.entry("college_name", "学院"),
            Map.entry("major_name", "专业"),
            Map.entry("class_name", "班级"),
            Map.entry("apply_amount", "申请金额"),
            Map.entry("approved_amount", "批准金额"),
            Map.entry("subsidy_type", "补助类型"),
            Map.entry("apply_status", "申请状态"),
            Map.entry("apply_time", "申请时间"),
            // 勤工助学字段
            Map.entry("ws_department", "用工部门"),
            Map.entry("ws_position_name", "岗位名称"),
            Map.entry("ws_recruit_count", "招聘人数"),
            Map.entry("ws_apply_count", "报名人次"),
            Map.entry("ws_hired_count", "在岗人数"),
            Map.entry("ws_online_rate", "在岗率(%)"),
            Map.entry("ws_student_name", "学生姓名"),
            Map.entry("ws_college_name", "所在学院"),
            Map.entry("ws_poverty_level", "贫困等级"),
            Map.entry("ws_total_work_hours", "总工时"),
            Map.entry("ws_salary_month", "发放月份"),
            Map.entry("ws_total_salary", "发放总额(元)"),
            Map.entry("ws_budget_exec_rate", "预算执行率(%)")
    );

    // ========== 报表类型 -> 默认字段列表 ==========
    public static final Map<String, List<String>> REPORT_DEFAULT_FIELDS = Map.of(
            // 基础模块
            "basic_student", Arrays.asList("student_no", "name", "college_name", "poverty_level", "apply_status"),
            "basic_subsidy", Arrays.asList("student_no", "name", "apply_amount", "approved_amount", "subsidy_type"),

            // 勤工助学模块
            "workstudy_position", Arrays.asList("ws_department", "ws_position_name", "ws_recruit_count", "ws_hired_count", "ws_online_rate"),
            "workstudy_student_college", Arrays.asList("ws_college_name", "ws_hired_count"),
            "workstudy_student_poverty", Arrays.asList("ws_poverty_level", "ws_hired_count"),
            "workstudy_salary_monthly", Arrays.asList("ws_salary_month", "ws_total_salary", "ws_budget_exec_rate"),
            "workstudy_salary_term", Arrays.asList("ws_academic_year", "ws_semester", "ws_total_salary", "ws_budget_exec_rate")
    );

    /**
     * 验证并返回安全的列名
     */
    public List<String> validateAndGetColumns(String module, List<String> requestedFields) {
        Assert.notEmpty(requestedFields, "字段列表不能为空");

        Map<String, String> fieldMap = getFieldMapByModule(module);

        return requestedFields.stream()
                .map(field -> {
                    String column = fieldMap.get(field);
                    Assert.notNull(column, "包含非法字段: " + field);
                    return column;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取字段中文名列表（用于Excel表头）
     */
    public List<String> getChineseHeaders(List<String> fieldCodes) {
        return fieldCodes.stream()
                .map(FIELD_CHINESE_NAMES::get)
                .collect(Collectors.toList());
    }

    /**
     * 根据模块获取字段映射
     */
    private Map<String, String> getFieldMapByModule(String module) {
        return switch (module) {
            case "basic" -> BASIC_FIELDS;
            case "workstudy" -> WORK_STUDY_FIELDS;
            default -> throw new IllegalArgumentException("未知模块: " + module);
        };
    }

    /**
     * 获取报表默认字段
     */
    public List<String> getDefaultFields(String module, String reportType) {
        String key = module + "_" + reportType;
        return REPORT_DEFAULT_FIELDS.getOrDefault(key, List.of());
    }
}