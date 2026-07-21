package edu.dashboard.controller;

import edu.dashboard.common.Result;
import edu.dashboard.domain.dto.CustomReportReqDTO;
import edu.dashboard.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * FR-3.16-001: 获取自定义报表数据 (用于前端预览)
     */
    @PostMapping("/custom")
    public Result<List<Map<String, Object>>> getCustomReport(@RequestBody CustomReportReqDTO reqDTO) {
        return Result.success(reportService.generateReport(reqDTO));
    }

    /**
     * FR-3.16-004: 导出Excel
     */
    @PostMapping("/export/excel")
    public void exportExcel(@RequestBody CustomReportReqDTO reqDTO, HttpServletResponse response) throws IOException {
        reportService.exportExcel(response, reqDTO);
    }
}