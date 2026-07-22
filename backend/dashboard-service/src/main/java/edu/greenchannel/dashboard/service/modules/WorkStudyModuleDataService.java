package edu.greenchannel.dashboard.service.modules;

import edu.greenchannel.common.BusinessException;
import edu.greenchannel.dashboard.common.FieldWhiteList;
import edu.greenchannel.dashboard.domain.dto.CustomReportReqDTO;
import edu.greenchannel.dashboard.mapper.DashboardMapper;
import edu.greenchannel.dashboard.service.DashboardService;
import edu.greenchannel.dashboard.service.ModuleDataService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class WorkStudyModuleDataService implements ModuleDataService {
    private final DashboardService dashboardService;
    private final DashboardMapper dashboardMapper;
    private final FieldWhiteList fieldWhiteList;

    public WorkStudyModuleDataService(DashboardService dashboardService, DashboardMapper dashboardMapper,
                                      FieldWhiteList fieldWhiteList) {
        this.dashboardService = dashboardService;
        this.dashboardMapper = dashboardMapper;
        this.fieldWhiteList = fieldWhiteList;
    }

    @Override
    public String getModuleCode() {
        return "workstudy";
    }

    @Override
    public Object getCoreStats() {
        return dashboardMapper.selectWsCoreStats();
    }

    @Override
    public Object getChartData(String chartType) {
        return switch (chartType) {
            case "position-stats" -> dashboardService.getWsPositionStats();
            case "student-college-stats" -> dashboardService.getWsStudentByCollegeStats();
            case "student-poverty-stats" -> dashboardService.getWsStudentByPovertyStats();
            case "salary-monthly-stats" -> dashboardService.getWsSalaryMonthlyStats();
            case "salary-term-stats" -> dashboardService.getWsSalaryTermStats();
            default -> throw new BusinessException(40000, "不支持的勤工助学图表类型: " + chartType);
        };
    }

    @Override
    public List<Map<String, Object>> getReportData(CustomReportReqDTO request) {
        if (request.getReportType() == null || request.getReportType().isBlank()) {
            throw new BusinessException(40000, "勤工助学报表类型不能为空");
        }
        Map<String, Object> filters = request.getFilters() == null ? Map.of() : request.getFilters();
        List<Map<String, Object>> rows = switch (request.getReportType()) {
            case "position-stat" -> dashboardMapper.selectWsPositionReport(longFilter(filters, "batchId"));
            case "student-stat" -> dashboardMapper.selectWsStudentReport(longFilter(filters, "collegeId"));
            case "salary-stat" -> dashboardMapper.selectWsSalaryReport(
                    integerFilter(filters, "year"), integerFilter(filters, "month"));
            case "position-detail" -> dashboardMapper.selectWsPositionDetail(integerFilter(filters, "status"));
            default -> throw new BusinessException(40000, "不支持的勤工助学报表类型: " + request.getReportType());
        };
        List<String> fields = fieldWhiteList.validateReportFields(getModuleCode(), request.getReportType(),
                request.getFields());
        request.setFields(fields);
        return fieldWhiteList.projectRows(rows, fields);
    }

    private Long longFilter(Map<String, Object> filters, String name) {
        Object value = filters.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException exception) {
            throw new BusinessException(40000, name + " 必须是整数");
        }
    }

    private Integer integerFilter(Map<String, Object> filters, String name) {
        Long value = longFilter(filters, name);
        if (value == null) {
            return null;
        }
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new BusinessException(40000, name + " 超出允许范围");
        }
        return value.intValue();
    }
}
