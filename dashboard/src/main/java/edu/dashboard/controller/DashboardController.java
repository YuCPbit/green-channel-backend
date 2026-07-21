package edu.dashboard.controller;

import edu.dashboard.common.Result;
import edu.dashboard.domain.vo.*;
import edu.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * FR-3.14-002: 统一核心指标接口
     * module: basic(基础), workstudy(勤工助学)
     */
    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats(
            @RequestParam(defaultValue = "basic") String module) {
        return Result.success(dashboardService.getStatsByModule(module));
    }

    /**
     * FR-3.14-003: 统一图表数据接口
     */
    @GetMapping("/chart")
    public Result<?> getChartData(
            @RequestParam String module,
            @RequestParam String type) {

        return switch (module) {
            case "basic" -> Result.success(getBasicChart(type));
            case "workstudy" -> Result.success(getWorkStudyChart(type));
            default -> Result.error(400, "未知模块");
        };
    }

    /**
     * FR-3.14-004: 年度趋势
     */
    @GetMapping("/yearly-trend")
    public Result<Map<String, Object>> getYearlyTrend(@RequestParam String currentYear) {
        Map<String, Object> result = new HashMap<>();
        result.put("trend", dashboardService.getYearlyTrend(currentYear));
        return Result.success(result);
    }

    // ========== 私有方法 ==========

    private Object getBasicChart(String type) {
        return switch (type) {
            case "college-compare" -> dashboardService.getCollegeComparison();
            case "heatmap" -> dashboardService.getOriginHeatmapData();
            case "subsidy-structure" -> dashboardService.getSubsidyStructure();
            default -> throw new IllegalArgumentException("未知图表类型");
        };
    }

    private Object getWorkStudyChart(String type) {
        return switch (type) {
            case "position-stats" -> dashboardService.getWsPositionStats();
            case "student-college-stats" -> dashboardService.getWsStudentByCollegeStats();
            case "student-poverty-stats" -> dashboardService.getWsStudentByPovertyStats();
            case "salary-monthly-stats" -> dashboardService.getWsSalaryMonthlyStats();
            case "salary-term-stats" -> dashboardService.getWsSalaryTermStats();
            default -> throw new IllegalArgumentException("未知图表类型");
        };
    }
}