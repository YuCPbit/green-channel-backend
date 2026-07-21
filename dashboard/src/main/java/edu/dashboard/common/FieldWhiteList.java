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

        // -------------------- 勤工助学模块 (workstudy) --------------------
        registerModuleFields("workstudy",
                List.of("ws_department", "ws_position_name", "ws_student_name", "ws_college_name",
                        "ws_poverty_level", "ws_work_hours", "ws_salary"),
                Map.ofEntries(
                        Map.entry("ws_department", "d.dept_name"),
                        Map.entry("ws_position_name", "p.position_name"),
                        Map.entry("ws_student_name", "s.name"),
                        Map.entry("ws_college_name", "c.college_name"),
                        Map.entry("ws_poverty_level", "s.poverty_level"),
                        Map.entry("ws_work_hours", "att.hours"),
                        Map.entry("ws_salary", "sal.amount")
                ),
                Map.ofEntries(
                        Map.entry("ws_department", "用工部门"),
                        Map.entry("ws_position_name", "岗位名称"),
                        Map.entry("ws_student_name", "学生姓名"),
                        Map.entry("ws_college_name", "所属学院"),
                        Map.entry("ws_poverty_level", "贫困等级"),
                        Map.entry("ws_work_hours", "工时"),
                        Map.entry("ws_salary", "实发薪酬")
                )
        );

        DEFAULT_REPORT_FIELDS.put("workstudy_position-detail",
                Arrays.asList("ws_department", "ws_position_name", "ws_student_name", "ws_salary"));
        DEFAULT_REPORT_FIELDS.put("workstudy_salary-detail",
                Arrays.asList("ws_student_name", "ws_college_name", "ws_work_hours", "ws_salary"));
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
            String chineseName = FIELD_CHINESE_NAME_MAP.get(field);
            // 使用反引号包裹列名，防止关键字冲突，同时使用AS别名保持一致性
            return "`" + column + "` AS `" + field + "`";
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