package edu.dashboard.common;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class FieldWhiteList {

    // ==================== 1. 模块与字段的归属关系 ====================
    private static final Map<String, Set<String>> MODULE_FIELD_MAP = new HashMap<>();

    // ==================== 2. 字段编码 -> 数据库列名 (防SQL注入) ====================
    private static final Map<String, String> FIELD_COLUMN_MAP = new HashMap<>();

    // ==================== 3. 字段编码 -> 中文名称 (Excel表头) ====================
    private static final Map<String, String> FIELD_CHINESE_NAME_MAP = new LinkedHashMap<>();

    // ==================== 4. 报表类型 -> 默认字段列表 ====================
    private static final Map<String, List<String>> DEFAULT_REPORT_FIELDS = new HashMap<>();

    static {
        // -------------------- 基础模块 (base) --------------------
        /*
        registerModuleFields("base",
                List.of("student_no", "name", "gender", "college_name", "major_name",
                        "class_name", "poverty_level", "phone", "apply_amount", "approved_amount"),
                Map.ofEntries(
                        Map.entry("student_no", "s.student_no"),
                        Map.entry("name", "s.name"),
                        Map.entry("gender", "s.gender"),
                        Map.entry("college_name", "c.college_name"),
                        Map.entry("major_name", "m.major_name"),
                        Map.entry("class_name", "cl.class_name"),
                        Map.entry("poverty_level", "s.poverty_level"),
                        Map.entry("phone", "s.phone"),
                        Map.entry("apply_amount", "sa.apply_amount"),
                        Map.entry("approved_amount", "sa.approved_amount")
                ),
                Map.ofEntries(
                        Map.entry("student_no", "学号"),
                        Map.entry("name", "姓名"),
                        Map.entry("gender", "性别"),
                        Map.entry("college_name", "学院"),
                        Map.entry("major_name", "专业"),
                        Map.entry("class_name", "班级"),
                        Map.entry("poverty_level", "贫困等级"),
                        Map.entry("phone", "联系电话"),
                        Map.entry("apply_amount", "申请金额"),
                        Map.entry("approved_amount", "批准金额")
                )
        );

        DEFAULT_REPORT_FIELDS.put("base_student-list",
                Arrays.asList("student_no", "name", "college_name", "poverty_level"));
        DEFAULT_REPORT_FIELDS.put("base_subsidy-list",
                Arrays.asList("student_no", "name", "apply_amount", "approved_amount"));
        */
// ==================== 勤工助学模块 (workstudy) ====================
        // 只调用一次 registerModuleFields
        registerModuleFields("workstudy",
                // 1. 字段编码全集（包含明细字段和统计字段）
                List.of(
                        // 明细报表字段 (ws_ 前缀)
                        "ws_department", "ws_position_name", "ws_student_name",
                        "ws_college_name", "ws_poverty_level", "ws_work_hours", "ws_salary",
                        // 统计报表字段 (无前缀，或业务名)
                        "dept_name", "position_count", "plan_recruit_count", "apply_count",
                        "hire_count", "on_job_count", "on_job_rate",
                        "college_name", "poverty_level_text", "student_count",
                        "salary_year", "salary_month", "paid_student_count", "total_salary_paid"
                ),
                // 2. 字段编码 -> 数据库列名/表达式 映射
                Map.ofEntries(
                        // 明细映射
                        Map.entry("ws_department", "p.department_name"),
                        Map.entry("ws_position_name", "p.position_name"),
                        Map.entry("ws_student_name", "s.name"),
                        Map.entry("ws_college_name", "c.college_name"),
                        Map.entry("ws_poverty_level", "s.poverty_level"),
                        Map.entry("ws_work_hours", "COALESCE(att.total_hours, 0)"),
                        Map.entry("ws_salary", "COALESCE(sal.total_amount, 0)"),

                        // 岗位统计映射
                        Map.entry("dept_name", "p.department_name"),
                        Map.entry("position_count", "COUNT(DISTINCT p.id)"),
                        Map.entry("plan_recruit_count", "SUM(p.recruit_count)"),
                        Map.entry("apply_count", "COUNT(DISTINCT app.id)"),
                        Map.entry("hire_count", "COUNT(DISTINCT h.id)"),
                        Map.entry("on_job_count", "COUNT(DISTINCT CASE WHEN h.hire_status = 1 THEN h.id END)"),
                        Map.entry("on_job_rate", "CASE WHEN SUM(p.recruit_count) > 0 THEN ROUND(COUNT(DISTINCT CASE WHEN h.hire_status = 1 THEN h.id END) * 100.0 / SUM(p.recruit_count), 2) ELSE 0 END"),

                        // 学生统计映射
                        Map.entry("college_name", "c.college_name"),
                        Map.entry("poverty_level_text", "CASE s.poverty_level WHEN 1 THEN '特别困难' WHEN 2 THEN '困难' WHEN 3 THEN '一般困难' WHEN 4 THEN '不困难' ELSE '未知' END"),
                        Map.entry("student_count", "COUNT(DISTINCT h.student_id)"),

                        // 薪酬统计映射
                        Map.entry("salary_year", "sal.salary_year"),
                        Map.entry("salary_month", "sal.salary_month"),
                        Map.entry("paid_student_count", "COUNT(DISTINCT sal.student_id)"),
                        Map.entry("total_salary_paid", "SUM(sal.final_amount)")
                ),
                // 3. 字段编码 -> 中文名称 映射
                Map.ofEntries(
                        // 明细中文名
                        Map.entry("ws_department", "用工部门"), Map.entry("ws_position_name", "岗位名称"),
                        Map.entry("ws_student_name", "学生姓名"), Map.entry("ws_college_name", "所属学院"),
                        Map.entry("ws_poverty_level", "贫困等级"), Map.entry("ws_work_hours", "工时"),
                        Map.entry("ws_salary", "实发薪酬"),

                        // 岗位统计中文名
                        Map.entry("dept_name", "用工部门"), Map.entry("position_count", "岗位数"),
                        Map.entry("plan_recruit_count", "计划招聘"), Map.entry("apply_count", "报名人数"),
                        Map.entry("hire_count", "录用人数"), Map.entry("on_job_count", "在岗人数"),
                        Map.entry("on_job_rate", "在岗率(%)"),

                        // 学生统计中文名
                        Map.entry("college_name", "所属学院"), Map.entry("poverty_level_text", "贫困等级"),
                        Map.entry("student_count", "参与人数"),

                        // 薪酬统计中文名
                        Map.entry("salary_year", "年度"), Map.entry("salary_month", "月份"),
                        Map.entry("paid_student_count", "发放人数"), Map.entry("total_salary_paid", "发放总额(元)")
                )
        );

        // 4. 默认报表字段配置
        DEFAULT_REPORT_FIELDS.put("workstudy_position-detail",
                Arrays.asList("ws_department", "ws_position_name", "ws_student_name", "ws_salary"));
        DEFAULT_REPORT_FIELDS.put("workstudy_position-stat",
                Arrays.asList("dept_name", "position_count", "plan_recruit_count", "apply_count", "hire_count", "on_job_count", "on_job_rate"));
        DEFAULT_REPORT_FIELDS.put("workstudy_student-stat",
                Arrays.asList("college_name", "poverty_level_text", "student_count"));
        DEFAULT_REPORT_FIELDS.put("workstudy_salary-stat",
                Arrays.asList("salary_year", "salary_month", "paid_student_count", "total_salary_paid"));
    }

    /**
     * 注册模块字段的辅助方法
     */
    private static void registerModuleFields(String module, List<String> fields,
                                             Map<String, String> columnMap, Map<String, String> chineseNameMap) {
        MODULE_FIELD_MAP.put(module, new HashSet<>(fields));
        FIELD_COLUMN_MAP.putAll(columnMap);
        FIELD_CHINESE_NAME_MAP.putAll(chineseNameMap);
    }

    /**
     * 验证字段合法性，并返回安全的SQL列名片段
     */
    public String getSafeColumns(String moduleReportKey, List<String> requestedFields) {
        String[] parts = moduleReportKey.split("_");
        String module = parts[0];
        String reportType = parts.length > 1 ? parts[1] : "default";

        Set<String> allowedFields = MODULE_FIELD_MAP.get(module);
        Assert.notNull(allowedFields, "不支持的模块编码: " + module);

        List<String> fieldsToUse = (requestedFields == null || requestedFields.isEmpty())
                ? DEFAULT_REPORT_FIELDS.getOrDefault(moduleReportKey, Collections.emptyList())
                : requestedFields;

        Assert.notEmpty(fieldsToUse, "字段列表不能为空");

        return fieldsToUse.stream().map(field -> {
            Assert.isTrue(allowedFields.contains(field), "非法字段: " + field);
            String column = FIELD_COLUMN_MAP.get(field);

            // 正确处理表别名
            String safeColumn;
            if (column == null) {
                throw new IllegalArgumentException("字段未配置映射: " + field);
            }

            // 处理聚合函数或表达式（如 COALESCE）
            if (column.contains("(") || column.contains(" ") || column.contains("CASE")) {
                safeColumn = column; // 表达式不加反引号
            } else if (column.contains(".")) {
                // 带表别名：p.department_name → `p`.`department_name`
                String[] partsOfColumn = column.split("\\.");
                safeColumn = "`" + partsOfColumn[0] + "`.`" + partsOfColumn[1] + "`";
            } else {
                // 不带表别名：`department_name`
                safeColumn = "`" + column + "`";
            }

            String chineseName = FIELD_CHINESE_NAME_MAP.get(field);
            return safeColumn + " AS `" + field + "`";
        }).collect(Collectors.joining(", "));
    }

    /**
     * 获取字段对应的中文名称
     */
    public String getChineseHeader(String fieldCode) {
        return FIELD_CHINESE_NAME_MAP.getOrDefault(fieldCode, fieldCode);
    }

    /**
     * 获取指定报表类型的默认字段列表
     */
    public List<String> getDefaultFields(String module, String reportType) {
        String key = module + "_" + reportType;
        return DEFAULT_REPORT_FIELDS.getOrDefault(key, Collections.emptyList());
    }

    /**
     * 获取数据库列名（不带反引号）
     */
    public String getColumnName(String fieldCode) {
        return FIELD_COLUMN_MAP.get(fieldCode);
    }
}