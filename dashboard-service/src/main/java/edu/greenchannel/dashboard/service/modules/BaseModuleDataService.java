package edu.greenchannel.dashboard.service.modules;

import edu.greenchannel.common.BusinessException;
import edu.greenchannel.dashboard.domain.dto.CustomReportReqDTO;
import edu.greenchannel.dashboard.service.DashboardService;
import edu.greenchannel.dashboard.service.ModuleDataService;
import edu.greenchannel.dashboard.service.ReportService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BaseModuleDataService implements ModuleDataService {
    private final DashboardService dashboardService;
    private final ReportService reportService;

    public BaseModuleDataService(DashboardService dashboardService, ReportService reportService) {
        this.dashboardService = dashboardService;
        this.reportService = reportService;
    }

    @Override
    public String getModuleCode() {
        return "base";
    }

    @Override
    public Object getCoreStats() {
        return dashboardService.getCachedStats();
    }

    @Override
    public Object getChartData(String chartType) {
        return switch (chartType) {
            case "college-compare" -> dashboardService.getCollegeComparison();
            case "heatmap" -> dashboardService.getOriginHeatmapData();
            case "subsidy-structure" -> dashboardService.getSubsidyStructure();
            default -> throw new BusinessException(40000, "不支持的基础图表类型: " + chartType);
        };
    }

    @Override
    public List<Map<String, Object>> getReportData(CustomReportReqDTO request) {
        if (!"student-list".equals(request.getReportType())) {
            throw new BusinessException(40000, "不支持的基础报表类型: " + request.getReportType());
        }
        return reportService.generateReport(request);
    }
}
