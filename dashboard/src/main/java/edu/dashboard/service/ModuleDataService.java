package edu.dashboard.service;

import java.util.List;
import java.util.Map;

/**
 * 所有业务模块的顶层接口（契约）
 * 新增任何模块，都必须实现此接口
 */
public interface ModuleDataService {

    /**
     * 模块唯一标识（如：base, workstudy, scholarship）
     */
    String getModuleCode();

    /**
     * 获取核心指标（用于大屏顶部数字展示）
     */
    Object getCoreStats();

    /**
     * 获取图表数据（用于ECharts等可视化组件）
     * @param chartType 图表类型标识（如：position-stats, pie-chart）
     */
    Object getChartData(String chartType);

    /**
     * 获取报表原始数据（用于Excel/PDF导出）
     * @param reportType 报表类型标识
     * @param filters 筛选条件
     */
    List<Map<String, Object>> getReportData(String reportType, Map<String, Object> filters);
}