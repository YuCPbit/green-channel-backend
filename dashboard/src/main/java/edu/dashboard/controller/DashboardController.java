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
     * FR-3.14-002: 核心指标接口
     */
    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats() {
        return Result.success(dashboardService.getCachedStats());
    }

    /**
     * FR-3.14-003: 学院对比
     */
    @GetMapping("/college-compare")
    public Result<List<CollegeCompareVO>> getCollegeCompare() {
        return Result.success(dashboardService.getCollegeComparison());
    }

    /**
     * FR-3.14-003: 生源地热力图
     */
    @GetMapping("/heatmap")
    public Result<List<HeatmapVO>> getHeatmap() {
        return Result.success(dashboardService.getOriginHeatmapData());
    }

    /**
     * FR-3.14-003: 补助结构
     */
    @GetMapping("/subsidy-structure")
    public Result<List<SubsidyStructureVO>> getSubsidyStructure() {
        return Result.success(dashboardService.getSubsidyStructure());
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
}