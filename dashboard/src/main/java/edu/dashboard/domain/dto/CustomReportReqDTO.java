package edu.dashboard.domain.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 自定义报表请求参数（最终版）
 * 用于接收前端发起的报表预览/导出请求
 */
@Data
public class CustomReportReqDTO {

    /**
     * 模块编码（必填）
     * 对应 ModuleDataService.getModuleCode()
     * 示例：base, workstudy, scholarship
     */
    private String module;

    /**
     * 报表类型（必填）
     * 由具体的业务模块定义其含义
     * 示例（workstudy）: position-detail, salary-detail
     * 示例（base）: student-list, subsidy-list
     */
    private String reportType;

    /**
     * 导出文件名/模板名称（必填）
     * 用于Excel下载时的文件名
     */
    private String templateName;

    /**
     * 勾选的字段列表（可选）
     * 如果不传，则使用 FieldWhiteList 中定义的默认字段
     */
    private List<String> fields;

    /**
     * 筛选条件（可选）
     * 前端传入的查询条件，将透传给具体的 ModuleDataService
     * 示例：{"collegeId": 1, "startDate": "2024-01-01", "status": 2}
     */
    private Map<String, Object> filters;

    /**
     * 排序字段（可选）
     */
    private String orderBy;

    /**
     * 排序方向（可选，默认 desc）
     * asc: 升序, desc: 降序
     */
    private String orderDir = "desc";

    /**
     * 页码（可选，用于预览分页）
     */
    private Integer pageNum = 1;

    /**
     * 每页条数（可选，用于预览分页）
     */
    private Integer pageSize = 10;
}