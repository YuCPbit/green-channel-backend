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
}