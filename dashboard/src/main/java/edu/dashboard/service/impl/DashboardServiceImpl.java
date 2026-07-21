package edu.dashboard.service.impl;

import edu.dashboard.domain.vo.CollegeCompareVO;
import edu.dashboard.domain.vo.DashboardStatsVO;
import edu.dashboard.domain.vo.HeatmapVO;
import edu.dashboard.domain.vo.SubsidyStructureVO;
import edu.dashboard.mapper.DashboardMapper;
import edu.dashboard.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Override
    public DashboardStatsVO getCachedStats() {
        return dashboardMapper.selectCoreStats();
    }

    @Override
    public List<Map<String, Object>> getWsPositionStats() {
        return dashboardMapper.selectWsPositionStats();
    }

    @Override
    public List<Map<String, Object>> getWsStudentByCollegeStats() {
        return dashboardMapper.selectWsStudentByCollegeStats();
    }

    @Override
    public List<Map<String, Object>> getWsStudentByPovertyStats() {
        return dashboardMapper.selectWsStudentByPovertyStats();
    }

    @Override
    public List<Map<String, Object>> getWsSalaryMonthlyStats() {
        return dashboardMapper.selectWsSalaryMonthlyStats();
    }

    @Override
    public List<Map<String, Object>> getWsSalaryTermStats() {
        return dashboardMapper.selectWsSalaryTermStats();
    }

    @Override
    public List<CollegeCompareVO> getCollegeComparison() {
        return dashboardMapper.selectCollegeCompare();
    }

    @Override
    public List<HeatmapVO> getOriginHeatmapData() {
        return dashboardMapper.selectOriginHeatmap();
    }

    @Override
    public List<SubsidyStructureVO> getSubsidyStructure() {
        return dashboardMapper.selectSubsidyStructure();
    }

    @Override
    public List<Map<String, Object>> getYearlyTrend(String currentYear) {
        return dashboardMapper.selectYearlyTrend(currentYear);
    }
}