package edu.dashboard.controller;

import com.alibaba.excel.EasyExcel;
import edu.dashboard.common.FieldWhiteList;
import edu.dashboard.common.Result;
import edu.dashboard.domain.dto.CustomReportReqDTO;
import edu.dashboard.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private FieldWhiteList fieldWhiteList;

    /**
     * FR-3.16-001: 获取自定义报表数据 (用于前端预览)
     */
    @PostMapping("/custom")
    public Result<List<Map<String, Object>>> getCustomReport(@RequestBody CustomReportReqDTO reqDTO) {
        return Result.success(reportService.generateReport(reqDTO));
    }

    /**
     * FR-3.16-004: 统一Excel导出接口
     * 支持所有模块的报表导出
     */
    @PostMapping("/export/excel")
    public void exportExcel(@RequestBody CustomReportReqDTO reqDTO, HttpServletResponse response) throws IOException {
        // 1. 获取实际字段列表（使用默认或自定义）
        List<String> fields = reqDTO.getActualFields(fieldWhiteList);

        // 2. 查询数据
        List<Map<String, Object>> data = reportService.generateReport(reqDTO);

        // 3. 生成Excel
        String fileName = URLEncoder.encode(reqDTO.getTemplateName() != null ?
                reqDTO.getTemplateName() : "数据报表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 4. 动态表头
        List<List<String>> head = createDynamicHead(fields);

        // 5. 动态数据行
        List<List<Object>> rows = createDynamicRows(data, fields);

        EasyExcel.write(response.getOutputStream())
                .head(head)
                .sheet("数据报表")
                .doWrite(rows);
    }

    /**
     * FR-3.20-011: 快捷导出接口（兼容旧接口）
     * 推荐使用上面的统一接口
     */
    @GetMapping("/quick-export")
    public void quickExport(
            @RequestParam String module,
            @RequestParam String reportType,
            HttpServletResponse response) throws IOException {

        CustomReportReqDTO reqDTO = new CustomReportReqDTO();
        reqDTO.setModule(module);
        reqDTO.setReportType(reportType);
        reqDTO.setTemplateName(module + "-" + reportType);

        exportExcel(reqDTO, response);
    }

    // ========== 私有方法 ==========

    private List<List<String>> createDynamicHead(List<String> fields) {
        return fields.stream()
                .map(field -> List.of(fieldWhiteList.FIELD_CHINESE_NAMES.getOrDefault(field, field)))
                .toList();
    }

    private List<List<Object>> createDynamicRows(List<Map<String, Object>> data, List<String> fields) {
        return data.stream()
                .map(row -> fields.stream()
                        .map(field -> row.get(field))
                        .toList())
                .toList();
    }
}