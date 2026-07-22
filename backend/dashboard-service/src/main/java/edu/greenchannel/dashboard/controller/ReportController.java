package edu.greenchannel.dashboard.controller;

import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.dashboard.domain.dto.CustomReportReqDTO;
import edu.greenchannel.dashboard.service.ReportService;
import edu.greenchannel.dashboard.service.ModuleDataService;
import edu.greenchannel.dashboard.service.ModuleRegistry;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
@RequirePermission("school:dashboard:view")
public class ReportController {

    @Autowired
    private ReportService reportService;
    @Autowired
    private ModuleRegistry moduleRegistry;

    /**
     * FR-3.16-001: 获取自定义报表数据 (用于前端预览)
     */
    @PostMapping("/custom")
    public ApiResponse<List<Map<String, Object>>> getCustomReport(@Valid @RequestBody CustomReportReqDTO reqDTO) {
        return previewReport(reqDTO);
    }

    @PostMapping("/preview")
    public ApiResponse<List<Map<String, Object>>> previewReport(@Valid @RequestBody CustomReportReqDTO reqDTO) {
        return ApiResponse.success(moduleService(reqDTO).getReportData(reqDTO));
    }

    /**
     * FR-3.16-004: 导出Excel
     */
    @PostMapping("/export/excel")
    public void exportExcel(@Valid @RequestBody CustomReportReqDTO reqDTO, HttpServletResponse response) throws IOException {
        export(reqDTO, response);
    }

    @PostMapping("/export")
    public void export(@Valid @RequestBody CustomReportReqDTO reqDTO, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> data = moduleService(reqDTO).getReportData(reqDTO);
        List<String> fields = reqDTO.getFields();
        if ((fields == null || fields.isEmpty()) && !data.isEmpty()) {
            fields = data.get(0).keySet().stream().toList();
        }
        reportService.exportRows(response, reqDTO, data, fields == null ? List.of() : fields);
    }

    private ModuleDataService moduleService(CustomReportReqDTO request) {
        String module = request.getModule() == null || request.getModule().isBlank() ? "base" : request.getModule();
        request.setModule(module);
        return moduleRegistry.getModuleService(module);
    }
}
