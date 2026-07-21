package edu.dashboard.service;

import edu.dashboard.domain.vo.*;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    DashboardStatsVO getCachedStats();
    List<CollegeCompareVO> getCollegeComparison();
    List<HeatmapVO> getOriginHeatmapData();
    List<SubsidyStructureVO> getSubsidyStructure();
    List<Map<String, Object>> getYearlyTrend(String currentYear);

    List<Map<String, Object>> getWsPositionStats();
    List<Map<String, Object>> getWsStudentByCollegeStats();
    List<Map<String, Object>> getWsStudentByPovertyStats();
    List<Map<String, Object>> getWsSalaryMonthlyStats();
    List<Map<String, Object>> getWsSalaryTermStats();
}