package edu.greenchannel.dashboard.controller;

import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.dashboard.domain.dto.CustomReportReqDTO;
import edu.greenchannel.dashboard.service.ReportService;
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

    /**
     * FR-3.16-001: 获取自定义报表数据 (用于前端预览)
     */
    @PostMapping("/custom")
    public ApiResponse<List<Map<String, Object>>> getCustomReport(@Valid @RequestBody CustomReportReqDTO reqDTO) {
        return ApiResponse.success(reportService.generateReport(reqDTO));
    }

    /**
     * FR-3.16-004: 导出Excel
     */
    @PostMapping("/export/excel")
    public void exportExcel(@Valid @RequestBody CustomReportReqDTO reqDTO, HttpServletResponse response) throws IOException {
        reportService.exportExcel(response, reqDTO);
    }
}
