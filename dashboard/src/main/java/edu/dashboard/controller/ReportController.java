package edu.dashboard.controller;

import com.alibaba.excel.EasyExcel;
import edu.dashboard.common.FieldWhiteList;
import edu.dashboard.common.Result;
import edu.dashboard.domain.dto.CustomReportReqDTO;
import edu.dashboard.service.ModuleRegistry;
import edu.dashboard.service.ModuleDataService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ModuleRegistry moduleRegistry;
    private final FieldWhiteList fieldWhiteList;

    public ReportController(ModuleRegistry moduleRegistry, FieldWhiteList fieldWhiteList) {
        this.moduleRegistry = moduleRegistry;
        this.fieldWhiteList = fieldWhiteList;
    }

    /**
     * 统一报表导出接口
     * POST /api/report/export
     */
    @PostMapping("/export")
    public void exportExcel(@RequestBody CustomReportReqDTO reqDTO, HttpServletResponse response) throws IOException {
        ModuleDataService service = moduleRegistry.getModuleService(reqDTO.getModule());
        List<Map<String, Object>> data = service.getReportData(reqDTO.getReportType(), reqDTO.getFilters());

        String fileName = URLEncoder.encode(reqDTO.getTemplateName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 动态表头
        List<String> fields = fieldWhiteList.getDefaultFields(reqDTO.getModule(), reqDTO.getReportType());
        List<List<String>> head = fields.stream()
                .map(field -> List.of(fieldWhiteList.getChineseHeader(field)))
                .toList();

        // 动态数据
        List<List<Object>> rows = data.stream()
                .map(row -> fields.stream().map(field -> row.get(field)).toList())
                .toList();

        EasyExcel.write(response.getOutputStream())
                .head(head)
                .sheet("Sheet1")
                .doWrite(rows);
    }

    /**
     * 报表预览接口
     */
    @PostMapping("/preview")
    public Result<List<Map<String, Object>>> previewReport(@RequestBody CustomReportReqDTO reqDTO) {
        ModuleDataService service = moduleRegistry.getModuleService(reqDTO.getModule());
        return Result.success(service.getReportData(reqDTO.getReportType(), reqDTO.getFilters()));
    }
}