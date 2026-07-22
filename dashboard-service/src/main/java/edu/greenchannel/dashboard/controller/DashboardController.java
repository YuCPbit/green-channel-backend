package edu.greenchannel.dashboard.controller;

import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.dashboard.domain.vo.*;
import edu.greenchannel.dashboard.service.DashboardService;
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
@RequirePermission("school:dashboard:view")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * FR-3.14-002: 核心指标接口
     */
    @GetMapping("/stats")
    public ApiResponse<DashboardStatsVO> getStats() {
        return ApiResponse.success(dashboardService.getCachedStats());
    }

    /**
     * FR-3.14-003: 学院对比
     */
    @GetMapping("/college-compare")
    public ApiResponse<List<CollegeCompareVO>> getCollegeCompare() {
        return ApiResponse.success(dashboardService.getCollegeComparison());
    }

    /**
     * FR-3.14-003: 生源地热力图
     */
    @GetMapping("/heatmap")
    public ApiResponse<List<HeatmapVO>> getHeatmap() {
        return ApiResponse.success(dashboardService.getOriginHeatmapData());
    }

    /**
     * FR-3.14-003: 补助结构
     */
    @GetMapping("/subsidy-structure")
    public ApiResponse<List<SubsidyStructureVO>> getSubsidyStructure() {
        return ApiResponse.success(dashboardService.getSubsidyStructure());
    }

    /**
     * FR-3.14-004: 年度趋势
     */
    @GetMapping("/yearly-trend")
    public ApiResponse<Map<String, Object>> getYearlyTrend(@RequestParam String currentYear) {
        Map<String, Object> result = new HashMap<>();
        result.put("trend", dashboardService.getYearlyTrend(currentYear));
        return ApiResponse.success(result);
    }

    // ===== 勤工助学统计接口 =====
    /**
     * 岗位维度统计
     */
    @GetMapping("/ws/position-stats")
    public ApiResponse<List<Map<String, Object>>> getWsPositionStats() {
        return ApiResponse.success(dashboardService.getWsPositionStats());
    }

    /**
     * 学生维度统计（按学院）
     */
    @GetMapping("/ws/student-college-stats")
    public ApiResponse<List<Map<String, Object>>> getWsStudentByCollegeStats() {
        return ApiResponse.success(dashboardService.getWsStudentByCollegeStats());
    }

    /**
     * 学生维度统计（按贫困等级）
     */
    @GetMapping("/ws/student-poverty-stats")
    public ApiResponse<List<Map<String, Object>>> getWsStudentByPovertyStats() {
        return ApiResponse.success(dashboardService.getWsStudentByPovertyStats());
    }

    /**
     * 薪酬维度统计（月度）
     */
    @GetMapping("/ws/salary-monthly-stats")
    public ApiResponse<List<Map<String, Object>>> getWsSalaryMonthlyStats() {
        return ApiResponse.success(dashboardService.getWsSalaryMonthlyStats());
    }

    /**
     * 薪酬维度统计（学期）
     */
    @GetMapping("/ws/salary-term-stats")
    public ApiResponse<List<Map<String, Object>>> getWsSalaryTermStats() {
        return ApiResponse.success(dashboardService.getWsSalaryTermStats());
    }
}
