package edu.dashboard.domain.dto;

import edu.dashboard.common.FieldWhiteList;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CustomReportReqDTO {
    /**
     * 模块标识
     * basic: 基础模块（绿色通道、大礼包）
     * workstudy: 勤工助学模块
     */
    private String module = "basic";

    /**
     * 报表类型（模块内细分）
     * basic模块: student（学生信息）、subsidy（补助信息）
     * workstudy模块: position（岗位统计）、student-college（按学院统计）、
     *               student-poverty（按贫困等级统计）、salary-monthly（月度薪酬）、salary-term（学期薪酬）
     */
    private String reportType;

    /**
     * 字段列表（前端勾选的字段编码）
     * 为空则使用默认字段
     */
    private List<String> fields;

    /**
     * 筛选条件
     * 如：collegeId=1, status=2, startDate=2024-01-01
     */
    private Map<String, Object> filters;

    /**
     * 模板名称（用于保存模板和导出文件名）
     */
    private String templateName;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方向（asc/desc）
     */
    private String orderDir = "desc";

    /**
     * 获取实际字段列表（使用默认或自定义）
     */
    public List<String> getActualFields(FieldWhiteList fieldWhiteList) {
        if (fields == null || fields.isEmpty()) {
            return fieldWhiteList.getDefaultFields(module, reportType);
        }
        return fields;
    }
}