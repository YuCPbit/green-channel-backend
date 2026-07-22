package edu.greenchannel.dashboard.service;

import edu.greenchannel.dashboard.domain.dto.CustomReportReqDTO;

import java.util.List;
import java.util.Map;

public interface ModuleDataService {
    String getModuleCode();

    Object getCoreStats();

    Object getChartData(String chartType);

    List<Map<String, Object>> getReportData(CustomReportReqDTO request);
}
