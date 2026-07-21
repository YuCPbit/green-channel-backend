package edu.dashboard.service;

import edu.dashboard.common.FieldWhiteList;
import edu.dashboard.domain.dto.CustomReportReqDTO;
import edu.dashboard.mapper.ReportMapper;
import edu.dashboard.mapper.WorkStudyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private WorkStudyMapper workStudyMapper;

    @Autowired
    private FieldWhiteList fieldWhiteList;

    /**
     * FR-3.16-001: 生成动态报表数据
     */
    public List<Map<String, Object>> generateReport(CustomReportReqDTO reqDTO) {
        String module = reqDTO.getModule();
        List<String> fields = reqDTO.getActualFields(fieldWhiteList);

        // 根据模块路由到不同的数据源
        return switch (module) {
            case "basic" -> {
                List<String> safeColumns = fieldWhiteList.validateAndGetColumns("basic", fields);
                yield reportMapper.selectDynamicReport(safeColumns, reqDTO.getFilters());
            }
            case "workstudy" -> getWorkStudyReport(reqDTO.getReportType());
            default -> throw new IllegalArgumentException("未知模块: " + module);
        };
    }

    /**
     * 获取勤工助学报表数据
     */
    private List<Map<String, Object>> getWorkStudyReport(String reportType) {
        return switch (reportType) {
            case "position" -> workStudyMapper.selectPositionStats();
            case "student-college" -> workStudyMapper.selectStudentByCollegeStats();
            case "student-poverty" -> workStudyMapper.selectStudentByPovertyStats();
            case "salary-monthly" -> workStudyMapper.selectSalaryMonthlyStats();
            case "salary-term" -> workStudyMapper.selectSalaryTermStats();
            default -> throw new IllegalArgumentException("未知报表类型: " + reportType);
        };
    }
}