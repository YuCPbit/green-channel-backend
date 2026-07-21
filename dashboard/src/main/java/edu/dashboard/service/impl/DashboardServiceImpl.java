package edu.dashboard.service.impl;

import edu.dashboard.domain.vo.*;
import edu.dashboard.mapper.DashboardMapper;
import edu.dashboard.mapper.WorkStudyMapper;
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

    @Autowired
    private WorkStudyMapper workStudyMapper;

    @Override
    public DashboardStatsVO getStatsByModule(String module) {
        return switch (module) {
            case "basic" -> dashboardMapper.selectCoreStats();
            case "workstudy" -> {
                DashboardStatsVO vo = new DashboardStatsVO();
                vo.setWsTotalPositions(workStudyMapper.countPositions());
                vo.setWsTotalHired(workStudyMapper.countHired());
                vo.setWsMonthlySalary(workStudyMapper.sumMonthlySalary());
                vo.setUpdateTime(java.time.LocalDateTime.now());
                yield vo;
            }
            default -> throw new IllegalArgumentException("未知模块: " + module);
        };
    }

    // ========== 基础模块方法 ==========
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

    // ========== 勤工助学模块方法 ==========
    @Override
    public List<Map<String, Object>> getWsPositionStats() {
        return workStudyMapper.selectPositionStats();
    }

    @Override
    public List<Map<String, Object>> getWsStudentByCollegeStats() {
        return workStudyMapper.selectStudentByCollegeStats();
    }

    @Override
    public List<Map<String, Object>> getWsStudentByPovertyStats() {
        return workStudyMapper.selectStudentByPovertyStats();
    }

    @Override
    public List<Map<String, Object>> getWsSalaryMonthlyStats() {
        return workStudyMapper.selectSalaryMonthlyStats();
    }

    @Override
    public List<Map<String, Object>> getWsSalaryTermStats() {
        return workStudyMapper.selectSalaryTermStats();
    }
}